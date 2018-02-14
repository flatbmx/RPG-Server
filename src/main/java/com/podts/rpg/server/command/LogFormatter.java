package com.podts.rpg.server.command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public final class LogFormatter extends Formatter {
	
	private static final DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	
	@Override
	public final String format(final LogRecord record) {
		return dateFormatter.format(new Date(record.getMillis())) + " [" + record.getLevel() + "]" + " " + record.getMessage();
	}
	
}
