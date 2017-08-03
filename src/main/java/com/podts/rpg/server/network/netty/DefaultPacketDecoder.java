package com.podts.rpg.server.network.netty;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.Stream;
import com.podts.rpg.server.network.packet.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

class DefaultPacketDecoder extends ByteToMessageDecoder {

	private static final PacketConstructor[] packetConstructors;
	
	private static final int PID_RSAHANDSHAKE = 0;
	private static final int PID_LOGINREQUST = 1;
	private static final int PID_MOVE = 2;
	private static final int PID_MESSAGE = 3;
	
	static {
		packetConstructors = new PacketConstructor[128];

		// RSAHandShake Constructor
		packetConstructors[PID_RSAHANDSHAKE] = new PacketConstructor() {
			@Override
			public Packet construct(Stream s, int size, byte opCode, ByteBuf buf) {
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
		packetConstructors[PID_LOGINREQUST] = new PacketConstructor() {
			@Override
			public Packet construct(Stream s, int size, byte opCode, ByteBuf buf) {
				String username = readEncryptedString(s, buf);
				String password = readEncryptedString(s, buf);
				return new LoginPacket(username, password);
			}
		};
	}

	@Override
	protected void decode(ChannelHandlerContext c, ByteBuf buf, List<Object> out) throws Exception {

		Stream stream = (Stream) c.channel();

		int size = buf.readInt();
		byte opCode = buf.readByte();

		if(opCode > -1 && opCode < packetConstructors.length) {
			if(packetConstructors[opCode] != null) {
				Packet p = packetConstructors[opCode].construct(stream, size - 1, opCode, buf);
				if(p != null) {
					System.out.println("Recieved " + p.getClass().getSimpleName());
					out.add(p);
				}
					
			}
		}

	}
	
	private static String readEncryptedString(Stream stream, ByteBuf buf) {
		int encryptedLength = buf.readInt();
		byte[] encryptedBytes = new byte[encryptedLength];
		buf.readBytes(encryptedBytes);
		ByteBuf realBuf = Unpooled.buffer();
		realBuf.writeBytes(decrypt(encryptedBytes, stream.getSecretKey()));
		realBuf.resetReaderIndex();
		int size = realBuf.readInt();
		String result = null;
		byte[] realChars = new byte[size];
		realBuf.readBytes(realChars);
		try {
			result = new String(realChars, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
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
		public Packet construct(Stream s, int size, byte opCode, ByteBuf buf);
	}

	DefaultPacketDecoder() {

	}

}
