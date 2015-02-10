package darkpurple1;

import battlecode.common.*;

import java.util.*;

public class HQ {
	static Team myTeam;
	static Team enemyTeam;
	static Direction[] spawnDirections;
	static RobotController rc;
	static int myrange;
	static Random rand; 
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		try {
			myTeam = rc.getTeam();
			enemyTeam = rc.getTeam().opponent();
			spawnDirections = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
					Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
					Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
			myrange = rc.getType().attackRadiusSquared;
			rand = new Random(42);
			rc.yield();
			
			while(true) {
				if(rc.isWeaponReady()) {
					attackSomething();
				}
				
				if(rc.isCoreReady() && rc.getTeamOre() >= 100) {
					trySpawn(RobotType.BEAVER);
				}
			}
			
			
		} catch (Exception e) {
			System.out.println("HQ Exception");
			e.printStackTrace();
		}
		 
	}
	
	private static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myrange, enemyTeam);
		if(enemies.length > 0) { 
			rc.attackLocation(enemies[rand.nextInt(enemies.length)].location);
		}
	}
	
	private static void trySpawn(RobotType type) throws GameActionException {
		for(Direction d : spawnDirections) {
			if(rc.canSpawn(d, type)){ 
				rc.spawn(d, type);
				break;
			}
		}
	}
}
