package darkpurple7;

import battlecode.common.*;

import java.util.*;

public class Mover {

	private static RobotController rc;
	private static Direction[] directions;
	private static enum BugPathState {HUGRIGHT, HUGLEFT, NONE};
	private static Random rand;
	private static MapLocation myLoc;
	private static BugPathState bugPathState;
	private static Direction wallDir;
	private static MapLocation[] towers;
	
	public static void init(RobotController theRC) {
		rc = theRC;
		rand = new Random();
		directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
				Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
				Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		myLoc = rc.getLocation();
		bugPathState = BugPathState.NONE;
		wallDir = null;
	}
	
	public static void goTo(MapLocation loc) throws GameActionException {
		myLoc = rc.getLocation();
		towers = rc.senseEnemyTowerLocations();
		bugPath(loc);
	}
	
	public static void bugPath(MapLocation loc) throws GameActionException {
		Direction d = myLoc.directionTo(loc);
		MapLocation m = myLoc.add(d);
		
		
		switch(bugPathState) {
		case NONE:
			//rc.setIndicatorString(0, "Direct");
			if(rc.senseTerrainTile(m).isTraversable()) {
				tryToMove(d);
			} else {
				startBugPath(loc);
			}
			break;
		case HUGRIGHT:
			// check if we can escape
			//rc.setIndicatorString(0, "HugRight");
			if(canEndBugPath(d)) {
				bugPathState = BugPathState.NONE;
			} else {
				Direction movingDirection = wallDir;
				
				while(!rc.senseTerrainTile(myLoc.add(movingDirection)).isTraversable()) {
					movingDirection = movingDirection.rotateLeft();
				}
				
				if(rc.isCoreReady()) {
					if(rc.canMove(movingDirection)) {
						rc.move(movingDirection);
						
						if(movingDirection.isDiagonal()) {
							wallDir = movingDirection.rotateRight().rotateRight().rotateRight();
						} else {
							wallDir = movingDirection.rotateRight().rotateRight();
						}
						
					} else {
						bugPathState = BugPathState.HUGLEFT;
					}
				} 
			}
			//rc.setIndicatorString(2, wallDir.toString());
			break;
		case HUGLEFT:
			//rc.setIndicatorString(0, "HugLeft");
			// check if we can escape
			if(canEndBugPath(d)) {
				bugPathState = BugPathState.NONE;
			} else {
				Direction movingDirection = wallDir;
			
				while(!rc.senseTerrainTile(myLoc.add(movingDirection)).isTraversable()) {
					movingDirection = movingDirection.rotateRight();
				}
				
				if(rc.isCoreReady()) {
					if(rc.canMove(movingDirection)) {
						rc.move(movingDirection);
						if(movingDirection.isDiagonal()) {
							wallDir = movingDirection.rotateLeft().rotateLeft().rotateLeft();
						} else { 
							wallDir = movingDirection.rotateLeft().rotateLeft();
						}
					} else {
						bugPathState = BugPathState.HUGRIGHT;
					}
				}
			}
			//rc.setIndicatorString(2, wallDir.toString());
			break;
		}
	}
	
	private static boolean canEndBugPath(Direction dir) {
		if(!rc.senseTerrainTile(rc.getLocation().add(dir)).isTraversable()) {
			return false;
		}
		Direction movingDirection = wallDir;
		switch(bugPathState) {
		case NONE:
			return true;
		case HUGLEFT:
			return isFrontMove(movingDirection.rotateRight().rotateRight(), dir);
		case HUGRIGHT:
			return isFrontMove(movingDirection.rotateLeft().rotateLeft(), dir);
		}
		return false;
	}

	private static boolean isFrontMove(Direction moving, Direction dir) {
		if(moving.ordinal() == dir.ordinal()) {
			return true;
		}
		if(moving.rotateLeft().ordinal() == dir.ordinal()) {
			return true;
		}
		if(moving.rotateLeft().rotateLeft().ordinal() == dir.ordinal()) {
			return true;
		}
		if(moving.rotateRight().ordinal() == dir.ordinal()) {
			return true;
		}
		if(moving.rotateRight().rotateRight().ordinal() == dir.ordinal()) {
			return true;
		}
		return false;
	}

