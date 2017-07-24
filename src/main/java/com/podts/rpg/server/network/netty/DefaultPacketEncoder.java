package com.podts.rpg.server.network.netty;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.Location;
import com.podts.rpg.server.model.entity.Entity;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.Stream;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.MessagePacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class DefaultPacketEncoder extends MessageToByteEncoder<Packet> {
	
	private static final Map<Class<? extends Packet>,PacketEncoder> encoders = new HashMap<Class<? extends Packet>, PacketEncoder>();
	
	private static final void addEncoder(Class<? extends Packet> c, PacketEncoder encoder) {
		encoders.put(c, encoder);
		encoder.init();
	}
	
	private static final int PID_AESREPLY = 0;
	private static final int PID_LOGINRESPONSE = 1;
	private static final int PID_ENTITY = 2;
	private static final int PID_MESSAGE = 3;
	
	static {
		addEncoder(AESReplyPacket.class, new PacketEncoder(PID_AESREPLY) {
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				AESReplyPacket p = (AESReplyPacket) op;
				
				buf.writeInt(p.getPlayer().getID());
				
				//Convert secret into bytes.
				byte[] encodedSecret = p.getSecret().getEncoded();
				//Encrypt secret bytes using public key.
				byte[] encryptedSecret = encrypt(encodedSecret, p.getPublicKey());
				//Write encrypted secret into buffer.
				buf.writeBytes(encryptedSecret);
			}
		});
		
		addEncoder(LoginResponsePacket.class, new PacketEncoder(PID_LOGINRESPONSE) {
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				
			}
		});
		
		addEncoder(EntityPacket.class, new PacketEncoder(PID_ENTITY) {
			private final Map<EntityPacket.UpdateType,Integer> packetTypeMap = new EnumMap<EntityPacket.UpdateType,Integer>(EntityPacket.UpdateType.class);
			private final Map<EntityType,Integer> entityTypeMap = new EnumMap<EntityType,Integer>(EntityType.class);
			void init() {
				packetTypeMap.put(EntityPacket.UpdateType.CREATE, 0);
				packetTypeMap.put(EntityPacket.UpdateType.UPDATE, 1);
				packetTypeMap.put(EntityPacket.UpdateType.DESTROY, 2);
				
				entityTypeMap.put(EntityType.SHIP_ESCAPEPOD, 0);
			}
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				EntityPacket p = (EntityPacket) op;
				Entity e = p.getEntity();
				buf.writeByte(packetTypeMap.get(p.getType()));
				buf.writeInt(e.getID());
				switch(p.getType()) {
				case DESTROY:
					break;
				case CREATE:
					buf.writeByte(entityTypeMap.get(e.getType()));
				case UPDATE:
					writeLocation(e.getLocation(), buf);
					break;
				}
			}
		});
		
		encoders.put(MessagePacket.class, new PacketEncoder(PID_MESSAGE) {
			@Override
			public void encode(NettyStream stream, Packet op, ByteBuf buf) {
				MessagePacket p = (MessagePacket) op;
				writeEncryptedString(p.getMessage(), stream, buf);
			}
		});
		
	}
	
	@Override
	protected void encode(ChannelHandlerContext c, Packet p, ByteBuf buf) throws Exception {
		
		NettyStream s = (NettyStream) c.channel();
		
		PacketEncoder encoder = encoders.get(p.getClass());
		
		if(encoder != null) {
			buf.writeByte(encoder.getOpCode());
			encoder.encode(s, p, buf);
		}
		
	}
	
	DefaultPacketEncoder() {
		
	}
	
	private static void writeLocation(Location loc, ByteBuf buf) {
		buf.writeDouble(loc.getX());
		buf.writeDouble(loc.getY());
	}
	
	private static void writeEncryptedLocation(Location loc, Stream stream, ByteBuf buf) {
		ByteBuf plainBuf = Unpooled.copiedBuffer(new byte[0]);
		writeLocation(loc, plainBuf);
		buf.writeBytes(encrypt(plainBuf.array(), stream.getSecretKey()));
	}
	
	private static void writeEncryptedString(String string, Stream stream, ByteBuf buf) {
		try {
			ByteBuf plainBuf = Unpooled.copiedBuffer(new byte[0]);
			byte[] plain = string.getBytes("UTF-8");
			plainBuf.writeInt(plain.length).writeBytes(plain);
			byte[] encryptedBytes = encrypt(plainBuf.array(), stream.getSecretKey());
			buf.writeInt(encryptedBytes.length).writeBytes(encryptedBytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}	
	}
	
	private static byte[] encrypt(byte[] bytes, SecretKey secretKey) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static byte[] encrypt(byte[] bytes, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return cipher.doFinal(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static abstract class PacketEncoder {
		
		private final int opCode;
		
		public final int getOpCode() {
			return opCode;
		}
		
		public abstract void encode(NettyStream s, Packet op, ByteBuf buf);
		
		void init() {
			
		}
		
		PacketEncoder(int opCode) {
			this.opCode = opCode;
		}
		
	}
	
}
