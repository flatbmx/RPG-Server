package com.podts.rpg.server.account;

import com.podts.rpg.server.model.Player;

public interface AccountLoader {
	
	public boolean accountExists(String username);
	
	public Player loadAccount(String username, String password)
			throws AccountDoesNotExistException, IncorrectPasswordException;
	
	public boolean createAccount(String username, String password) throws AccountAlreadyExistsException;
	
	public boolean saveAccount(Player player);
	
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
