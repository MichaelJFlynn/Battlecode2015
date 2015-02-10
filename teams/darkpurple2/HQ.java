package darkpurple2;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class HQ {
	static Team myTeam;
	static Team enemyTeam;
	static RobotController rc;
	static int myrange;
	static Random rand; 
	static int supplyID;
	static int supplyWaitLimit;
	public static void loop(RobotController theRC) {
		rc = theRC;
		supplyID = 0;
		supplyWaitLimit = 0;
		try {
			myTeam = rc.getTeam();
			enemyTeam = rc.getTeam().opponent();
			myrange = rc.getType().attackRadiusSquared;
			rand = new Random(42);
			MessageBoard.init(rc);
			
			while(true) {
				rc.yield();
				if(attackSomething()) {
					continue;
				}
				
				// this means we are not supplying any robot
				if(supplyID == 0 || supplyWaitLimit > 50) { 
					supplyID = MessageBoard.popSupplyQueue();
					supplyWaitLimit++;
				}
				
				if(supplyID != 0 && rc.canSenseRobot(supplyID)) {
					RobotInfo robot = rc.senseRobot(supplyID);
					MapLocation location = robot.location;
					if(location.distanceSquaredTo(rc.getLocation()) <= 15) {
						rc.transferSupplies(robot.type.buildTurns * 200, robot.location);
						supplyID = 0;
					} else {
						supplyWaitLimit++;
					}
				} else {
					supplyWaitLimit++;
				}
				
				MessageBoard.queryBuildOrder();
				
			}
			
			
		} catch (Exception e) {
			System.out.println("HQ Exception");
			e.printStackTrace();
		}
		 
	}
	
	private static boolean attackSomething() throws GameActionException {
		RobotInfo[] enemies = rc.senseNearbyRobots(myrange, enemyTeam);
		if(enemies.length > 0 && rc.isWeaponReady()) { 
			rc.attackLocation(enemies[rand.nextInt(enemies.length)].location);
			return true;
		} else {
			return false;
		}
	}
	

}
