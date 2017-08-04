package com.podts.rpg.server.account;

import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.PlayerEntity;
import com.podts.rpg.server.model.universe.Universe;

public class AcceptingAccountLoader extends AccountLoader {

	@Override
	public boolean doAccountExists(String username) {
		return false;
	}

	@Override
	public Player loadAccount(String username, String password)
			throws InvalidUsernameException, AccountDoesNotExistException, IncorrectPasswordException {
		
		checkValidUsername(username);
		
		Player player = new Player();
		player.setEntity(new PlayerEntity(player, Universe.get().getDefaultWorld().createLocation(0, 0, 0)));
		
		return player;
	}

	@Override
	public boolean createAccount(String username, String password)
			throws InvalidUsernameException, AccountAlreadyExistsException {
		return false;
	}

	@Override
	public boolean saveAccount(Player player) {
		return false;
	}

}
