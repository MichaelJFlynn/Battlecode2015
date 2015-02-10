package darkpurple;

import battlecode.common.*;

public class AerospaceLab {
	static RobotController rc;
	static final Direction[] spawnDirections = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		try {
			while(true) {
				rc.yield();
				tryToSpawn(RobotType.LAUNCHER);			
			}
		} catch(Exception e) {
			System.out.println("Aerospacelab exception");
			e.printStackTrace();
		}		
	}

	
	private static void tryToSpawn(RobotType type) throws GameActionException {
		if(!rc.isCoreReady()) return;
		for(Direction d : spawnDirections) {
			if(rc.canSpawn(d, type)){ 
				rc.spawn(d, type);
				break;
			}
		}
	}
}
