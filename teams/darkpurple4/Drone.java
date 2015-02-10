package darkpurple4;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Drone {
	
	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team myTeam;
	static Team enemyTeam; 
	static boolean waveAttack;
	static RobotInfo target;
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		target = null;
		
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
				RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);

				resupplyFriendlies();
				
				// fight enemies
				if(enemies.length > 0) {
					if(target == null) {
						target = enemies[0];
					}
					Skirmisher.battlecode();
					continue;
				}
				
				if(target != null && rc.canSenseRobot(target.ID)) {
					Mover.moveDrone(myLoc.directionTo(rc.senseRobot(target.ID).location));
				} else {
					target = null;
					Mover.moveDrone(myLoc.directionTo(MessageBoard.getRallyLoc()));
				}
			}
		} catch(Exception e) {
			System.out.println("Drone exception");
			e.printStackTrace(); 
		}
	}

	private static void resupplyFriendlies() throws GameActionException {
		if(rc.getSupplyLevel() < .5*rc.getType().supplyUpkeep * 100) {
			return;
		}
		for(RobotInfo friend : rc.senseNearbyRobots(15, myTeam)) {
			if(friend.supplyLevel < 10 * friend.type.supplyUpkeep) {
				rc.transferSupplies(friend.type.supplyUpkeep * 50, friend.location);
			}
		}
	}
	

}
