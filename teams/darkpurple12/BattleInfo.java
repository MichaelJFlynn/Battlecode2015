package darkpurple12;

import battlecode.common.*;

public class BattleInfo {
	
	private double POTENTIAL_DAMAGE_RECEIVED;
	private double[] POTENTIAL_DAMAGE_DONE;
	private int POTENTIAL_KILLS;
	private boolean POTENTIAL_DEATH;
	private boolean WINNING;
	private RobotInfo[] friends;
	private RobotInfo[] enemies;
	private Direction moveDirection;
	private boolean towerDamage;
	
	
	public BattleInfo(double recieved, double[] dealt, int kills, boolean death, boolean winning, boolean tdamage,
			RobotInfo[] p_friends, RobotInfo[] p_enemies, Direction move) {
		POTENTIAL_DAMAGE_RECEIVED = recieved;
		POTENTIAL_DAMAGE_DONE = dealt;
		POTENTIAL_KILLS = kills;
		POTENTIAL_DEATH = death;
		WINNING = winning;
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
	
	public boolean winning() {
		return WINNING;
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
