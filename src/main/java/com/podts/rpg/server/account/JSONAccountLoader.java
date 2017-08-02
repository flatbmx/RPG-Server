package com.podts.rpg.server.account;

import java.io.File;

import com.podts.rpg.server.model.Player;

public final class JSONAccountLoader extends AccountLoader {
	
	private final File root;
	
	private final File getAccountFile(String username) {
		return new File(root.getPath() + username + ".json");
	}
	
	private final File getValidAccountFile(String username) {
		return getAccountFile(username);
	}
	
	@Override
	public boolean doAccountExists(String username) {
		return getValidAccountFile(username).exists();
	}
	
	@Override
	public Player loadAccount(String username, String password)
			throws AccountDoesNotExistException, IncorrectPasswordException {
		
		File accountFile = getValidAccountFile(username);
		
		
		
		return new Player();
		
	}

	@Override
	public boolean createAccount(String username, String password) throws AccountAlreadyExistsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveAccount(Player player) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public JSONAccountLoader(File rootDirectory) {
		root = rootDirectory;
	}
	
}
