package com.podts.rpg.server.network.netty;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.podts.rpg.server.Server;
import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.HasLocation;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.model.universe.TileElement.TileType;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.AcknowledgePacket;
import com.podts.rpg.server.network.packet.AcknowledgementPacket;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
import com.podts.rpg.server.network.packet.MessagePacket;
import com.podts.rpg.server.network.packet.PingPacket;
import com.podts.rpg.server.network.packet.PlayerInitPacket;
import com.podts.rpg.server.network.packet.StatePacket;
import com.podts.rpg.server.network.packet.TilePacket;
import com.podts.rpg.server.network.packet.TilePacket.TileSendType;
import com.podts.rpg.server.network.packet.TilePacket.TileUpdateType;
import com.podts.rpg.server.network.packet.TileSelectionPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

class DefaultPacketEncoder extends MessageToByteEncoder<Packet> {
	
	private static final Map<Class<? extends Packet>,PacketEncoder> encoders = new HashMap<Class<? extends Packet>, PacketEncoder>();
	
	private static final void addEncoder(Class<? extends Packet> c, PacketEncoder encoder) {
		if(encoders.containsKey(c)) throw new IllegalArgumentException(c.getSimpleName() + " already has an encoder.");
		encoders.put(c, encoder);
		encoder.init();
	}
	
	private static final byte PID_AESREPLY = 0;
	private static final byte PID_LOGINRESPONSE = 1;
	private static final byte PID_PING = 2;
	private static final byte PID_TILE = 3;
	private static final byte PID_INIT = 4;
	private static final byte PID_STATE = 5;
	private static final byte PID_ENTITY = 6;
	private static final byte PID_MESSAGE = 7;
	private static final byte PID_ACK = 8;
	private static final byte PID_TILESELECTION = 9;
	
	private static final String STRING_ENCODING = "UTF-8";
	
	private static final Logger getLogger() {
		return Server.get().getLogger();
	}
	
	private final static Map<TileType,Integer> tileTypes = new EnumMap<>(TileType.class);
	
	private final static int getTileID(Tile tile) {
		return getTileID(tile.getType());
	}
	
	private final static int getTileID(TileType type) {
		return tileTypes.get(type);
	}
	
	static {
		
		for(TileType type : TileType.values()) {
			tileTypes.put(type, type.ordinal());
		}
		
		addEncoder(AESReplyPacket.class, new PacketEncoder(PID_AESREPLY) {
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				AESReplyPacket p = (AESReplyPacket) op;
				
				//Convert secret into bytes.
				byte[] encodedSecret = p.getSecret().getEncoded();
				//Encrypt secret bytes using public key.
				byte[] encryptedSecret = encrypt(encodedSecret, p.getPublicKey());
				//Write encrypted secret into buffer.
				buf.writeBytes(encryptedSecret);
			}
		});
		
