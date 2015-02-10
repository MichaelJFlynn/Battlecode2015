package darkpurple12;

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
			MessageBoard.broadcastRallyLoc(findClosestTo(rc.senseTowerLocations(), rc.senseEnemyHQLocation()));
			
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
						int distanceSquaredTo = location.distanceSquaredTo(rc.getLocation());
						if(distanceSquaredTo <= 15) {
							if(robot.type == RobotType.COMMANDER) {
								rc.transferSupplies((int) rc.getSupplyLevel(), robot.location);
							} else {
								rc.transferSupplies(robot.type.buildTurns * 300, robot.location);
							}
							supplyID = 0;
						} else {
							if(distanceSquaredTo < 36) {
								MessageBoard.requestSupply(supplyID);
							}
							supplyID = 0;
						}
					} else {
						supplyID = 0;
					}
				}
				
				MessageBoard.queryBuildOrder();
				
				if(Clock.getRoundNum() > 300) {
					int deficit = MessageBoard.checkSupplyDrones();
					MessageBoard.resupplySupplyDrones(deficit);
					MessageBoard.cleanUpRemoteSupplyQueue();
				}
				
				//rc.setIndicatorString(0, "Doing tower proxy computations");
				//rc.setIndicatorString(1, "Tower Queue size: " + towerProxQueue.size());
				while(towerProxQueue.size() > 0 && Clock.getBytecodeNum() < 9500) {
					MapLocation loc = towerProxQueue.poll();
					if(MessageBoard.recordTowerTile(loc, towers)){
						for(Direction dir : directions) {
							if(! MessageBoard.towerTileRecorded(loc.add(dir))) {
								towerProxQueue.add(loc.add(dir));
								MessageBoard.visitTowerTile(loc.add(dir));
							}
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
	
	public static MapLocation findClosestTo(MapLocation[] locs, MapLocation m) {
		MapLocation min = null;
		int min_distance = 99999999;
		for(MapLocation loc : locs) {
			int dist = m.distanceSquaredTo(loc);
			if(dist < min_distance) {
				min = loc;
				min_distance = dist;
			}
		}
		return min;
	}

}
