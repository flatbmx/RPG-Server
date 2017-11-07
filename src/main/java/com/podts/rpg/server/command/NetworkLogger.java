package com.podts.rpg.server.command;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.podts.rpg.server.network.Stream;
import com.podts.rpg.server.network.packet.MessagePacket;

public class NetworkLogger extends GameLogger {
	
	private final Stream stream;
	
	public final Stream getStream() {
		return stream;
	}
	
	protected Handler createhandler() {
		return new NetworkHandler();
	}
	
	NetworkLogger(Stream stream, String bundle) {
		super(stream.getAddress().toString() + " - Logger", bundle);
		this.stream = stream;
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
