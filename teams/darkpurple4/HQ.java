package darkpurple4;

import java.util.AbstractQueue;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import battlecode.common.*;

public class HQ {
	static Team myTeam;
	static Team enemyTeam;
	static RobotController rc;
	static int myrange;
	static Random rand; 
	static int supplyID;
	static Queue<MapLocation> towerProxQueue;
	static final Direction[] directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static MapLocation[] towers;
	
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		supplyID = 0;
		try {
			myTeam = rc.getTeam();
			enemyTeam = rc.getTeam().opponent();
			myrange = rc.getType().attackRadiusSquared;
			rand = new Random(42);
			MessageBoard.init(rc);
			towerProxQueue = new LinkedList<MapLocation>(); 
			towers = rc.senseEnemyTowerLocations();
			for(MapLocation tower : towers) {
				towerProxQueue.add(tower);
			}
			
			while(true) {
				rc.yield();
				if(attackSomething()) {
					continue;
				}
				
				
				
				// this means we are not supplying any robot
				rc.setIndicatorString(0, "Supplying: " + supplyID);
				if(supplyID == 0) { 
					supplyID = MessageBoard.popSupplyQueue();
				} else {
					if(rc.canSenseRobot(supplyID)) {
						RobotInfo robot = rc.senseRobot(supplyID);
						MapLocation location = robot.location;
						if(location.distanceSquaredTo(rc.getLocation()) <= 15) {
							rc.transferSupplies(robot.type.buildTurns * 200, robot.location);
							supplyID = 0;
						} else {
							rc.setIndicatorString(1, "Not in range, putting " + supplyID + " back in queue");
							MessageBoard.requestSupply(supplyID);
							supplyID = 0;
						}
					} else {
						rc.setIndicatorString(1, "Could not sense, putting " + supplyID + " back in queue");
						MessageBoard.requestSupply(supplyID);
						supplyID = 0;
					}
				}
				
				
				MessageBoard.queryBuildOrder();
				
				while(towerProxQueue.size() > 0 && Clock.getBytecodeNum() < 9500) {
					MapLocation loc = towerProxQueue.poll();
					MessageBoard.recordTowerTile(loc, towers);
					for(Direction dir : directions) {
						if(! MessageBoard.towerTileRecorded(loc.add(dir))) {
							towerProxQueue.add(loc.add(dir));
						}
					}
					
				}
				
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
