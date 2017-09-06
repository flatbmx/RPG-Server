package com.podts.rpg.server.model.universe;

import java.sql.*;

public final class SQLWorldLoader extends WorldLoader {
	
	private static final String TILE_TABLE_NAME = "tiles";
	
	private final String address, username, password, database;
	private final int port;
	
	private Connection connection;
	
	@Override
	public void loadTiles(Tile[][] tiles, Location point) {
		// TODO Auto-generated method stub

	}

	@Override
	public Tile loadTile(Location point) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WorldLoader saveTiles(Tile[][] tiles) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doSaveTile(Tile tile) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected boolean init() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}
		
		Statement st;
		
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port, username, password);
			
			st = connection.createStatement();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		if(!createDatabase()) {
			close();
			return false;
		}
		
		if(!createTileTable()) {
			close();
			return false;
		}
		
		return true;
	}
	
	private void close() {
		try {
			connection.close();
		} catch (SQLException e) {}
	}
	
	private boolean createDatabase() {
		try {
			Statement st = connection.createStatement();
			st.executeUpdate("CREATE DATABASE " + database);	
		} catch (SQLException e) {
			if(e.getErrorCode() != 1007) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	private boolean createTileTable() {
		try {
			Statement st = connection.createStatement();
			
			String createTiletable = "CREATE TABLE IF NOT EXISTS " + TILE_TABLE_NAME
					+ " (type	BYTE,"
					+ " x		INTEGER,"
					+ " y		INTEGER,"
					+ " z		INTEGER)";
			
			st.execute(createTiletable);
			
		} catch (SQLException e) {
			if(e.getErrorCode() != 1050) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public SQLWorldLoader(String address, int port, String username, String password, String database) {
		this.address = address;
		this.port = port;
		this.username = username;
		this.password = password;
		this.database = database;
	}
	
	public static final void main(String[] args) {
		
		SQLWorldLoader loader = new SQLWorldLoader("localhost", 3306, "tester", "ZXEkZf1vk5Hf09K8", "test");
		
		if(!loader.init())
			System.out.println("Failed");
		
	}
	
}
