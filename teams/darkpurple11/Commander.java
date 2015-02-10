package darkpurple11;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Commander {
	public static final double SUPPLY_DRONE_CARRY_LIMIT = 4 * (30 * 200);
	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team myTeam;
	static Team enemyTeam; 
	static boolean wave_attack;
	
	static enum CommanderState { ATTACKING, FLEEING}; 
	
	public static CommanderState myState;
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		wave_attack = false;
		myState = CommanderState.ATTACKING;
		MessageBoard.init(rc);
		Mover.init(rc);
		Skirmisher.init(rc);
		try {
			MessageBoard.requestSupply(rc.getID());
			while(rc.getSupplyLevel() == 0) {
				Mover.goTo(rc.senseHQLocation());
				rc.yield();
			}
			while(true) {
				rc.yield();
				towers = rc.senseEnemyTowerLocations();
				myLoc = rc.getLocation();
				RobotInfo[] enemies = rc.senseNearbyRobots(36, enemyTeam);
				
				if(rc.getSupplyLevel() == 0) {
					MessageBoard.requestRemoteSupply();
				}
				
				resupplyFriendlies();
				
				
				// fight enemies
				if(enemies.length > 0) {
					if(myState == CommanderState.ATTACKING) {
						Skirmisher.battlecode();
						continue;
					}
				}
				rc.setIndicatorString(0, "Mystate: "  + myState);
				rc.setIndicatorString(1, "Rally Loc: " + MessageBoard.getRallyLoc());
				MapLocation rallyLoc = MessageBoard.getRallyLoc();
				switch(myState) {
				case ATTACKING:
					if(rc.getHealth() > RobotType.COMMANDER.maxHealth / 3) {
						Skirmisher.setGoalLoc(rallyLoc);
						Mover.commanderGoTo(rallyLoc);
					} else {
						Skirmisher.setGoalLoc(rc.senseHQLocation());
						Mover.commanderGoTo(rc.senseHQLocation());
						myState = CommanderState.FLEEING;
					}
					break;
				case FLEEING:
					if(rc.getHealth() < RobotType.COMMANDER.maxHealth * .75) {
						Skirmisher.setGoalLoc(rc.senseHQLocation());
						Mover.commanderGoTo(rc.senseHQLocation());
					} else {
						Skirmisher.setGoalLoc(rallyLoc);
						Mover.commanderGoTo(rallyLoc);
						myState = CommanderState.ATTACKING;
					}
					break;
				}
				
			}
		} catch(Exception e) {
			System.out.println("Commander exception");
			e.printStackTrace(); 
		}
	}


	private static void resupplyFriendlies() throws GameActionException {
		if(rc.getSupplyLevel() < rc.getType().supplyUpkeep * 100) {
			return;
		}
		for(RobotInfo friend : rc.senseNearbyRobots(15, myTeam)) {
			if(friend.supplyLevel < 10 * friend.type.supplyUpkeep) {
				rc.transferSupplies(friend.type.supplyUpkeep * 30, friend.location);
			}
		}
	}
	
	public static MapLocation findClosest(MapLocation[] locs) {
		MapLocation min = null;
		int min_distance = 99999999;
		for(MapLocation loc : locs) {
			int dist = myLoc.distanceSquaredTo(loc);
			if(dist < min_distance) {
				min = loc;
				min_distance = myLoc.distanceSquaredTo(loc); 
			}
		}
		return min;
	}
	
	public static MapLocation findClosestTo(MapLocation[] locs, MapLocation m) {
		MapLocation min = null;
		int min_distance = 99999999;
		for(MapLocation loc : locs) {
			int dist = m.distanceSquaredTo(loc);
			if(dist < min_distance) {
				min = loc;
				min_distance = dist;
			}
		}
		return min;
	}
	
	private static boolean closeToTowers() {
		double TSqrange = RobotType.TOWER.attackRadiusSquared;
		double HQSqrange = RobotType.HQ.attackRadiusSquared;
		for(MapLocation tower : towers) { 
			if(myLoc.distanceSquaredTo(tower) <= TSqrange + Math.sqrt(TSqrange)*2 + 1) {
				return true;
			}
		}
		if(myLoc.distanceSquaredTo(rc.senseEnemyHQLocation()) <= HQSqrange + Math.sqrt(HQSqrange)*2 + 1) {
			return true; 
		}
		
		return false;
	}
	
}
