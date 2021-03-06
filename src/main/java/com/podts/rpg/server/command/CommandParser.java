package com.podts.rpg.server.command;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CommandParser {
	
	static final String[] extractParameters(String commandText) {
		
		List<String> list = new ArrayList<String>();
		Matcher m = Pattern.compile("\"[^\"]+\"|[\\w]+").matcher(commandText);
		
		while(m.find()) {
			String value = m.group();
			if(value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);
			list.add(value);
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	static final CommandEntry parse(String commandText) {
		
		if(commandText == null) return null;
		commandText = commandText.substring(1);
		if(commandText.trim().isEmpty()) return null;
		
		List<String> list = new ArrayList<String>();
		Matcher m = Pattern.compile("\"[^\"]+\"|[\\w]+").matcher(commandText);
		
		while(m.find()) {
			String value = m.group();
			if(value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length()-1);
			list.add(value);
		}
		
		CommandEntry result = new CommandEntry();
		result.fullEntry = commandText;
		result.name = list.get(0);
		list.remove(result.name);
		result.parameters = list.toArray(new String[list.size()]);
		
		return result;
	}
	
}
