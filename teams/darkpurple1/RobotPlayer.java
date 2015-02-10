package darkpurple1;

import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) throws Exception {
		switch (rc.getType()) {
			case HQ:
				HQ.loop(rc);
				break;
			case TOWER:
				Tower.loop(rc);
				break; 
			case BEAVER:
				Beaver.loop(rc);
				break;
			case BARRACKS:
				Barracks.loop(rc);
				break;
			case SOLDIER:
				Soldier.loop(rc);
				break;
			default:
				while(true) {
					rc.yield();
				}
		}
	}
}