	private static void startBugPath(MapLocation loc) throws GameActionException {
		Direction d = myLoc.directionTo(loc);
		
		
		if(d.isDiagonal()) {
			
		}
		
		Direction hugLeftDir = d.rotateRight();
		Direction hugRightDir = d.rotateLeft(); 
		
		while(!rc.isCoreReady()) 
			rc.yield();
		
		while(!rc.senseTerrainTile(myLoc.add(hugLeftDir)).isTraversable()) {
			hugLeftDir = hugLeftDir.rotateRight();
		}
		while(!rc.senseTerrainTile(myLoc.add(hugRightDir)).isTraversable()) {
			hugRightDir = hugRightDir.rotateLeft();
		}
		
		if(loc.distanceSquaredTo(myLoc.add(hugRightDir)) > loc.distanceSquaredTo(myLoc.add(hugLeftDir))) {
			// we greedily chose the closest move
			bugPathState = BugPathState.HUGLEFT;			
			if(rc.canMove(hugLeftDir)) {
				rc.move(hugLeftDir);
				// wall direction is different for diagonal moves
				if(hugLeftDir.isDiagonal()) {
					wallDir = hugLeftDir.rotateLeft().rotateLeft().rotateLeft();
				} else {
					wallDir = hugLeftDir.rotateLeft().rotateLeft();
				}
			} else {
				wallDir = d;
				bugPathState = BugPathState.HUGRIGHT;
			}
				
		} else {
			bugPathState = BugPathState.HUGRIGHT;
			if(rc.canMove(hugRightDir)) {
				rc.move(hugRightDir);
				if(hugRightDir.isDiagonal()) {
					wallDir = hugRightDir.rotateRight().rotateRight().rotateRight();
				} else {
					wallDir = hugRightDir.rotateRight().rotateRight();
				}
			} else {
				wallDir = d;
				bugPathState = BugPathState.HUGLEFT;
				}
		}
		
		
	}

	public static boolean safeToGoAround() {
		return true;
	}
	
	// returns true or false depending on if something is in the way
	public static boolean tryToMove(Direction preferred) throws GameActionException {
		myLoc = rc.getLocation();
		if(!rc.isCoreReady()) {
			return false;
		}
		if(rc.canMove(preferred) && rc.isPathable(rc.getType(), myLoc.add(preferred) )) {
			rc.move(preferred);
			return true;
		} else {
			Direction right = preferred.rotateRight();
			Direction left = preferred.rotateLeft();
			if(rand.nextBoolean()) {
				if(rc.canMove(right) && rc.isPathable(rc.getType(), myLoc.add(right))) {
					rc.move(right);
					return true;
				}
				if(rc.canMove(left) && rc.isPathable(rc.getType(), myLoc.add(left))) {
					rc.move(left);
					return true;
				}
			} else {
				if(rc.canMove(left)  && rc.isPathable(rc.getType(), myLoc.add(left))) {
					rc.move(left);
					return true;
				}
				if(rc.canMove(right)&& rc.isPathable(rc.getType(), myLoc.add(right))){ 
					rc.move(right);
					return true;
				}
			}
			if(rand.nextBoolean()) {
				if(rc.canMove(right.rotateRight())&& rc.isPathable(rc.getType(), myLoc.add(right.rotateRight()))) {
					rc.move(right.rotateRight());
					return true;
				}
				if(rc.canMove(left.rotateLeft()) && rc.isPathable(rc.getType(), myLoc.add(left.rotateLeft()))) {
					rc.move(left.rotateLeft());
					return true;
				}
			}
			if(rc.canMove(left.rotateLeft())&& rc.isPathable(rc.getType(), myLoc.add(left.rotateLeft()))) {
				rc.move(left.rotateLeft());
				return true;
			}
			if(rc.canMove(right.rotateRight()) && rc.isPathable(rc.getType(), myLoc.add(right.rotateRight()))) {
				rc.move(right.rotateRight());
				return true;
			}
		}
		
		return false;
	}
	
	
	static int directionToInt(Direction d) {
		switch(d) {
			case NORTH:
				return 0;
			case NORTH_EAST:
				return 1;
			case EAST:
				return 2;
			case SOUTH_EAST:
				return 3;
			case SOUTH:
				return 4;
			case SOUTH_WEST:
				return 5;
			case WEST:
				return 6;
			case NORTH_WEST:
				return 7;
			default:
				return -1;
		}
	}

