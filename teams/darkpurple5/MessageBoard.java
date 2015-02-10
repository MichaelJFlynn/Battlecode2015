package darkpurple5;

import battlecode.common.*;

public class MessageBoard {

	private static RobotController rc;
	
	static final int BUILD_PROGRESS_POINTER_CHANNEL = 4;
	static final int BUILD_QUANTITY_CHANNEL = 5;
	
	static final int RALLY_LOC_CHANNEL = 6;
	
	static final int SOLDIER_COUNT_CHANNEL = 20;
	static final int SOLDIER_WAVE_CHANNEL = 21;
	static final int SOLDIERS_PER_WAVE = 20;
	static int myWave;
	
	static final int MINER_CHANNEL_ZERO = 23;
	static final int MINER_CHANNEL_ONE = 24;
	static final int MINER_CHANNEL_TWO = 25;
	
	static final int SUPPLY_DRONE_CHANNEL_ZERO = 26;
	static final int SUPPLY_DRONE_CHANNEL_ONE = 27;
	static final int SUPPLY_DRONE_CHANNEL_TWO = 28;
	static final int SUPPLY_DRONE_REQUEST_CHANNEL = 29; 
	static final int SUPPLY_DRONE_COUNT = 2;
	
	static final int SUPPLY_DRONE_ID_ARRAY = 50;
	
	static final int SUPPLY_QUEUE_FRONT_POINTER = 98;
	static final int SUPPLY_QUEUE_BACK_POINTER = 99; 
	static final int SUPPLY_QUEUE_START = 100;
	static final int SUPPLY_QUEUE_LENGTH = 100;
	
	static final int MAP_BASE_CHANNEL = 51136;
	static final int MAP_LOWER_LIMIT = 36736;
	static final int MAP_MAX_WIDTH = 120;
	static final int MAP_MAX_HEIGHT = 120;
	
	static final int MINING_QUEUE_FRONT_POINTER = 999;
	static final int MINING_QUEUE_BACK_POINTER = 998;
	static final int MINING_QUEUE_START = 1000;
	static final int MINING_QUEUE_LENGTH = 1000;
	
	static final int REMOTE_SUPPLY_QUEUE_FRONT_POINTER = 2000;
	static final int REMOTE_SUPPLY_QUEUE_BACK_POINTER = 2001;
	static final int REMOTE_SUPPLY_QUEUE_START = 2002;
	static final int REMOTE_SUPPLY_QUEUE_LENGTH = 998;

