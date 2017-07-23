package com.podts.rpg.server.network.netty;

import java.nio.channels.SocketChannel;
import java.util.List;

import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyStreamServerChannel extends NioServerSocketChannel {
	@Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        SocketChannel ch = javaChannel().accept();

        try {
            if (ch != null) {
                buf.add(new NettyStream(this, ch));
                return 1;
            }
        } catch (Throwable t) {
            try {
                ch.close();
            } catch (Throwable t2) {
                
            }
        }

        return 0;
    }
}
