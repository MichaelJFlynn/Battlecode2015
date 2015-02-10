package darkpurple10;

import battlecode.common.*; 

public class Soldier {

	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team myTeam;
	static Team enemyTeam; 
	static boolean waveAttack;
	static MapLocation harassLocation;
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		waveAttack = false;
		MessageBoard.init(rc);
		Mover.init(rc);
		Skirmisher.init(rc);
		try {
			harassLocation = MessageBoard.getHarassLocation();
		
			if(harassLocation == null) harassLocation = rc.senseEnemyHQLocation();
			MessageBoard.soldierSpawned();
			MessageBoard.requestSupply(rc.getID());
			while(rc.getSupplyLevel() == 0) {
				Mover.goTo(rc.senseHQLocation());
				rc.yield();
			}
			while(true) {
				Skirmisher.setGoalLoc(harassLocation);
				rc.yield();
				towers = rc.senseEnemyTowerLocations();
				myLoc = rc.getLocation();
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);

				resupplyFriendlies();

				// fight enemies
				if(enemies.length > 0) {
					Skirmisher.battlecode();
					continue;
				}

				if(closeToTowers()) {
					if(Skirmisher.tryToAttackTower(towers, myLoc))
						continue;
				}
				if(myLoc.equals(harassLocation)) {
					harassLocation = rc.senseEnemyHQLocation();
				}
				Mover.goTo(harassLocation);
			}
		} catch(Exception e) {
			System.out.println("Soldier exception");
			e.printStackTrace(); 
		}
	}

	private static boolean closeToTowers() {
		for(MapLocation tower : towers) { 
			if(myLoc.distanceSquaredTo(tower) <= 35) {
				return true;
			}
		}
		if(myLoc.distanceSquaredTo(rc.senseEnemyHQLocation()) <= 35) {
			return true; 
		}
		
		return false;
	}

	public static MapLocation findClosest(MapLocation[] locs) {
		MapLocation min = null;
		int min_distance = 99999999;
		for(MapLocation loc : locs) {
			int dist = myLoc.distanceSquaredTo(loc);
			if(dist < min_distance) {
				min = loc;
				min_distance = dist;
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
}
