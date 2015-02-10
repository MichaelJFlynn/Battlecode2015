package darkpurple1;

import battlecode.common.*;

public class MessageBoard {

	private static RobotController rc;
	static final int SOLDIER_COUNT_CHANNEL = 20;
	static final int SOLDIER_WAVE_CHANNEL = 31;
	static final int SOLDIERS_PER_WAVE = 20;
	static int myWave;
	
	
	
	static final BuildItem[] sprintBuildOrder = new BuildItem[]
			{ new BuildItem(RobotType.BEAVER, 2, true) 
			
			};
	static BuildItem[] currentBuildOrder = sprintBuildOrder;
	
	public static void init(RobotController theRC) {
		rc = theRC;
	}
	
	public static void soldierSpawned() throws GameActionException {
		int soldiers = rc.readBroadcast(SOLDIER_COUNT_CHANNEL);
		int wave = rc.readBroadcast(SOLDIER_WAVE_CHANNEL);
		if(soldiers >= SOLDIERS_PER_WAVE) {
			myWave = wave + 1;
			rc.broadcast(SOLDIER_WAVE_CHANNEL, wave + 1);
			rc.broadcast(SOLDIER_COUNT_CHANNEL, 1);
		} else {
			myWave = wave;
			rc.broadcast(SOLDIER_COUNT_CHANNEL, soldiers+1);
		}
	}

	public static int spawnedSoldiersInMyWave() throws GameActionException {
		return rc.readBroadcast(SOLDIER_COUNT_CHANNEL);
	}
	
	
	
	public static RobotType queryBuildOrder() {
		
		
		
		return null; 
	}

}
