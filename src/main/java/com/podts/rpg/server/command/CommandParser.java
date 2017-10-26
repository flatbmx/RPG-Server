package com.podts.rpg.server.command;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	public static void main(String[] args) {
		
		System.out.println(Arrays.asList(extractParameters("msg will2233 \"Hey bro\"")));
		
	}
	
}
