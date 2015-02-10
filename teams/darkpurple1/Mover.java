package darkpurple1;

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

	private static boolean againstWall() {
		return !rc.senseTerrainTile(rc.getLocation().add(wallDir)).isTraversable();
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
		if(!rc.isCoreReady()) {
			return false;
		}
		if(rc.canMove(preferred)) {
			rc.move(preferred);
			return true;
		} else {
			Direction right = preferred.rotateRight();
			Direction left = preferred.rotateLeft();
			if(rand.nextBoolean()) {
				if(rc.canMove(right)) {
					rc.move(right);
					return true;
				}
				if(rc.canMove(left)) {
					rc.move(left);
					return true;
				}
			} else {
				if(rc.canMove(left)) {
					rc.move(left);
					return true;
				}
				if(rc.canMove(right)){ 
					rc.move(right);
					return true;
				}
			}
			if(rand.nextBoolean()) {
				if(rc.canMove(right.rotateRight())) {
					rc.move(right.rotateRight());
					return true;
				}
				if(rc.canMove(left.rotateLeft())) {
					rc.move(left.rotateLeft());
					return true;
				}
			}
			if(rc.canMove(left.rotateLeft())) {
				rc.move(left.rotateLeft());
				return true;
			}
			if(rc.canMove(right.rotateRight())) {
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
	
	/*/ 
	 * Bug path steps
	 * left:
	 *  Assuming that the wall is to our immediate left
	 *  if we can turn around a corner
	 *    do it, wall dir rotates 90 degrees
	 *  else
	 *    go along wall
	 *  Ending, wall is to our immediate left
	 * 
	 * right:
	 *  Assuming that the wall is to our immediate right
	 *  
	 *  
	 *  Ending, wall is to our immediate right
	*/
}
