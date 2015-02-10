package team372;

import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Missile {
	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team enemyTeam; 
	static Team myTeam; 
	static final int MAP_BASE_CHANNEL = 51136;

	public static void loop(RobotController theRC) {
		rc = theRC;
		try {
			rc.setIndicatorString(1, "Getting Target");
			MapLocation goal = getMissileDestination();
			rc.setIndicatorString(1, "Target : " + goal);
			myLoc = rc.getLocation();
			myTeam = rc.getTeam();
			enemyTeam = myTeam.opponent();
			tryToMove(myLoc.directionTo(goal));
			rc.yield();
			while(true) {
				myLoc = rc.getLocation();
				RobotInfo[] enemies = rc.senseNearbyRobots(2, enemyTeam);
				RobotInfo[] friends = rc.senseNearbyRobots(2, myTeam); 
				
				if(goal == null) {
					//Mover.tryToMove(myLoc.directionTo(findClosest(towers)));
				} else {
					tryToMove(myLoc.directionTo(goal));
				}
				
				if(enemies.length > 0 && friends.length == 0) {
					for(RobotInfo enemy : enemies) {
						if(enemy.type != RobotType.MISSILE) 
							rc.explode();
					}
				}
				

				rc.yield();
			}
		} catch(Exception e) {
			System.out.println("Missile exception");
			e.printStackTrace(); 
		}
	}
	
	public static MapLocation getMissileDestination() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation hq = rc.senseHQLocation();
		int map_channel = getChannelFromLocation(myLoc);
		int signal = rc.readBroadcast(map_channel);
		if(signal == 0) return null;
		int x = (signal % 1000) + hq.x - 500;
		int y = ((signal / 1000) % 1000) + hq.y - 500;
		MapLocation dest = new MapLocation(x,y);
		return dest;
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
	
	public static int getChannelFromLocation(MapLocation loc) {
		MapLocation hq = rc.senseHQLocation();
		int x = loc.x - hq.x;
		int y = loc.y - hq.y;
		return 120*y + x + MAP_BASE_CHANNEL;
	}
	
	public static boolean tryToMove(Direction preferred) throws GameActionException {
		myLoc = rc.getLocation();
		if(!rc.isCoreReady()) {
			return false;
		}
		if(rc.canMove(preferred)) {
			rc.move(preferred);
			return true;
		} else {
			Direction right = preferred.rotateRight();
			Direction left = preferred.rotateLeft();
			if(rc.canMove(right)) {
				rc.move(right);
				return true;
			}
			if(rc.canMove(left)) {
				rc.move(left);
				return true;
			}


			if(rc.canMove(right.rotateRight())) {
				rc.move(right.rotateRight());
				return true;
			}
			if(rc.canMove(left.rotateLeft())) {
				rc.move(left.rotateLeft());
				return true;
			}
		}
		
		return false;
	}
}
