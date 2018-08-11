package com.podts.rpg.server.command;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.podts.rpg.server.network.NetworkStream;
import com.podts.rpg.server.network.packet.MessagePacket;

public class NetworkLogger extends GameLogger {
	
	private final NetworkStream networkStream;
	
	public final NetworkStream getStream() {
		return networkStream;
	}
	
	protected Handler createhandler() {
		return new NetworkHandler();
	}
	
	NetworkLogger(NetworkStream networkStream) {
		super(networkStream.getAddress().toString() + " - Logger");
		this.networkStream = networkStream;
		setUseParentHandlers(false);
	}
	
	private final class NetworkHandler extends Handler {

		@Override
		public final void close() throws SecurityException {
			
		}

		@Override
		public final void flush() {
			
		}

		@Override
		public final void publish(LogRecord record) {
			getStream().sendPacket(new MessagePacket(getFormatter().format(record)));
		}
		
	}
	
}
