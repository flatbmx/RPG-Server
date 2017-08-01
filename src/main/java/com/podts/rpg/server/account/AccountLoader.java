package com.podts.rpg.server.account;

import com.podts.rpg.server.model.Player;

public abstract class AccountLoader {
	
	public static final int MIN_USERNAME_LENGTH = 4;
	public static final int MAX_USERNAME_LENGTH = 16;
	
	public static final boolean isValidUsername(String username) {
		if(username == null) return false;
		if(username.length() <= MIN_USERNAME_LENGTH || username.length() >= MAX_USERNAME_LENGTH) return false;
		
		for(char c : username.toCharArray()) {
			if(!Character.isLetterOrDigit(c)) return false;
		}
		
		return true;
	}
	
	public boolean accountExists(String username) {
		if(username == null) throw new NullPointerException("Cannot determine if account exists with null name.");
		if(!isValidUsername(username)) return false;
		return doAccountExists(username);
	}
	
	public abstract boolean doAccountExists(String username);
	
	public abstract Player loadAccount(String username, String password)
			throws AccountDoesNotExistException, IncorrectPasswordException;
	
	public abstract boolean createAccount(String username, String password) throws AccountAlreadyExistsException;
	
	public abstract boolean saveAccount(Player player);
	
	public final class AccountAlreadyExistsException extends Exception {
		private static final long serialVersionUID = 6622249245566732955L;
	}
	
	public final class AccountDoesNotExistException extends Exception {
		private static final long serialVersionUID = -2823154578162794923L;
	}
	
	public final class IncorrectPasswordException extends Exception {
		private static final long serialVersionUID = 7210933251204330894L;
	}
	
}