	static final Direction[] directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST, Direction.NONE};
	
	static final BuildItem[] sprintBuildOrder = new BuildItem[]
			{
		new BuildItem(RobotType.BEAVER, 1), 
		new BuildItem(RobotType.MINERFACTORY, 1),
		new BuildItem(RobotType.HELIPAD, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 1),
		new BuildItem(RobotType.AEROSPACELAB, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 6),
		new BuildItem(RobotType.AEROSPACELAB, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 2),
		new BuildItem(RobotType.AEROSPACELAB, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 1),
		new BuildItem(RobotType.BEAVER, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 2),
		new BuildItem(RobotType.AEROSPACELAB, 1),
		new BuildItem(RobotType.HELIPAD, 1),
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
	
	
	
	public static boolean queryBuildOrder() throws GameActionException {
		int buildIndex = rc.readBroadcast(BUILD_PROGRESS_POINTER_CHANNEL);
		int buildQuantity = rc.readBroadcast(BUILD_QUANTITY_CHANNEL);
		//rc.setIndicatorString(0, "" + buildIndex);
		if(buildIndex >= currentBuildOrder.length) return false;
		BuildItem item = currentBuildOrder[buildIndex];
		RobotType next = item.type();
		//rc.setIndicatorString(1, "" + next);
		MapLocation hq = rc.senseHQLocation();
		MapLocation myLoc = rc.getLocation();
		if(rc.isCoreReady()) {	
			for(Direction dir : directions) {
				if(next.isBuildable()) {
					if(rc.canBuild(dir, next) && (hq.x % 2 == myLoc.add(dir).x % 2 && hq.y % 2 == myLoc.add(dir).y % 2)) {
						// we're building, broadcast moving down build order
						buildQuantity++;
						if(buildQuantity >= item.quantity()) {
							rc.broadcast(BUILD_PROGRESS_POINTER_CHANNEL, buildIndex + 1);
							rc.broadcast(BUILD_QUANTITY_CHANNEL, 0);
						} else {
							rc.broadcast(BUILD_QUANTITY_CHANNEL, buildQuantity);
						}

						// actually build the building
						rc.build(dir, next);
						return true; 
					}
				} else {
					if(rc.canSpawn(dir, next)) {

						// we're building, broadcast moving down build order
						buildQuantity++;
						if(buildQuantity >= item.quantity()) {
							rc.broadcast(BUILD_PROGRESS_POINTER_CHANNEL, buildIndex + 1);
							rc.broadcast(BUILD_QUANTITY_CHANNEL, 0);
						} else {
							rc.broadcast(BUILD_QUANTITY_CHANNEL, buildQuantity);
						}

						// actually build the building
						rc.spawn(dir, next);
						return true; 
					} 
				}
			}
		} else {
			//rc.setIndicatorString(2, "No build requirements or not ready");
		}

		return false; 
	}

	
	public static void requestSupply(int robotID) throws GameActionException {
		int back_pointer = rc.readBroadcast(SUPPLY_QUEUE_BACK_POINTER); 
		rc.broadcast(back_pointer + SUPPLY_QUEUE_START, robotID);
		rc.broadcast(SUPPLY_QUEUE_BACK_POINTER, (back_pointer + 1) % SUPPLY_QUEUE_LENGTH);
	}
	
	public static int popSupplyQueue() throws GameActionException {
		int front_pointer = rc.readBroadcast(SUPPLY_QUEUE_FRONT_POINTER);
		int ID = rc.readBroadcast(front_pointer + SUPPLY_QUEUE_START);
		//System.out.println("Pop Supply Queue: front pointer " + front_pointer + ", back pointer " + rc.readBroadcast(SUPPLY_QUEUE_BACK_POINTER));
		if(ID == 0) {
			// theres nothing here, so we shouldn't increment the queue pointer
			return ID;
		} else {
			rc.broadcast(front_pointer + SUPPLY_QUEUE_START, 0);
			rc.broadcast(SUPPLY_QUEUE_FRONT_POINTER, (front_pointer + 1) % SUPPLY_QUEUE_LENGTH);
			return ID;
		}
	}

	public static MapLocation getMissileDestination() throws GameActionException {
		MapLocation myLoc = rc.getLocation();
		MapLocation hq = rc.senseHQLocation();
		int map_channel = getChannelFromLocation(myLoc);
		int signal = rc.readBroadcast(map_channel);
		if(signal == 0) return null;
		int x = (signal % 1000) + hq.x - 500;
		int y = ((signal / 1000) % 1000) + hq.y - 500;
		MapLocation dest = new MapLocation(x,y);
		return dest;
	}

	public static int getChannelFromLocation(MapLocation loc) {
		MapLocation hq = rc.senseHQLocation();
		int x = loc.x - hq.x;
		int y = loc.y - hq.y;
		return 120*y + x + MAP_BASE_CHANNEL;
	}
	
	public static void broadcastCoordinatesToLocationChannel(MapLocation location, MapLocation coordinates) throws GameActionException {
		MapLocation hq = rc.senseHQLocation();
		int channel = getChannelFromLocation(location);
		int x = coordinates.x - hq.x + 500;
		int y = coordinates.y - hq.y + 500;
		rc.broadcast(channel, y*1000 + x);
	}

	public static void addLocationToMiningQueue(MapLocation add) throws GameActionException {
		int back_pointer = rc.readBroadcast(MINING_QUEUE_BACK_POINTER);
		MapLocation hq = rc.senseHQLocation();
		int x = add.x - hq.x + 500;
		int y = add.y - hq.y + 500; 
		rc.broadcast(back_pointer + MINING_QUEUE_START, 1000*y + x);
		rc.broadcast(MINING_QUEUE_BACK_POINTER, (back_pointer + 1) % MINING_QUEUE_LENGTH);
	}

	public static MapLocation requestMiningLocation() throws GameActionException {
		MapLocation hq = rc.senseHQLocation();
		int front_pointer = rc.readBroadcast(MINING_QUEUE_FRONT_POINTER);
		int signal = rc.readBroadcast(front_pointer + MINING_QUEUE_START);
		int x = 0;
		int y = 0;
		try{
		while(signal != 0) {
			rc.broadcast(front_pointer + MINING_QUEUE_FRONT_POINTER, 0);
			rc.broadcast(MINING_QUEUE_FRONT_POINTER, (front_pointer + 1) % MINING_QUEUE_LENGTH);
			x = (signal % 1000) + hq.x - 500;
			y = ((signal / 1000) % 1000) + hq.y - 500; 
			MapLocation loc = new MapLocation(x,y);
			int map_signal = rc.readBroadcast(getChannelFromLocation(loc));
			int claimed = (map_signal / (1000*1000)) % 10;
			if(claimed == 0) {
				return loc;
			} else {
				front_pointer = rc.readBroadcast(MINING_QUEUE_FRONT_POINTER);
				signal = rc.readBroadcast(front_pointer + MINING_QUEUE_START);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("x: " + x + ", y: " + y + ", signal: " + signal);
		}
		return null;
	}

	public static void claimMiningLocation(MapLocation mining_location) throws GameActionException {
		if(mining_location == null) return; 
		int channel = getChannelFromLocation(mining_location);
		int signal = rc.readBroadcast(channel);
		rc.broadcast(channel, signal + 1000*1000);
	}

	public static int checkMinersNeeded() throws GameActionException {
		int channel = MINER_CHANNEL_ZERO + ((Clock.getRoundNum()-1) % 3);
		int channel_next = MINER_CHANNEL_ZERO + ((Clock.getRoundNum()+1) % 3);
		int number = rc.readBroadcast(channel);
		rc.broadcast(channel_next, 0);
		int front = rc.readBroadcast(MINING_QUEUE_FRONT_POINTER);
		int back = rc.readBroadcast(MINING_QUEUE_BACK_POINTER);
		if(front > back) {
			return (MINING_QUEUE_LENGTH - front) + back - number;
		} else {
			return (back - front) - number;
		}
	}

	public static void checkInMiner() throws GameActionException {
		int channel = MINER_CHANNEL_ZERO + (Clock.getRoundNum() % 3); 
		int number = rc.readBroadcast(channel);
		
		rc.broadcast(channel, number);
	}

	public static double towersNear(MapLocation loc) throws GameActionException {
		int channel = getChannelFromLocation(loc);
		int signal = rc.readBroadcast(channel);
		int quantity = ((signal/(1000*1000*10)) % 10)- 2;
		if(quantity >= 0) {
			return quantity;
		} else {
			return 0;
		}
}

	public static boolean recordTowerTile(MapLocation loc, MapLocation[] towers) throws GameActionException {
		int channel = getChannelFromLocation(loc);
		int quantity = 0;
		for(MapLocation tower : towers) {
			if(loc.distanceSquaredTo(tower) < RobotType.TOWER.attackRadiusSquared) {
				quantity++;
			}
		}
		rc.broadcast(channel, (quantity+2) * 1000 * 1000 * 10 );
		return (quantity > 0); 
	}

	public static boolean towerTileRecorded(MapLocation mapLocation) throws GameActionException {
		int channel = getChannelFromLocation(mapLocation);
		int signal = rc.readBroadcast(channel);
		
		return (signal / (1000 * 1000 * 10) != 0);
	}

	public static MapLocation getRallyLoc() throws GameActionException {
		int signal = rc.readBroadcast(RALLY_LOC_CHANNEL);
		if(signal == 0) {
			return rc.senseEnemyHQLocation();
		} else {
			MapLocation hq = rc.senseHQLocation();
			int x = (signal % 1000) + hq.x - 500;
			int y = ((signal / 1000) % 1000) + hq.y - 500; 
			MapLocation loc = new MapLocation(x,y);
			return loc;
		}
	}
	
	public static void broadcastRallyLoc(MapLocation loc) throws GameActionException {
		MapLocation hq = rc.senseHQLocation();
		int signalx = loc.x - hq.x + 500;
		int signaly = loc.y - hq.y + 500;
		rc.broadcast(RALLY_LOC_CHANNEL, 1000*signaly + signalx);
	}

	public static int checkSupplyDrones() throws GameActionException {
		int channel = SUPPLY_DRONE_CHANNEL_ZERO + ((Clock.getRoundNum()-1) % 3);
		int channel_next = SUPPLY_DRONE_CHANNEL_ZERO + ((Clock.getRoundNum()+1) % 3);
		int number = rc.readBroadcast(channel);
		rc.broadcast(channel_next, 0);
		int deficit = SUPPLY_DRONE_COUNT - number;
		rc.broadcast(SUPPLY_DRONE_REQUEST_CHANNEL, deficit);
		return deficit;
	}
/*
	public static void cleanUpRemoteSupplyQueue() throws GameActionException {
		int front_pointer = rc.readBroadcast(REMOTE_SUPPLY_QUEUE_FRONT_POINTER);
		int back_pointer = rc.readBroadcast(REMOTE_SUPPLY_QUEUE_BACK_POINTER);
		int front_channel = front_pointer + REMOTE_SUPPLY_QUEUE_START;
		int back_channel = (back_pointer - 1) % REMOTE_SUPPLY_QUEUE_LENGTH +  REMOTE_SUPPLY_QUEUE_START;
		int roundNum = Clock.getRoundNum();
		while(front_channel != back_channel) {
			if(rc.readBroadcast(front_channel) % 10000 < roundNum - 1) {
				// flip the signal in from the back
				int back_signal = rc.readBroadcast(back_channel);
				rc.broadcast(front_channel, back_signal);
				rc.broadcast(back_channel, 0);
				
				// change the back pointers accordingly
				back_pointer = (back_pointer - 1) % REMOTE_SUPPLY_QUEUE_LENGTH;
				back_channel = (back_pointer - 1) % REMOTE_SUPPLY_QUEUE_LENGTH + REMOTE_SUPPLY_QUEUE_START;
			} else {
				// move up one in the queue
				front_pointer = (front_pointer + 1) % REMOTE_SUPPLY_QUEUE_LENGTH;
				front_channel = front_pointer + REMOTE_SUPPLY_QUEUE_START;
			}
		}
		// now front_channel == back_channel
		if(rc.readBroadcast(front_channel) % 10000 < roundNum - 1) {
			rc.broadcast(front_channel, 0);
			rc.broadcast(REMOTE_SUPPLY_QUEUE_BACK_POINTER, front_channel - REMOTE_SUPPLY_QUEUE_START);
		} else {
			rc.broadcast(REMOTE_SUPPLY_QUEUE_BACK_POINTER, back_pointer - REMOTE_SUPPLY_QUEUE_START);
		}
	}*/
	
	public static void cleanUpRemoteSupplyQueue() throws GameActionException {
		int front_pointer = rc.readBroadcast(REMOTE_SUPPLY_QUEUE_FRONT_POINTER);
		int back_pointer = rc.readBroadcast(REMOTE_SUPPLY_QUEUE_BACK_POINTER);
		int roundNum = Clock.getRoundNum();
		int front_channel = front_pointer + REMOTE_SUPPLY_QUEUE_START;
		while(rc.readBroadcast(front_channel) % 1000 < roundNum - 1 && 
				front_pointer != back_pointer && Clock.getBytecodesLeft() > 500) {
			
			// if the signal is out of date
			// set it to zero
			rc.broadcast(front_channel, 0);
			// move up one in the queue
			front_pointer = (front_pointer + 1) % REMOTE_SUPPLY_QUEUE_LENGTH;
			front_channel = front_pointer + REMOTE_SUPPLY_QUEUE_START;
		}
	
		rc.broadcast(REMOTE_SUPPLY_QUEUE_FRONT_POINTER, front_pointer);
	}

	public static boolean tryToBecomeSupplyDrone() throws GameActionException {
		int deficit = rc.readBroadcast(SUPPLY_DRONE_REQUEST_CHANNEL);
		if(deficit > 0) {
			rc.broadcast(SUPPLY_DRONE_REQUEST_CHANNEL, deficit - 1);
			return true;
		}
		return false;
	}

	public static void checkInSupplyDrone() throws GameActionException {
		int channel = SUPPLY_DRONE_CHANNEL_ZERO + (Clock.getRoundNum() % 3);
		int signal = rc.readBroadcast(channel);
		rc.broadcast(SUPPLY_DRONE_ID_ARRAY + signal, rc.getID());
		rc.broadcast(channel, signal + 1);
	}
	
	public static void requestRemoteSupply() throws GameActionException {
		int back_pointer = rc.readBroadcast(REMOTE_SUPPLY_QUEUE_BACK_POINTER); 
		rc.broadcast(back_pointer + REMOTE_SUPPLY_QUEUE_START, rc.getID()*10000 + Clock.getRoundNum());
		rc.broadcast(REMOTE_SUPPLY_QUEUE_BACK_POINTER, (back_pointer + 1) % REMOTE_SUPPLY_QUEUE_LENGTH);
	}

	public static int popRemoteSupplyQueue() throws GameActionException {
		int front_pointer = rc.readBroadcast(REMOTE_SUPPLY_QUEUE_FRONT_POINTER);
		int ID = rc.readBroadcast(front_pointer + REMOTE_SUPPLY_QUEUE_START) / 10000;
		if(ID == 0) {
			// theres nothing here, so we shouldn't increment the queue pointer
			return ID;
		} else {
			rc.broadcast(front_pointer + REMOTE_SUPPLY_QUEUE_START, 0);
			rc.broadcast(REMOTE_SUPPLY_QUEUE_FRONT_POINTER, (front_pointer + 1) % REMOTE_SUPPLY_QUEUE_LENGTH);
			return ID;
		}
	}

	public static void visitTowerTile(MapLocation add) throws GameActionException {
		int channel = getChannelFromLocation(add);		
		rc.broadcast(channel, 1000 * 1000 * 10 );
	}

	public static void resupplySupplyDrones(int deficit) throws GameActionException {
		for(int i =0; i < SUPPLY_DRONE_COUNT - deficit; i++) {
			int id = rc.readBroadcast(SUPPLY_DRONE_ID_ARRAY + i);
			if(rc.canSenseRobot(id)) {
				RobotInfo robot = rc.senseRobot(id);
				if(rc.getLocation().distanceSquaredTo(robot.location) < 15) {
					int supply_needed = (int) (Drone.SUPPLY_DRONE_CARRY_LIMIT + 6000 - rc.getSupplyLevel());
					if(supply_needed > 0) {
						rc.transferSupplies(supply_needed, robot.location);
					}
				}
			}
		}
		
	}
}
