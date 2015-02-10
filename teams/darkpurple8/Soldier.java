package darkpurple8;

import battlecode.common.*; 

public class Soldier {

	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team myTeam;
	static Team enemyTeam; 
	static boolean waveAttack;
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		waveAttack = false;
		MessageBoard.init(rc);
		Mover.init(rc);
		Skirmisher.init(rc);
		try {
			MessageBoard.soldierSpawned();
			MessageBoard.requestSupply(rc.getID());
			while(rc.getSupplyLevel() == 0) {
				Mover.goTo(rc.senseHQLocation());
				rc.yield();
			}
			while(true) {
				rc.yield();
				towers = rc.senseEnemyTowerLocations();
				myLoc = rc.getLocation();
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);

				resupplyFriendlies();

				// fight enemies
				if(enemies.length > 0 || closeToTowers()) {
					Skirmisher.battlecode();
					continue;
				}

				
				if(MessageBoard.spawnedSoldiersInMyWave() >= MessageBoard.SOLDIERS_PER_WAVE) {
					waveAttack = true;
				}
				
				if(waveAttack) {
					if(towers.length > 0) {
						Mover.goTo(findClosest(towers));
					} else {
						Mover.goTo(rc.senseEnemyHQLocation());
					}
				} else {
					Mover.goTo(MessageBoard.getRallyLoc());
				}
			}
		} catch(Exception e) {
			System.out.println("Soldier exception");
			e.printStackTrace(); 
		}
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
