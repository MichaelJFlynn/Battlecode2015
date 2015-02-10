package darkpurple12;

import java.util.*;

import battlecode.common.*;

public class Beaver {
	
	static RobotController rc; 
	static Direction[] directions;
	static Team myTeam;
	static Team enemyTeam;
	static MapLocation goalLoc;
	static MapLocation myLoc;
	static RobotInfo[] enemies;
	static RobotInfo[] adjacents;
	static Random rand;
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
				Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
				Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		goalLoc = null; 
		rand = new Random();
		Mover.init(rc);
		Skirmisher.init(rc);
		MessageBoard.init(rc);	
		try {
			requestSupply();
			while(true) {
				rc.yield();
				myLoc = rc.getLocation();
				enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
				adjacents = rc.senseNearbyRobots(1, myTeam);
				if(enemies.length > 0) {
					Skirmisher.battlecode();
					continue;
				}
				
				if(MessageBoard.queryBuildOrder()) {
					goalLoc = null;
					continue;
				}

				if(rc.getSupplyLevel() <= 0) {
					requestSupply();
				}
				rc.setIndicatorString(2, "Goal Loc: " + goalLoc);
				/*if(rand.nextBoolean() && rand.nextBoolean()) {
					Mover.tryToMove(myLoc.directionTo(rc.senseHQLocation()));
				} else {
					Mover.tryToMove(directions[rand.nextInt(8)]);
				}*/
				goToNextBuildLocation();
			} 
		
		} catch (Exception e) {
			System.out.println("Beaver exception");
			e.printStackTrace();
		}
		
	}

	private static void goToNextBuildLocation() throws GameActionException {
		if(goalLoc == null) {
			int minDist = 9999999;
			int minBuildingBlocks = 4;
			MapLocation hqLoc = rc.senseHQLocation();
			MapLocation[] locs = MapLocation.getAllMapLocationsWithinRadiusSq(myLoc, 15);
			for(MapLocation loc : locs) {
				int dist = loc.distanceSquaredTo(hqLoc);
				int buildingBlocks = MessageBoard.buildingBlocks(loc);
				RobotInfo rob = rc.senseRobotAtLocation(loc);
				if(dist < minDist && buildingBlocks <= minBuildingBlocks && rc.senseTerrainTile(loc).isTraversable() &&
						(rob == null)) {
					goalLoc = loc;
					minDist = dist;
					minBuildingBlocks = buildingBlocks;
				}
			}
			if(goalLoc == null) {
				Mover.tryToMove(myLoc.directionTo(hqLoc).opposite());
				return;
			}
		}
		if(rand.nextBoolean() || myLoc.equals(goalLoc)) {
			Mover.tryToMove(directions[rand.nextInt(directions.length)]);
		} else {
			Mover.goTo(goalLoc);
		}
		
		
	}

	private static void requestSupply() throws GameActionException {
		MessageBoard.requestSupply(rc.getID());
		while(rc.getSupplyLevel() == 0) {
			Mover.goTo(rc.senseHQLocation());
			rc.yield();
		}
	}
	

}


