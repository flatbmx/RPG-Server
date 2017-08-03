package com.podts.rpg.server.network.netty;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.Stream;

import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

class NettyStream extends NioSocketChannel implements Stream {
	
	private static KeyGenerator keyGenerator;
	
	static {
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	private final SecretKey secret;
	protected Player player;
	
	@Override
	public SecretKey getSecretKey() {
		return secret;
	}
	
	public final Player getPlayer() {
		return player;
	}
	
	public final void setPlayer(Player player) {
		this.player = player;
	}
	
	@Override
	public void sendPacket(Packet p) {
		this.writeAndFlush(p);
	}
	
	public final Channel getChannel() {
		return this;
	}
	
	@Override
	public InetAddress getAddress() {
		return remoteAddress().getAddress();
	}
	
	public void closeStream() {
		close();
	}
	
	NettyStream(ServerChannel sc, SocketChannel c) {
		super(sc, c);
		secret = keyGenerator.generateKey();
	}
	
}
