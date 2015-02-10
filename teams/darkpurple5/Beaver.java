package darkpurple5;

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
		goalLoc = enemyLoc; 
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
					continue;
				}

				if(rc.getSupplyLevel() <= 0) {
					requestSupply();
				}
				if(rand.nextBoolean()) {
					Mover.tryToMove(directions[rand.nextInt(8)]);
				} else {
					Mover.tryToMove(myLoc.directionTo(rc.senseHQLocation()));
				}
			} 
		
		} catch (Exception e) {
			System.out.println("Beaver exception");
			e.printStackTrace();
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


