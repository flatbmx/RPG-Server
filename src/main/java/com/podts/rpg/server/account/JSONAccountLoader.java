package com.podts.rpg.server.account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.Gson;
import com.podts.rpg.server.AccountLoader;
import com.podts.rpg.server.Player;
import com.podts.rpg.server.model.entity.EntityFactory;
import com.podts.rpg.server.model.universe.Location;
import com.podts.rpg.server.model.universe.Universe;

public final class JSONAccountLoader extends AccountLoader {
	
	private static final Gson gson = new Gson();
	
	private final File root;
	
	private final File getAccountFile(String username) {
		return new File(root.getPath() + username + ".json");
	}
	
	@Override
	public boolean doAccountExists(String username) {
		return getAccountFile(username).exists();
	}
	
	@Override
	public Player doLoadAccount(String username, String password)
			throws InvalidUsernameException, AccountDoesNotExistException, IncorrectPasswordException {
		
		checkValidUsername(username);
		
		File accountFile = getAccountFile(username);
		
		Reader reader = null;
		try {
			reader = new FileReader(accountFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		GPlayer gp = gson.fromJson(reader, GPlayer.class);
		
		Player player = createPlayer(gp.username, gp.password);
		player.setEntity(EntityFactory.constructPlayerEntity(player, Universe.get().getDefaultWorld().createLocation(gp.x, gp.y, gp.z)));
		
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return player;
		
	}

	@Override
	public Player doCreateAccount(String username, String password) throws AccountAlreadyExistsException {
		//TODO implement me actually.
		return createPlayer(username, password);
	}

	@Override
	public boolean saveAccount(Player player) {
		
		GPlayer gp = new GPlayer(player);
		
		try {
			Writer writer = new FileWriter(getAccountFile(player.getUsername()));
			gson.toJson(gp, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	public JSONAccountLoader(File rootDirectory) {
		root = rootDirectory;
	}
	
	private static class GPlayer {
		String username;
		String password;
		int x, y, z;
		
		GPlayer(Player p) {
			username = p.getUsername();
			password = p.getPassword();
			Location l = p.getEntity().getLocation();
			x = l.getX();
			y = l.getY();
			z = l.getZ();
		}
		
	}
	
}
