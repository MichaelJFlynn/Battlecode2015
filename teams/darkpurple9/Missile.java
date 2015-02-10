package darkpurple9;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Missile {
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

		MessageBoard.init(rc);
		Mover.init(rc);
		Skirmisher.init(rc);
		try {
			MapLocation goal = MessageBoard.getMissileDestination();
			while(true) {
				myLoc = rc.getLocation();
				RobotInfo[] enemies = rc.senseNearbyRobots(35, enemyTeam);
				towers = rc.senseEnemyTowerLocations();
				
				if(goal == null) {
					Mover.tryToMove(myLoc.directionTo(findClosest(towers)));
				} else {
					Mover.tryToMove(myLoc.directionTo(goal));
				}
				
				if(enemies.length > 0) {
					for(RobotInfo enemy : enemies) {
						if(enemy.location.distanceSquaredTo(myLoc) < RobotType.MISSILE.attackRadiusSquared
								&& enemy.type != RobotType.MISSILE) {
							if(rc.senseNearbyRobots(2, myTeam).length == 0) {
								rc.explode();
							}
						}
					}
				}
				

				rc.yield();
			}
		} catch(Exception e) {
			System.out.println("Missile exception");
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
}
