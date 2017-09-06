package com.podts.rpg.server.account;

import com.podts.rpg.server.model.Player;
import com.podts.rpg.server.model.entity.EntityFactory;
import com.podts.rpg.server.model.entity.PlayerEntity;
import com.podts.rpg.server.model.universe.Universe;

public class AcceptingAccountLoader extends AccountLoader {

	@Override
	public boolean doAccountExists(String username) {
		return true;
	}

	@Override
	public Player loadAccount(String username, String password)
			throws InvalidUsernameException, AccountDoesNotExistException, IncorrectPasswordException {
		
		//checkValidUsername(username);
		//checkValidPassword(password);
		
		Player player = new Player(username, password);
		player.setEntity(EntityFactory.constructPlayerEntity(player, Universe.get().getDefaultWorld().createLocation(0, 0, 0)));
		
		return player;
	}

	@Override
	public Player createAccount(String username, String password)
			throws InvalidUsernameException, AccountAlreadyExistsException, IncorrectPasswordException {
		//checkValidUsername(username);
		//checkValidPassword(password);
		Player player = new Player(username, password);
		player.setEntity(EntityFactory.constructPlayerEntity(player, Universe.get().getDefaultWorld().createLocation(0, 0, 0)));
		return player;
	}

	@Override
	public boolean saveAccount(Player player) {
		return true;
	}

}