	public static void goToClosestOre() throws GameActionException {
		myLoc = rc.getLocation();
		MapLocation[] potential = MapLocation.getAllMapLocationsWithinRadiusSq(myLoc, rc.getType().sensorRadiusSquared);
		
		MapLocation oreLoc = null;
		double maxore = 0;
		for(MapLocation m : potential) {
			if(rc.canSenseLocation(m)) {
				double ore = rc.senseOre(m);
				if(ore > maxore) { 
					oreLoc = m;
					maxore = ore;
				}
			}
		}
		
		if(!(oreLoc == null)) {
			goTo(oreLoc);
		} else {
			// there is no local ore ahhhh
			goTo(maximumDistanceFromHQ(potential));
		}
	
	}
	
	public static MapLocation maximumDistanceFromHQ(MapLocation[] locs) {
		MapLocation furthest = locs[0];
		MapLocation hq = rc.senseHQLocation();
		int distance = furthest.distanceSquaredTo(hq);
		
		for(MapLocation loc : locs) {
			int newdist = loc.distanceSquaredTo(hq);
			if(newdist > distance) {
				furthest = loc;
				distance = newdist;
			}
		}
		return furthest;
	}
	
	public static boolean blocked(MapLocation m) throws GameActionException { 
		myLoc = rc.getLocation();
		MapLocation current = myLoc.add(myLoc.directionTo(m));
		while(! current.equals(m))  {
			if(rc.isLocationOccupied(current)) {
				return true;
			}
			current = current.add(current.directionTo(m));
		}
		
		return false;
	}
	
	private static boolean closeToTowers(MapLocation m) {
		double TSqrange = RobotType.TOWER.attackRadiusSquared;
		double HQSqrange = RobotType.HQ.attackRadiusSquared;
		towers = rc.senseEnemyTowerLocations();
		for(MapLocation tower : towers) { 
			if(m.distanceSquaredTo(tower) <= TSqrange + Math.sqrt(TSqrange)*2 + 1) {
				return true;
			}
		}
		if(m.distanceSquaredTo(rc.senseEnemyHQLocation()) <= HQSqrange + Math.sqrt(HQSqrange)*4 + 2) {
			return true; 
		}
		
		return false;
	}
	
	private static boolean safeMove(MapLocation m) {
		return !closeToTowers(m) && rc.isPathable(rc.getType(), m);
	}
	
	public static boolean moveDrone(Direction preferred) throws GameActionException {
		myLoc = rc.getLocation();
		if(!rc.isCoreReady()) {
			return false;
		}
		if(rc.canMove(preferred) && safeMove(myLoc.add(preferred))) {
			rc.move(preferred);
			return true;
		} else {
			Direction right = preferred.rotateRight();
			Direction left = preferred.rotateLeft();
			if(rand.nextBoolean()) {
				if(rc.canMove(right) && safeMove(myLoc.add(right))) {
					rc.move(right);
					return true;
				}
				if(rc.canMove(left) && safeMove(myLoc.add(left))) {
					rc.move(left);
					return true;
				}
			} else {
				if(rc.canMove(left)  && safeMove(myLoc.add(left))) {
					rc.move(left);
					return true;
				}
				if(rc.canMove(right) && safeMove(myLoc.add(right))){ 
					rc.move(right);
					return true;
				}
			}
			right = right.rotateRight();
			left = left.rotateLeft();
			if(rand.nextBoolean()) {
				if(rc.canMove(right) && safeMove(myLoc.add(right))) {
					rc.move(right);
					return true;
				}
				if(rc.canMove(left) && safeMove(myLoc.add(left))) {
					rc.move(left);
					return true;
				}
			} else {
				if(rc.canMove(left)  && safeMove(myLoc.add(left))) {
					rc.move(left);
					return true;
				}
				if(rc.canMove(right) && safeMove(myLoc.add(right))){ 
					rc.move(right);
					return true;
				}
			}
		}
		
		return false;
	}
}
