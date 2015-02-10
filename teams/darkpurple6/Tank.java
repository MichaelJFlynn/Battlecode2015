package darkpurple6;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Tank {
	public static final double SUPPLY_DRONE_CARRY_LIMIT = 4 * (30 * 200);
	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team myTeam;
	static Team enemyTeam; 
	static boolean wave_attack;
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		wave_attack = false;
		
		MessageBoard.init(rc);
		Mover.init(rc);
		Skirmisher.init(rc);
		try {
			MessageBoard.tankSpawned();
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
				
				if(rc.getSupplyLevel() == 0) {
					MessageBoard.requestRemoteSupply();
				}
				
				resupplyFriendlies();
				
				
				// fight enemies
				if(enemies.length > 0) {
					Skirmisher.battlecode();
					continue;
				}
				
				if(MessageBoard.getTanksSpawned() > MessageBoard.TANKS_PER_WAVE) {
					wave_attack = true;
				}
				
				if(wave_attack) {
					if(towers.length > 0) {
						Mover.goTo(findClosest(towers));
					} else {
						Mover.goTo(rc.senseEnemyHQLocation());
					}
				} else {
					Mover.goTo(findClosestTo(rc.senseTowerLocations(), rc.senseEnemyHQLocation()));
				}
			}
		} catch(Exception e) {
			System.out.println("Drone exception");
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
}
