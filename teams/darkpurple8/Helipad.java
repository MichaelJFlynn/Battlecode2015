package darkpurple8;

import battlecode.common.*;

public class Helipad {

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
				if(MessageBoard.checkSupplyDrones() > 0)
					tryToSpawn(RobotType.DRONE);			
			}
		} catch(Exception e) {
			System.out.println("MinerFactory exception");
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
