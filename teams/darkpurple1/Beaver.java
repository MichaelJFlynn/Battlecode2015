package darkpurple1;

import java.util.*;

import battlecode.common.*;

public class Beaver {
	
	static RobotController rc; 
	static Direction[] directions;
	static Team myTeam;
	static Team enemyTeam;
	static MapLocation goalLoc;
	static MapLocation myLoc;
	static RobotInfo[] enemies;
	static RobotInfo[] adjacents;
	static Random rand;
	
	public static void loop(RobotController theRC) {
		rc = theRC;
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
				Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
				Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		goalLoc = enemyLoc; 
		rand = new Random();
		Mover.init(rc);
		Skirmisher.init(rc);
		try {
			
			while(true) {
				myLoc = rc.getLocation();
				enemies = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemyTeam);
				adjacents = rc.senseNearbyRobots(1, myTeam);
				if(enemies.length > 0) {
					Skirmisher.battlecode();
					continue;
				}
				
				if(rc.hasBuildRequirements(RobotType.HANDWASHSTATION) && rc.readBroadcast(2) < 1
						&& rc.isCoreReady()) {
					if(tryToBuild(RobotType.HANDWASHSTATION)) {
						rc.broadcast(2, 1);
					}
					
				}
				
				if(rc.hasBuildRequirements(RobotType.BARRACKS) && rc.readBroadcast(1) <= 2
						&& rc.isCoreReady()) {
					rc.broadcast(1, rc.readBroadcast(1) + 1);
					if(!tryToBuild(RobotType.BARRACKS)) {
						rc.broadcast(1, rc.readBroadcast(1) -1);
					}
					continue;
				}
				
				
				// calculate where some ore is
				if(notNearAnything() && rc.senseOre(rc.getLocation()) > 0) {
					miningLoop();
					continue;
				}
				

				
				if(rc.isCoreReady()) {
					Mover.goTo(goalLoc);
				}
			} 
		
		} catch (Exception e) {
			System.out.println("Beaver exception");
			e.printStackTrace();
		}
		
	}
	
	private static void miningLoop() throws GameActionException {
		while(notNearAnything() && rc.senseOre(myLoc) > 0) {
			if(rc.isCoreReady()) {
				rc.mine();
			}
		}
	}

	
	public static boolean notNearAnything() throws GameActionException {
		boolean nearAnything = false;
		
		// if we are too near a adjacents
		
		if(adjacents.length > 0) {
			nearAnything = true;
			goalLoc = oreTileWithNoAdjacents();
		}
		
		// if we are too near an enemy
		if(enemies.length > 0) {
			nearAnything = true;
		}
		
		return !nearAnything;
	}
	
	private static MapLocation oreTileWithNoAdjacents() throws GameActionException {
		MapLocation[] locs = shuffleMapLocation(MapLocation.getAllMapLocationsWithinRadiusSq(myLoc, rc.getType().sensorRadiusSquared));
		//Collections.shuffle(locs, rand);
		for(MapLocation loc : locs) {
			if(rc.senseOre(loc) > 0) {
				boolean OK = true;
				for(Direction dir : directions) {
					try {
						if(!dir.isDiagonal() && rc.senseRobotAtLocation(loc.add(dir)) != null) {
							OK = false;
						}
					} catch (GameActionException e) {
						
					}
				}
				if(OK) {
					return loc;
				}
			}
		}
		return rc.senseEnemyHQLocation();
	}


	public static MapLocation[] shuffleMapLocation(MapLocation[] m) {
		for(int i=0; i < m.length; i++) {
			int sub = rand.nextInt(m.length);
			MapLocation temp = m[sub];
			m[sub] = m[i];
			m[i] = temp;
		}
		return m;
	}
	
	public static boolean tryToBuild(RobotType type) throws GameActionException {
		for(Direction d : directions) {
			if(rc.canBuild(d, type)){ 
				rc.build(d, type);
				return true;
			}
		}
		return false;
	}
}


