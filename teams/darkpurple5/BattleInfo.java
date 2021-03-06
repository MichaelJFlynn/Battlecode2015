package darkpurple5;

import battlecode.common.*;

public class BattleInfo {
	
	private double POTENTIAL_DAMAGE_RECEIVED;
	private double[] POTENTIAL_DAMAGE_DONE;
	private int POTENTIAL_KILLS;
	private boolean POTENTIAL_DEATH;
	private boolean PASSABLE_TERRAIN;
	private RobotInfo[] friends;
	private RobotInfo[] enemies;
	private Direction moveDirection;
	private boolean towerDamage;
	
	
	public BattleInfo(double recieved, double[] dealt, int kills, boolean death, boolean terr, boolean tdamage,
			RobotInfo[] p_friends, RobotInfo[] p_enemies, Direction move) {
		POTENTIAL_DAMAGE_RECEIVED = recieved;
		POTENTIAL_DAMAGE_DONE = dealt;
		POTENTIAL_KILLS = kills;
		POTENTIAL_DEATH = death;
		PASSABLE_TERRAIN = terr;
		friends = p_friends;
		enemies = p_enemies;
		moveDirection = move;
		towerDamage = tdamage;
	}
	
	public double POTENTIAL_DAMAGE_RECEIVED() {
		return POTENTIAL_DAMAGE_RECEIVED;
	}
	
	public double[] POTENTIAL_DAMAGE_DONE() {
		return POTENTIAL_DAMAGE_DONE;
	}
	
	public int POTENTIAL_KILLS() {
		return POTENTIAL_KILLS;
	}
	
	public boolean POTENTIAL_DEATH() {
		return POTENTIAL_DEATH;
	}
	
	public boolean PASSABLE_TERRAIN() {
		return PASSABLE_TERRAIN;
	}
	
	public RobotInfo[] friends() {
		return friends;
	}

	public RobotInfo[] enemies() {
		return enemies;
	}
	
	public Direction moveDirection() {
		return moveDirection;
	}
	
	public boolean towerDamage() {
		return towerDamage;
	}
}
