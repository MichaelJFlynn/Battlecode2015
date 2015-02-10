package team372;

import battlecode.common.*;

public class Miner {

	static RobotController rc;
	static MapLocation myLoc;
	static RobotInfo[] enemies;
	static final Direction[] directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	static MapLocation mining_location;
	static MapLocation[] towers;
	static Team myTeam;
	static boolean optimalMining;
	
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		myTeam = rc.getTeam();
		Mover.init(rc);	
		Skirmisher.init(rc);	
		MessageBoard.init(rc);
		mining_location = null;
		optimalMining = true;
		try {
			MessageBoard.requestSupply(rc.getID());
			while(rc.getSupplyLevel() == 0) {
				Mover.goTo(rc.senseHQLocation());
				rc.yield();
			}
			while(true) {
				towers = rc.senseEnemyTowerLocations();
				myLoc = rc.getLocation();
				enemies = rc.senseNearbyRobots(myLoc, 35, rc.getTeam().opponent());
				rc.yield();
				MessageBoard.checkInMiner();
				
				if(rc.getSupplyLevel() == 0) {
					MessageBoard.requestRemoteSupply();
				}
				resupplyFriendlies();

				if(enemies.length > 0) {
					if(mining_location != null) {
						MessageBoard.addLocationToSubParMiningQueue(mining_location);
						mining_location = null;
					}
					Skirmisher.battlecode();
					MessageBoard.broadcastRallyLoc(myLoc);
					continue;
				}
				
				rc.setIndicatorString(0, "mining location: " + mining_location);
/*
				if(!myLoc.equals(mining_location) && mining_location != null && rc.senseRobotAtLocation(myLoc.add(myLoc.directionTo(mining_location))) != null) {
					if(optimalMining) {
						MessageBoard.addLocationToMiningQueue(mining_location);
					} else {
						MessageBoard.addLocationToSubParMiningQueue(mining_location);
					}
					mining_location = null;
				}*/
				
				if(mining_location == null) {
					// here we want to add all our neighbors to the queue
					double maxOre = 5; 
					for(Direction dir : directions) {
						double ore = rc.senseOre(myLoc.add(dir));
						if(ore > 0 && rc.canMove(dir)) {
							if(ore > maxOre) {
								maxOre = ore;
								mining_location = myLoc.add(dir);
								optimalMining = true;
							}
							if(ore > 5) {
								MessageBoard.addLocationToMiningQueue(myLoc.add(dir));		
							} else {
								MessageBoard.addLocationToSubParMiningQueue(myLoc.add(dir));
							}
						}
					}

					if(mining_location == null) {
						mining_location = MessageBoard.requestMiningLocation(); 
						optimalMining = true;
					}
					if(mining_location == null) {
						mining_location = MessageBoard.requestSubParMiningLocation(); 
						optimalMining = false;
					}
					
					MessageBoard.claimMiningLocation(mining_location);
					

					if(mining_location != null) {
						Mover.goTo(mining_location);
					} else {
						Mover.tryToMove(myLoc.directionTo(rc.senseHQLocation()).opposite());
					}
					
				} else {
					if(rc.canSenseLocation(mining_location)) {
						RobotInfo info = rc.senseRobotAtLocation(mining_location);
						if(info != null && info.type.isBuilding) {
							mining_location = null;
							continue;
						}
					}
					rc.setIndicatorString(2, "" + optimalMining);
					if(myLoc.equals(mining_location)) {
						if(rc.senseOre(myLoc) > (optimalMining ? 5 : 0)){
							if(rc.isCoreReady()) {
								rc.mine();
								continue;
							}
						} else {
							if(optimalMining) {
								MessageBoard.addLocationToSubParMiningQueue(mining_location);
								MessageBoard.unclaimLocation(mining_location);
							}
							mining_location = null;
						}
					} else {
						Mover.goTo(mining_location);
					}
				}					
			}
		} catch(Exception e) {
			System.out.println("Minerexception");
			e.printStackTrace();
		}
	}
	
	private static boolean nearGoodMining() {
		for(Direction dir : directions) {
			if(rc.canSenseLocation(rc.getLocation().add(dir))) {
				double ore = rc.senseOre(rc.getLocation().add(dir));
				if( ore > 4) {
					return true;
				}
			}
		}
		return false;
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
}


