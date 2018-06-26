package com.podts.rpg.server.network.netty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.podts.rpg.server.network.NetworkManager;
import com.podts.rpg.server.network.Packet;
import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.StreamListener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.Future;

public final class NettyNetworkManager extends NetworkManager {

	private ServerBootstrap bootstrap;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private static NettyNetworkManager manager;
	
	protected static final NettyNetworkManager get() {
		return manager;
	}
	
	private final Set<NettyStream> streams, safeStreams;
	
	private final ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ch.closeFuture().addListener(new ChannelFutureListener() {
			    @Override
			    public void operationComplete(ChannelFuture future) throws Exception {
			        NettyNetworkManager.this.closeChannel(future.channel());
			    }
			});
			ch.pipeline().addLast(new ChannelWatcher())
			.addLast(new DefaultFrameEncoder())
			.addLast(new DefaultPacketEncoder())
			.addLast(new DefaultFrameDecoder())
			.addLast(new DefaultPacketDecoder())
			.addLast(new DefaultPacketHandler());
			
			streams.add((NettyStream)ch);
			
			System.out.println("Connected: " + ch.remoteAddress());
		}
	};
	
	private final class DefaultFrameDecoder extends LengthFieldBasedFrameDecoder {
		DefaultFrameDecoder() {
			super(20_000, 0, 4);
		}
	}
	
	private final class DefaultFrameEncoder extends LengthFieldPrepender {
		DefaultFrameEncoder() {
			super(4);
		}
	}
	
	private final void closeChannel(Channel channel) {
		NettyStream stream = (NettyStream) channel;
		stream.getPlayer().getEntity().deRegister();
		streams.remove(stream);
        NettyNetworkManager.this.onPlayerDisconnect(stream);
	}
	
	private final class ChannelWatcher extends ChannelOutboundHandlerAdapter {
		
		private void handleClose(ChannelHandlerContext ctx) {
			closeChannel(ctx.channel());
		}
		
		@Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	    	if(!cause.getMessage().equals("An existing connection was forcibly closed by the remote host")) {
	    		cause.printStackTrace();
	    	}
	    	handleClose(ctx);
	    }
		
	}
	
	protected final void doSetPacketStream(Packet packet, NetworkStream networkStream) {
		setPacketStream(packet, networkStream);
	}
	
	@Override
	protected boolean doBind(String address, int port) {
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
			.channel(NettyStreamServerChannel.class)
			.childHandler(channelInitializer)
			.option(ChannelOption.SO_BACKLOG, 128)
			.childOption(ChannelOption.SO_KEEPALIVE, true);

			// Bind and start to accept incoming connections.
			ChannelFuture f = bootstrap.bind(address, port).sync();
			
			if(f.isSuccess()) {
				manager = this;
			}
			
			return f.isSuccess();

		} catch(Exception e) {
			e.printStackTrace();
			shutdownAndWaitGroups();
		}
		return false;
	}
	
	protected void doUnbind() {
		shutdownAndWaitGroups();
	}
	
	private boolean shutdownAndWaitGroups() {
		Future<?> f1 = workerGroup.shutdownGracefully();
		Future<?> f2 = bossGroup.shutdownGracefully();
		
		try {
			f1.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			f2.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		bootstrap = null;
		workerGroup = null;
		bossGroup = null;
		return f1.isSuccess() && f2.isSuccess();
	}
	
	public Collection<NettyStream> getStreams() {
		return safeStreams;
	}
	
	public NettyNetworkManager() {
		streams = new HashSet<>();
		safeStreams = Collections.unmodifiableSet(streams);
	}

	public NettyNetworkManager(StreamListener listener) {
		super(listener);
		streams = new HashSet<>();
		safeStreams = Collections.unmodifiableSet(streams);
	}

}
