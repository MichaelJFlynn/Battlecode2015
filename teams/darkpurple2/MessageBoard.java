package darkpurple2;

import battlecode.common.*;

public class MessageBoard {

	private static RobotController rc;
	static final int SOLDIER_COUNT_CHANNEL = 20;
	static final int SOLDIER_WAVE_CHANNEL = 21;
	static final int SOLDIERS_PER_WAVE = 20;
	static int myWave;
	
	static final int BUILD_PROGRESS_POINTER_CHANNEL = 4;
	static final int BUILD_QUANTITY_CHANNEL = 5;
	
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

	static final Direction[] directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST, Direction.NONE};
	
	static final BuildItem[] sprintBuildOrder = new BuildItem[]
			{
		new BuildItem(RobotType.BEAVER, 1), 
		new BuildItem(RobotType.MINERFACTORY, 1),
		new BuildItem(RobotType.HELIPAD, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 3),
		new BuildItem(RobotType.AEROSPACELAB, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 2),
		new BuildItem(RobotType.AEROSPACELAB, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 1),
		new BuildItem(RobotType.BEAVER, 1),
		new BuildItem(RobotType.SUPPLYDEPOT, 1),
		new BuildItem(RobotType.AEROSPACELAB, 2),
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
		rc.setIndicatorString(0, "" + buildIndex);
		if(buildIndex >= currentBuildOrder.length) return false;
		BuildItem item = currentBuildOrder[buildIndex];
		RobotType next = item.type();
		rc.setIndicatorString(1, "" + next);
		
		if(rc.isCoreReady()) {	
			for(Direction dir : directions) {
				if(next.isBuildable()) {
					if(rc.canBuild(dir, next)) {

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
			rc.setIndicatorString(2, "No build requirements or not ready");
		}

		return false; 
	}

	
	public static void requestSupply(int robotID) throws GameActionException {
		int back_pointer = rc.readBroadcast(SUPPLY_QUEUE_BACK_POINTER); 
		rc.broadcast(back_pointer + SUPPLY_QUEUE_START, rc.getID());
		rc.broadcast(SUPPLY_QUEUE_BACK_POINTER, (back_pointer + 1) % SUPPLY_QUEUE_LENGTH);
	}
	
	public static int popSupplyQueue() throws GameActionException {
		int front_pointer = rc.readBroadcast(SUPPLY_QUEUE_FRONT_POINTER);
		int ID = rc.readBroadcast(front_pointer + SUPPLY_QUEUE_START);
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
		while(signal != 0) {
			rc.broadcast(front_pointer + MINING_QUEUE_FRONT_POINTER, 0);
			rc.broadcast(MINING_QUEUE_FRONT_POINTER, (front_pointer + 1) % MINING_QUEUE_LENGTH);
			int x = (signal % 1000) + hq.x - 500;
			int y = ((signal / 1000) % 1000) + hq.y - 500; 
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
		return null;
	}

	public static void claimMiningLocation(MapLocation mining_location) throws GameActionException {
		if(mining_location == null) return; 
		int channel = getChannelFromLocation(mining_location);
		int signal = rc.readBroadcast(channel);
		rc.broadcast(channel, signal + 1000*1000);
	}
}
