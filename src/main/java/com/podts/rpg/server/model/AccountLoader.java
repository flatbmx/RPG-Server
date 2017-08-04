package com.podts.rpg.server.model;

public abstract class AccountLoader {
	
	public static final int MIN_USERNAME_LENGTH = 4;
	public static final int MAX_USERNAME_LENGTH = 16;
	
	public final boolean isValidUsername(String username) {
		try {
			checkValidUsername(username);
		} catch(InvalidUsernameException e) {
			return false;
		}
		return true;
	}
	
	protected final void checkValidUsername(String username) throws InvalidUsernameException {
		if(username == null) throw new InvalidUsernameException("Username cannot be null.");
		if(username.length() < MIN_USERNAME_LENGTH) throw new InvalidUsernameException("Username has to be at least " + MIN_USERNAME_LENGTH + " characters long.");
		if(username.length() > MAX_USERNAME_LENGTH) throw new InvalidUsernameException("Username cannot be longer than " + MAX_USERNAME_LENGTH + " characters long.");
		for(char c : username.toCharArray()) {
			if(!Character.isLetterOrDigit(c)) throw new InvalidUsernameException("Username can not contain special characters.");
		}
	}
	
	public boolean accountExists(String username) {
		if(username == null) throw new NullPointerException("Cannot determine if account exists with null name.");
		if(!isValidUsername(username)) return false;
		return doAccountExists(username);
	}
	
	public abstract boolean doAccountExists(String username);
	
	public abstract Player loadAccount(String username, String password)
			throws InvalidUsernameException, AccountDoesNotExistException, IncorrectPasswordException;
	
	public abstract boolean createAccount(String username, String password) throws InvalidUsernameException, AccountAlreadyExistsException;
	
	public abstract boolean saveAccount(Player player);
	
	public final class InvalidUsernameException extends Exception {
		private static final long serialVersionUID = 3860240642463628337L;
		protected InvalidUsernameException() {super("Invalid username");}
		protected InvalidUsernameException(String message) {
			super(message);
		}
	}
	
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
