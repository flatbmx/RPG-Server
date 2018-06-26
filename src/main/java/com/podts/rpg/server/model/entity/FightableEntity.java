package com.podts.rpg.server.model.entity;

import com.podts.rpg.server.model.EntityType;
import com.podts.rpg.server.model.universe.Entity;
import com.podts.rpg.server.model.universe.Location;

public class FightableEntity extends Entity implements Fightable {
	
	private int hp;
	private int maxHp;
	
	public int getHP() {
		return hp;
	}
	
	public FightableEntity setHP(int newHP) {
		newHP = Math.max(0, maxHp);
		hp = Math.min(newHP, maxHp);
		return this;
	}
	
	public int getMaxHP() {
		return maxHp;
	}
	
	public double getHPPercentage() {
		return getHP()/getMaxHP();
	}
	
	public FightableEntity(String name, EntityType type, Location loc) {
		super(name, type, loc);
	}
	
}
