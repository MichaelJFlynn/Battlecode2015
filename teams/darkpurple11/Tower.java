package darkpurple11;

import java.util.Random;

import battlecode.common.*;

public class Tower {
	
	static RobotController rc;
	static int myrange;
	static Team myTeam;
	static Team enemyTeam;
	static Random rand;
	static RobotInfo[] friends;
	static RobotInfo[] enemies;
	
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myrange = rc.getType().attackRadiusSquared;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		rand = new Random(42); 
		MessageBoard.init(rc);
		try {
			while(true) {
				rc.yield();
				friends = rc.senseNearbyRobots(36, myTeam);
				enemies = rc.senseNearbyRobots(36, enemyTeam);
				
				if(enemies.length > 0 && friends.length == 0) {
					MessageBoard.broadcastRallyLoc(enemies[0].location);
				}
				
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
