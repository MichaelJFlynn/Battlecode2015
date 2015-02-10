package darkpurple11;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class TrainingField {
	static RobotController rc;
	static final Direction[] spawnDirections = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		MessageBoard.init(rc);
		try {
			while(true) {
				rc.yield();
				// both these methods return a deficit
				if(!rc.hasCommander()) {
					tryToSpawn(RobotType.COMMANDER);
				}
			}
		} catch(Exception e) {
			System.out.println("TrainingField exception");
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
