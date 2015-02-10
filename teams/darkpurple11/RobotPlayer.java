package darkpurple11;

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
			case MINERFACTORY:
				MinerFactory.loop(rc);
				break;
			case MINER: 
				Miner.loop(rc);
				break;
			case HELIPAD:
				Helipad.loop(rc);
				break;
			case DRONE:
				Drone.loop(rc);
				break;
			case AEROSPACELAB:
				AerospaceLab.loop(rc);
				break;
			case LAUNCHER:
				Launcher.loop(rc);
				break;
			case MISSILE:
				Missile.loop(rc);
				break; 
			case TANKFACTORY:
				TankFactory.loop(rc);
				break;
			case TANK:
				Tank.loop(rc);
				break;
			case TRAININGFIELD:
				TrainingField.loop(rc);
				break; 
			case COMMANDER:
				Commander.loop(rc);
				break;
			default:
				while(true) {
					rc.yield();
				}
		}
	}
}
