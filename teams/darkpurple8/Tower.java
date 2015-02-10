package darkpurple8;

import java.util.Random;

import battlecode.common.*;

public class Tower {
	
	static RobotController rc;
	static int myrange;
	static Team myTeam;
	static Team enemyTeam;
	static Random rand;
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myrange = rc.getType().attackRadiusSquared;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		rand = new Random(42); 
		
		try {
			while(true) {
				rc.yield();
				if(rc.isWeaponReady()) {
					attackSomething();
				}
			}
		} catch (Exception e) {
			System.out.println("Tower exception");
			e.printStackTrace();
		}
		
	}

	private static void attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myrange, enemyTeam);
		if(enemies.length > 0) { 
			rc.attackLocation(enemies[rand.nextInt(enemies.length)].location);
		}
	}
	
	
}
