package darkpurple4;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Barracks {

	static RobotController rc;
	static Direction[] spawnDirections;

	public static void loop(RobotController theRC) {
		rc = theRC;
		spawnDirections = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
				Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
				Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		try {
			while(true) {
				rc.yield();
				if(rc.hasSpawnRequirements(RobotType.SOLDIER) && rc.isCoreReady()) {
					trySpawn(RobotType.SOLDIER);
				}
			}
		} catch(Exception e) {
			System.out.println("Barracks error");
			e.printStackTrace(); 
		}
		
	}

	
	private static void trySpawn(RobotType type) throws GameActionException {
		for(Direction d : spawnDirections) {
			if(rc.canSpawn(d, type)){ 
				rc.spawn(d, type);
				break;
			}
		}
	}
}