		addEncoder(LoginResponsePacket.class, new PacketEncoder(PID_LOGINRESPONSE) {
			private final Map<LoginResponseType,Integer> responseTypeMap = new EnumMap<>(LoginResponseType.class);
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				LoginResponsePacket p = (LoginResponsePacket) op;
				buf.writeByte(responseTypeMap.get(p.getType()));
				writeString(p.getResponse(), buf);
			}
			void init() {
				responseTypeMap.put(LoginResponseType.WAIT, 0);
				responseTypeMap.put(LoginResponseType.ACCEPT, 1);
				responseTypeMap.put(LoginResponseType.DECLINE, 2);
			}
		});
		
		addEncoder(PingPacket.class, new AcknowledgementPacketEncoder(PID_PING));
		
		addEncoder(StatePacket.class, new PacketEncoder(PID_STATE) {
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				StatePacket p = (StatePacket) op;
				buf.writeByte(p.getState().getID());
			}
		});
		
		addEncoder(PlayerInitPacket.class, new PacketEncoder(PID_INIT) {
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				PlayerInitPacket p = (PlayerInitPacket) op;
				buf.writeInt(p.getPlayer().getID());
				writeLocation(p.getPlayer().getEntity().getLocation(), buf);
			}
		});
		
		addEncoder(TilePacket.class, new PacketEncoder(PID_TILE) {
			private final Map<TileUpdateType,Integer> updateTypes = new EnumMap<>(TileUpdateType.class);
			private final Map<TileSendType,Integer> sendTypes = new EnumMap<>(TileSendType.class);
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				TilePacket p = (TilePacket) op;
				buf.writeByte(updateTypes.get(p.getUpdateType()))
				.writeByte(sendTypes.get(p.getSendType()));
				if(TileUpdateType.CREATE.equals(p.getUpdateType())) {
					if(p.getSendType().equals(TileSendType.SINGLE)) {
						Tile tile = p.getTile();
						buf.writeByte(getTileID(tile));
						writeLocation(tile.getLocation(), buf);
					} else if(p.getSendType().equals(TileSendType.GROUP)) {
						writeGridTiles(p.getTiles(), buf);
					}
				} else if(TileUpdateType.DESTROY.equals(p.getUpdateType())) {
					if(p.getSendType().equals(TileSendType.SINGLE)) {
						Tile tile = p.getTile();
						writeLocation(tile.getLocation(), buf);
					} else if(p.getSendType().equals(TileSendType.GROUP)) {
						Tile[][] tiles = p.getTiles();
						writeLocation(tiles[0][0].getLocation(), buf);
						buf.writeInt(tiles.length)
						.writeInt(tiles[0].length);
					}
				}
			}
			
			@Override
			void init() {
				updateTypes.put(TileUpdateType.CREATE, 0);
				updateTypes.put(TileUpdateType.DESTROY, 1);
				
				sendTypes.put(TileSendType.GROUP, 0);
				sendTypes.put(TileSendType.SINGLE, 1);
			}
		});
		
		addEncoder(EntityPacket.class, new PacketEncoder(PID_ENTITY) {
			private final Map<EntityPacket.UpdateType,Integer> packetTypeMap = new EnumMap<EntityPacket.UpdateType,Integer>(EntityPacket.UpdateType.class);
			private final Map<EntityType,Integer> entityTypeMap = new EnumMap<EntityType,Integer>(EntityType.class);
			void init() {
				packetTypeMap.put(EntityPacket.UpdateType.CREATE, 0);
				packetTypeMap.put(EntityPacket.UpdateType.UPDATE, 1);
				packetTypeMap.put(EntityPacket.UpdateType.DESTROY, 2);
				
				entityTypeMap.put(EntityType.PLAYER, 0);
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
					writeString(e.getName(), buf);
					buf.writeByte(entityTypeMap.get(e.getType()));
				case UPDATE:
					writeLocation(e.getLocation(), buf);
					break;
				}
			}
		});
		
		addEncoder(AcknowledgePacket.class, new PacketEncoder(PID_ACK) {
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				AcknowledgePacket p = (AcknowledgePacket) op;
				buf.writeInt(p.getACK());
			}
		});
		
		addEncoder(MessagePacket.class, new PacketEncoder(PID_MESSAGE) {
			@Override
			public void encode(NettyStream stream, Packet op, ByteBuf buf) {
				MessagePacket p = (MessagePacket) op;
				writeEncryptedString(p.getMessage(), stream, buf);
			}
		});
		
		addEncoder(TileSelectionPacket.class, new PacketEncoder(PID_TILESELECTION) {
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				TileSelectionPacket p = (TileSelectionPacket) op;
				Collection<Tile> tiles = p.getSelections();
				writePlaneLocations(tiles, buf);
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
		} else {
			getLogger().warning("No encoder found for " + p.getClass().getSimpleName() + ", packet not sent!");
		}
		
	}
	
	DefaultPacketEncoder() {
		
	}
	
	private static void writeLocation(HasLocation loc, ByteBuf buf) {
		writeLocation(loc.getLocation(), buf);
	}
	
	private static void writeLocation(Location loc, ByteBuf buf) {
		buf.writeInt(loc.getX())
		.writeInt(loc.getY())
		.writeInt(loc.getZ());
	}
	
	private static void writePlaneLocations(Collection<? extends HasLocation> locs, ByteBuf buf) {
		Iterator<? extends HasLocation> it = locs.iterator();
		buf.writeInt(locs.size());
		writeLocation(it.next(), buf);
		while(it.hasNext()) {
			writePlaneLocation(it.next(), buf);
		}
	}
	
	private static void writePlaneLocation(HasLocation loc, ByteBuf buf) {
		writePlaneLocation(loc.getLocation(), buf);
	}
	
	private static void writePlaneLocation(Location loc, ByteBuf buf) {
		buf.writeInt(loc.getX())
		.writeInt(loc.getY());
	}
	
	private static <T extends Tile> void writeGridTiles(T[][] tiles, ByteBuf buf) {
		final int width = tiles.length;
		final int height = tiles[0].length;
		buf.writeInt(width).writeInt(height);
		writeLocation(tiles[0][0], buf);
		for(int j=0; j<height; ++j) {
			for(int i=0; i<width; ++i) {
				buf.writeByte(getTileID(tiles[i][j]));
			}
		}
	}
	
	private static void writeEncryptedLocation(Location loc, NetworkStream networkStream, ByteBuf buf) {
		ByteBuf plainBuf = Unpooled.buffer();
		writeLocation(loc, plainBuf);
		buf.writeBytes(encrypt(plainBuf.array(), networkStream.getSecretKey()));
	}
	
	private static void writeEncryptedString(String string, NetworkStream networkStream, ByteBuf buf) {
		try {
			ByteBuf plainBuf = Unpooled.buffer();
			byte[] plain = string.getBytes(STRING_ENCODING);
			plainBuf.writeInt(plain.length).writeBytes(plain);
			byte[] encryptedBytes = encrypt(plainBuf.array(), networkStream.getSecretKey());
			buf.writeInt(encryptedBytes.length).writeBytes(encryptedBytes);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("No string encoder for " + STRING_ENCODING + "!");
		}
	}
	
	private static void writeString(String string, ByteBuf buf) {
		try {
			byte[] plain = string.getBytes(STRING_ENCODING);
			buf.writeInt(plain.length);
			buf.writeBytes(plain);
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("No string encoder for " + STRING_ENCODING + "!");
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
	
	private static class AcknowledgementPacketEncoder extends PacketEncoder {
		
		@Override
		public final void encode(NettyStream s, Packet op, ByteBuf buf) {
			AcknowledgementPacket p = (AcknowledgementPacket) op;
			buf.writeInt(p.getACK());
			encodePayload(s, p, buf);
		}
		
		public void encodePayload(NettyStream s, AcknowledgementPacket op, ByteBuf buf) {
			
		}
		
		AcknowledgementPacketEncoder(int opCode) {
			super(opCode);
		}
		
	}
	
}
