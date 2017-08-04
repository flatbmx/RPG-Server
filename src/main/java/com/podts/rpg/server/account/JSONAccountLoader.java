package com.podts.rpg.server.account;

import java.io.File;

import com.podts.rpg.server.model.AccountLoader;
import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.PlayerEntity;
import com.podts.rpg.server.model.universe.Universe;

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
			throws InvalidUsernameException, AccountDoesNotExistException, IncorrectPasswordException {
		
		checkValidUsername(username);
		
		File accountFile = getValidAccountFile(username);
		
		Player player = new Player();
		player.setEntity(new PlayerEntity(player, Universe.get().getDefaultWorld().createLocation(0, 0, 0)));
		
		return player;
		
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
