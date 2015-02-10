package team372;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Drone {
	
	public static final double SUPPLY_DRONE_CARRY_LIMIT = 4 * (30 * 200);
	
	static RobotController rc;
	static MapLocation[] towers;
	static MapLocation myLoc;
	static Team myTeam;
	static Team enemyTeam; 
	static boolean waveAttack;
	static RobotInfo target;
	static boolean isSupplyDrone;
	static SupplyDroneState supplyDroneState;
	static int supplyee;
	private static enum SupplyDroneState { FETCHING, READING, DELIVERING, ARRIVED, DISTRIBUTING, NULL }
	static MapLocation harassLocation;
	
	public static void loop(RobotController theRC) {
		rc = theRC; 
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		target = null;
		supplyee = 0; 
		isSupplyDrone = false;
		supplyDroneState = SupplyDroneState.NULL;
		
		MessageBoard.init(rc);
		Mover.init(rc);
		Skirmisher.init(rc);
		try {
			if(MessageBoard.checkHarrassDrones() <=0 && MessageBoard.tryToBecomeSupplyDrone()) {
				isSupplyDrone = true;
				supplyDroneState = SupplyDroneState.FETCHING;
				//System.out.println("I am a supply drone!");
			}
			harassLocation = MessageBoard.getHarassLocation();
			if(harassLocation == null) harassLocation = rc.senseEnemyHQLocation();
			MessageBoard.requestSupply(rc.getID());
			while(rc.getSupplyLevel() == 0) {
				Mover.goTo(rc.senseHQLocation());
				rc.yield();
			}
			while(true) {
				rc.yield();
				towers = rc.senseEnemyTowerLocations();
				myLoc = rc.getLocation();
				RobotInfo[] enemies = rc.senseNearbyRobots(35, enemyTeam);
				Skirmisher.setGoalLoc(harassLocation);
				if(isSupplyDrone) {
					rc.setIndicatorString(0, "" + supplyDroneState);
					runSupplyDrone(); 
					continue; 
				} else {
					MessageBoard.checkInHarassDrone();
					Skirmisher.setGoalLoc(rc.senseEnemyHQLocation());
				}
				
				if(rc.getSupplyLevel() == 0) {
					MessageBoard.requestRemoteSupply();
				}
				
				resupplyFriendlies();
				
				
				// fight enemies
				if(enemies.length > 0) {
					//if(target == null) {
						//target = enemies[0];
					//}
					//if(Clock.getRoundNum() < 1300) {
						//Skirmisher.moveWithoutAttacking(towers, enemies);
					//} else {
						Skirmisher.battlecode();
					//}
					harassLocation = MessageBoard.getHarassLocation();
					if(harassLocation == null) harassLocation = rc.senseEnemyHQLocation();
					continue;
				}
				
				if(closeToTowers()) {
					if(Skirmisher.tryToAttackTower(towers, myLoc))
						continue;
				}
				/*
				if(target != null && rc.canSenseRobot(target.ID)) {
					Mover.moveDrone(myLoc.directionTo(rc.senseRobot(target.ID).location));
				} else {
					target = null;
					Mover.moveDrone(myLoc.directionTo(rc.senseEnemyHQLocation()));
				}*/
				if(myLoc.equals(harassLocation)) {
					harassLocation = rc.senseEnemyHQLocation();
				}
				Mover.moveDrone(myLoc.directionTo(harassLocation));
			}
		} catch(Exception e) {
			System.out.println("Drone exception");
			e.printStackTrace(); 
		}
	}


	private static void runSupplyDrone() throws Exception {
		MessageBoard.checkInSupplyDrone();
		switch(supplyDroneState) {
		case FETCHING:
			Skirmisher.setGoalLoc(rc.senseHQLocation());
			if(rc.getSupplyLevel() < SUPPLY_DRONE_CARRY_LIMIT) {
				Mover.moveDrone(myLoc.directionTo(rc.senseHQLocation()));
			} else {
				supplyee = MessageBoard.popRemoteSupplyQueue(); 
				if(supplyee == 0) {
					supplyDroneState = SupplyDroneState.DELIVERING;
				} else {
					supplyDroneState = SupplyDroneState.DELIVERING;
				}
			}
			break;
		case READING:
			supplyee = MessageBoard.popRemoteSupplyQueue(); 
			if(supplyee != 0) {
				supplyDroneState = SupplyDroneState.DELIVERING;
			}
			break; 
		case DELIVERING:
			if(rc.canSenseRobot(supplyee)) {
				RobotInfo robot = rc.senseRobot(supplyee);
				Mover.moveDrone(myLoc.directionTo(robot.location));
				if(myLoc.distanceSquaredTo(robot.location) < 3) {
					supplyDroneState = SupplyDroneState.DISTRIBUTING;
				}
			} else {
				supplyee = 0; 
				supplyDroneState = SupplyDroneState.READING;
			}
			break;
		case ARRIVED:
			break;
		case DISTRIBUTING:
			RobotInfo[] supplyable = rc.senseNearbyRobots(15, myTeam);
			for(RobotInfo robot : supplyable) {
				if(robot.supplyLevel < robot.type.supplyUpkeep * 20) {
					rc.transferSupplies(3000, robot.location);
					if(rc.getSupplyLevel() <= 1000) {
						break;
					}
				}
			}
			if(rc.getSupplyLevel() > 500) {
				supplyDroneState = SupplyDroneState.READING;
			} else {
				supplyee = 0;
				supplyDroneState = SupplyDroneState.FETCHING;
			}
			break;
		case NULL:
			throw new Exception("supply drone state is null inside runSupplyDrone");
		}
		
	}


	private static void resupplyFriendlies() throws GameActionException {
		if(rc.getSupplyLevel() < rc.getType().supplyUpkeep * 100) {
			return;
		}
		for(RobotInfo friend : rc.senseNearbyRobots(15, myTeam)) {
			if(friend.supplyLevel < 10 * friend.type.supplyUpkeep) {
				rc.transferSupplies(friend.type.supplyUpkeep * 30, friend.location);
			}
		}
	}
	
	private static boolean closeToTowers() {
		double TSqrange = RobotType.TOWER.attackRadiusSquared;
		double HQSqrange = RobotType.HQ.attackRadiusSquared;
		for(MapLocation tower : towers) { 
			if(myLoc.distanceSquaredTo(tower) <= TSqrange + Math.sqrt(TSqrange)*2 + 1) {
				return true;
			}
		}
		if(myLoc.distanceSquaredTo(rc.senseEnemyHQLocation()) <= HQSqrange + Math.sqrt(HQSqrange)*2 + 1) {
			return true; 
		}
		
		return false;
	}
}
