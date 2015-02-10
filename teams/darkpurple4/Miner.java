package darkpurple4;

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
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		Mover.init(rc);	
		Skirmisher.init(rc);	
		MessageBoard.init(rc);
		mining_location = null; 
		try {
			MessageBoard.requestSupply(rc.getID());
			while(rc.getSupplyLevel() == 0) {
				Mover.goTo(rc.senseHQLocation());
				rc.yield();
			}
			while(true) {
				towers = rc.senseEnemyTowerLocations();
				myLoc = rc.getLocation();
				enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
				rc.yield();
				MessageBoard.checkInMiner();
				if(enemies.length > 0 || closeToTowers()) {
					if(mining_location != null) {
						MessageBoard.addLocationToMiningQueue(mining_location);
						mining_location = null;
					}
					Skirmisher.battlecode();
					continue;
				}
				
				rc.setIndicatorString(0, "mining location: " + mining_location);

				if(mining_location == null) {
					// here we want to add all our neighbors to the queue
					double maxOre = 0; 
					for(Direction dir : directions) {
						double ore = rc.senseOre(myLoc.add(dir));
						if(ore > 0 && rc.canMove(dir)) {
							if(ore > maxOre) {
								maxOre = ore;
								mining_location = myLoc.add(dir);
							}
							MessageBoard.addLocationToMiningQueue(myLoc.add(dir));		
						}
					}

					if(mining_location == null) {
						mining_location = MessageBoard.requestMiningLocation(); 
						MessageBoard.claimMiningLocation(mining_location);
					} else {
						MessageBoard.claimMiningLocation(mining_location);
					}

					if(mining_location != null) {
						Mover.goTo(mining_location);
					} else {
					}
					
				} else {
					if(rc.canSenseLocation(mining_location)) {
						RobotInfo info = rc.senseRobotAtLocation(mining_location);
						if(info != null && info.type.isBuilding) {
							mining_location = null;
							continue;
						}
					}
					if(myLoc.equals(mining_location)) {
						if(rc.senseOre(myLoc) > 0 ){
							if(rc.isCoreReady()) {
								rc.mine();
								continue;
							}
						} else {
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


