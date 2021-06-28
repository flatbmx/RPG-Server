package com.podts.rpg.server.network.netty;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.podts.rpg.server.Server;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.model.universe.Universe;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.packet.AcknowledgementPacket;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.LoginPacket;
import com.podts.rpg.server.network.packet.MessagePacket;
import com.podts.rpg.server.network.packet.PingPacket;
import com.podts.rpg.server.network.packet.RSAHandShakePacket;
import com.podts.rpg.server.network.packet.TileSelectionPacket;
import com.podts.rpg.server.network.packet.TileSelectionPacket.SelectionType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

class DefaultPacketDecoder extends ByteToMessageDecoder {
	
	private static final PacketConstructor[] packetConstructors;
	
	private static final int PID_RSAHANDSHAKE = 0;
	private static final int PID_LOGINREQUEST = 1;
	private static final int PID_PING = 2;
	private static final int PID_MOVE = 3;
	private static final int PID_MESSAGE = 4;
	private static final int PID_TILESELECTION = 5;
	
	private static final Map<Byte,TileSelectionPacket.SelectionType> selectionMap = new HashMap<>();
	
	private static final TileSelectionPacket.SelectionType getSelectionTypeFromByte(byte b) {
		return selectionMap.get(b);
	}
	
	static {
		
		selectionMap.put(Byte.valueOf("0"), SelectionType.ADD);
		selectionMap.put(Byte.valueOf("1"), SelectionType.REMOVE);
		selectionMap.put(Byte.valueOf("2"), SelectionType.TOTAL);
		
		
		packetConstructors = new PacketConstructor[128];

		// RSAHandShake Constructor
		packetConstructors[PID_RSAHANDSHAKE] = new PacketConstructor() {
			@Override
			public RSAHandShakePacket construct(NetworkStream s, int size, byte opCode, ByteBuf buf) {
				byte[] keyBytes = new byte[size];
				buf.readBytes(keyBytes, 0, size);
				try {
					PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
					return new RSAHandShakePacket(publicKey);
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		
		// LoginPacket Constructor
		packetConstructors[PID_LOGINREQUEST] = new PacketConstructor() {
			@Override
			public LoginPacket construct(NetworkStream s, int size, byte opCode, ByteBuf buf) {
				String username = readEncryptedString(s, buf);
				String password = readEncryptedString(s, buf);
				return new LoginPacket(username, password);
			}
		};
		
		packetConstructors[PID_PING] = new AcknowledgementPacketConstructor() {
			@Override
			public PingPacket construct(NetworkStream s, int size, byte opCode, ByteBuf buf) {
				return new PingPacket(buf.readInt());
			}
		};
		
		packetConstructors[PID_MOVE] = new PacketConstructor() {
			@Override
			public EntityPacket construct(NetworkStream s, int size, byte opCode, ByteBuf buf) {
				Location newLocation = readLocation(buf);
				return EntityPacket.constructMove(s.getPlayer().getEntity(), newLocation);
			}
		};
		
		packetConstructors[PID_MESSAGE] = new PacketConstructor() {
			@Override
			public MessagePacket construct(NetworkStream s, int size, byte opCode, ByteBuf buf) {
				String message = readEncryptedString(s, buf);
				return new MessagePacket(s.getPlayer(), message);
			}
		};
		
		packetConstructors[PID_TILESELECTION] = new PacketConstructor() {
			@Override
			public TileSelectionPacket construct(NetworkStream s, int size, byte opCode, ByteBuf buf) {
				Collection<Tile> tiles = new HashSet<>();
				byte typeByte = buf.readByte();
				final TileSelectionPacket.SelectionType type = getSelectionTypeFromByte(typeByte);
				int totalTiles = buf.readInt();
				for(int i=0; i<totalTiles; ++i) { 
					tiles.add(readLocation(buf).getTile());
				}
				return new TileSelectionPacket(type, tiles);
			}
		};
		
	}

	@Override
	protected void decode(ChannelHandlerContext c, ByteBuf buf, List<Object> out) throws Exception {

		NetworkStream stream = (NetworkStream) c.channel();

		int size = buf.readInt();
		byte opCode = buf.readByte(); 

		if(opCode > -1 && opCode < packetConstructors.length) {
			if(packetConstructors[opCode] != null) {
				Packet packet = packetConstructors[opCode].construct(stream, size - 1, opCode, buf);
				if(packet != null) {
					NettyNetworkManager.get().doSetPacketStream(packet, stream);
					out.add(packet);
				}
			} else {
				stream.flag();
				Server.get().getLogger().warning("Recieved unknown Packet OPCODE = " + opCode + " with size " + (size-1) + " from " + stream.ownerString());
				buf.skipBytes(size-1);
			}
		}

	}
	
	private static final Location readLocation(ByteBuf buf) {
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		return Universe.get().getDefaultWorld().createLocation(x, y, z);
	}
	
	private static String readEncryptedString(NetworkStream networkStream, ByteBuf buf) {
		int encryptedLength = buf.readInt();
		byte[] encryptedBytes = new byte[encryptedLength];
		buf.readBytes(encryptedBytes);
		ByteBuf realBuf = Unpooled.buffer();
		realBuf.writeBytes(decrypt(encryptedBytes, networkStream.getSecretKey()));
		realBuf.resetReaderIndex();
		int size = realBuf.readInt();
		byte[] realChars = new byte[size];
		realBuf.readBytes(realChars);
		return decodeString(realChars);
	}
	
	private static final String decodeString(byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("Unable to encode UTF-8!");
		}
	}
	
	public static final ByteBuf decryptBuf(ByteBuf buf, SecretKey secretKey) {
		return decryptBuf(buf, buf.readInt(), secretKey);
	}
	
	public static final ByteBuf decryptBuf(ByteBuf buf, int size, SecretKey secretKey) {
		final ByteBuf rawBuf = Unpooled.buffer();
		byte[] encBytes = new byte[size];
		buf.readBytes(encBytes);
		rawBuf.writeBytes(decrypt(encBytes, secretKey));
		rawBuf.resetReaderIndex();
		return rawBuf;
	}
	
	public static byte[] decrypt(byte[] bytes, SecretKey secretKey) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] decrypt(byte[] bytes, PrivateKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return cipher.doFinal(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static interface PacketConstructor {
		public Packet construct(NetworkStream s, int size, byte opCode, ByteBuf buf);
	}
	
	private static interface AcknowledgementPacketConstructor extends PacketConstructor {
		public AcknowledgementPacket construct(NetworkStream s, int size, byte opCode, ByteBuf buf);
	}
	
	DefaultPacketDecoder() {

	}

}
