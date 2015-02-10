package darkpurple3;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Launcher {
	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team myTeam;
	static Team enemyTeam; 
	static boolean waveAttack;
	static RobotInfo[] enemies;
	static final Direction[] directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		waveAttack = false;
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
				enemies = rc.senseNearbyRobots(35, enemyTeam);

				// fight enemies
				if(enemies.length > 0 || closeToTowers()) {
					launchMissiles();
					continue;
				}
				
				
				if(towers.length > 0) {
				    	Mover.goTo(findClosest(towers));
				} else {
					Mover.goTo(rc.senseEnemyHQLocation());
				}
			}
		} catch(Exception e) {
			System.out.println("Launcher exception");
			e.printStackTrace(); 
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
	
	public static void launchMissiles() throws GameActionException {
		int minDistance = 99999;
		MapLocation loc;
		myLoc = rc.getLocation();
		if(enemies.length > 0) { 
			loc = enemies[0].location;
			for(RobotInfo enemy : enemies) {
				int dist = myLoc.distanceSquaredTo(enemy.location);
				if(dist < minDistance) {
					minDistance = dist; 
					loc = enemy.location;
				}
			}
		} else {
			loc = findClosest(towers);
		}
		Direction dir = myLoc.directionTo(loc);
		if(rc.canLaunch(dir) && rc.isPathable(RobotType.MISSILE, myLoc.add(dir))) {
			MessageBoard.broadcastCoordinatesToLocationChannel(myLoc.add(dir), loc);
			rc.launchMissile(dir);
			return;
		}
		
		Direction dirleft = dir.rotateLeft(); 
		if(rc.canLaunch(dirleft)  && rc.isPathable(RobotType.MISSILE, myLoc.add(dirleft))) {
			MessageBoard.broadcastCoordinatesToLocationChannel(myLoc.add(dirleft), loc);
			rc.launchMissile(dirleft);
			return;
		}
		
		Direction dirRight = dir.rotateRight();
		if(rc.canLaunch(dirRight) && rc.isPathable(RobotType.MISSILE, myLoc.add(dirRight))) {
			MessageBoard.broadcastCoordinatesToLocationChannel(myLoc.add(dirRight), loc);
			rc.launchMissile(dirRight);
			return;
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
}
