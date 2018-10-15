package com.podts.rpg.server.network.netty;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.podts.rpg.server.Player;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.Packet;

import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

class NettyStream extends NioSocketChannel implements NetworkStream {
	
	private static KeyGenerator keyGenerator;
	
	static {
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError("JVM does not support AES!");
		}
	}
	
	private final SecretKey secret;
	protected Player player;
	
	private Instant lastPing;
	private int ping;
	
	public int getPing() {
		return ping;
	}
	
	@Override
	public SecretKey getSecretKey() {
		return secret;
	}
	
	@Override
	public final Player getPlayer() {
		return player;
	}
	
	@Override
	public final void setPlayer(Player player) {
		this.player = player;
	}
	
	@Override
	public void sendPacket(Packet p) {
		writeAndFlush(p);
	}
	
	@Override
	public void sendPacket(Packet... packets) {
		for(Packet p : packets)
			write(p);
		flush();
	}
	
	public final Channel getChannel() {
		return this;
	}
	
	@Override
	public InetAddress getAddress() {
		return remoteAddress().getAddress();
	}
	
	@Override
	public void closeStream() {
		close();
	}
	
	@Override
	public String toString() {
		return "[" + getAddress() + "]";
	}
	
	NettyStream(ServerChannel sc, SocketChannel c) {
		super(sc, c);
		secret = keyGenerator.generateKey();
	}
	
}
