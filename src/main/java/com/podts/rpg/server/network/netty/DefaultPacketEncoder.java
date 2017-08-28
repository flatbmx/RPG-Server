package com.podts.rpg.server.network.netty;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.RectangleTileSelction;
import com.podts.rpg.server.model.universe.Tile;
import com.podts.rpg.server.model.universe.Tile.TileType;
import com.podts.rpg.server.model.universe.TileSelection;
import com.podts.rpg.server.model.universe.TileSelection.SelectionType;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.Stream;
import com.podts.rpg.server.network.packet.AESReplyPacket;
import com.podts.rpg.server.network.packet.EntityPacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket;
import com.podts.rpg.server.network.packet.LoginResponsePacket.LoginResponseType;
import com.podts.rpg.server.network.packet.MessagePacket;
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
	
	private static final int PID_AESREPLY = 0;
	private static final int PID_LOGINRESPONSE = 1;
	private static final int PID_TILE = 2;
	private static final int PID_INIT = 3;
	private static final int PID_STATE = 4;
	private static final int PID_ENTITY = 5;
	private static final int PID_MESSAGE = 6;
	private static final int PID_TILESELECTION = 7;
	
	static {
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
			private final Map<TileType,Integer> tileTypes = new EnumMap<>(TileType.class);
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
						buf.writeByte(tileTypes.get(tile.getType()));
						writeLocation(tile.getLocation(), buf);
					} else if(p.getSendType().equals(TileSendType.GROUP)) {
						Tile[][] tiles = p.getTiles();
						writeLocation(tiles[0][0].getLocation(), buf);
						buf.writeInt(tiles.length)
						.writeInt(tiles[0].length);
						for(int y=0; y<tiles[0].length; ++y) {
							for(int x=0; x<tiles.length; ++x) {
								buf.writeByte(tileTypes.get(tiles[x][y].getType()));
							}
						}
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
			void init() {
				updateTypes.put(TileUpdateType.CREATE, 0);
				updateTypes.put(TileUpdateType.DESTROY, 1);
				
				sendTypes.put(TileSendType.GROUP, 0);
				sendTypes.put(TileSendType.SINGLE, 1);
				
				tileTypes.put(TileType.VOID, 0);
				tileTypes.put(TileType.DIRT, 1);
				tileTypes.put(TileType.GRASS, 2);
				tileTypes.put(TileType.WATER, 3);
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
					buf.writeByte(entityTypeMap.get(e.getType()));
				case UPDATE:
					writeLocation(e.getLocation(), buf);
					break;
				}
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
			private final Map<SelectionType,Integer> typeMap = new HashMap<SelectionType, Integer>();
			void init() {
				typeMap.put(SelectionType.SET, 0);
				typeMap.put(SelectionType.SQUARE, 1);
			}
			@Override
			public void encode(NettyStream s, Packet op, ByteBuf buf) {
				TileSelectionPacket p = (TileSelectionPacket) op;
				for(TileSelection sel : p.getSelections()) {
					buf.writeByte(typeMap.get(sel.getSelectionType()));
					
					if(sel instanceof RectangleTileSelction) {
						
						RectangleTileSelction rectSel = (RectangleTileSelction) sel;
						
						buf.writeInt(rectSel.getWidth()).writeInt(rectSel.getHeight());
						writeLocation(rectSel.getTopLeft(), buf);
						
					} else {
						
						buf.writeInt(sel.size());
						for(Tile tile : sel) {
							writeLocation(tile.getLocation(), buf);
						}
						
					}
				}
				
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
		buf.writeInt(loc.getX());
		buf.writeInt(loc.getY());
		buf.writeInt(loc.getZ());
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
	
	private static void writeString(String string, ByteBuf buf) {
		byte[] plain;
		try {
			plain = string.getBytes("UTF-8");
			buf.writeInt(plain.length).writeBytes(plain);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
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
