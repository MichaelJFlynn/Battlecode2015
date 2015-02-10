package darkpurple1;

import java.util.Comparator;
import java.util.PriorityQueue;

import battlecode.common.*;

public class Skirmisher {
	
	static BattleInfo[] battleInfos;
	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;
	static MapLocation myLoc;
	static final Direction[] directions = new Direction[] {Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST, Direction.NONE};
	private static final Comparator<BattleInfo> BattleInfoComparitor = new Comparator<BattleInfo>() {

		@Override
		public int compare(BattleInfo a, BattleInfo b) {
			return (int) (scoreBattleInfo(b) - scoreBattleInfo(a));
		}
		
	};
	static final double INCOMING_DAMAGE_MODIFIER = 1;
	static final double TOWER_DAMAGE_MODIFIER = 3;
	
	public static void init(RobotController theRC) {
		rc = theRC;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
	}
	
	
	public static void battlecode() throws GameActionException {
		myLoc = rc.getLocation();
		Direction currentMove = Direction.NONE;
		battleInfos = getBattleInfoForEachPossibleMove();
		
		currentMove = tryToMakeBestMove();
		//rc.setIndicatorString(1, "Moving: " + currentMove + ", score: " + scoreBattleInfo(battleInfos[currentMove.ordinal()]));
		//rc.setIndicatorString(2, "Enemies: " + battleInfos[currentMove.ordinal()].enemies());
		tryToAttackBestTarget(currentMove);
		
	}
	
	private static void tryToAttackBestTarget(Direction currentMove) throws GameActionException {
		BattleInfo info = battleInfos[currentMove.ordinal()];
		RobotInfo[] enemies = info.enemies();
		if(enemies.length == 0) return; 
		double[] damages = info.POTENTIAL_DAMAGE_DONE();
		double maxDamage = 0;
		int bestIndex = 0;
		for(int i=0; i < enemies.length; i++) {
			if(maxDamage < damages[i]) {
				maxDamage = damages[i];
				bestIndex = i;
			}
		}
		MapLocation attackLocation = enemies[bestIndex].location;
		if(maxDamage > 0 && rc.isWeaponReady() && rc.canAttackLocation(attackLocation)) {
			//rc.setIndicatorString(0, "Shooting: " + attackLocation);
			rc.attackLocation(attackLocation);
		}
		
	}
	
	private static Direction tryToMakeBestMove() throws GameActionException {
		if(!rc.isCoreReady()) {
			return Direction.NONE;
		}
		PriorityQueue<BattleInfo> pQ = new PriorityQueue<BattleInfo>(directions.length, BattleInfoComparitor);
		for(BattleInfo info : battleInfos) {
			pQ.add(info);
		}
		for(BattleInfo info : pQ) {
			if(scoreBattleInfo(info) <= scoreBattleInfo(battleInfos[Direction.NONE.ordinal()])) {
				return Direction.NONE;
			}
			if(rc.canMove(info.moveDirection())) {
				rc.move(info.moveDirection());
				return info.moveDirection();
			}
		}
		
		return Direction.NONE;
	}
	
	private static BattleInfo[] getBattleInfoForEachPossibleMove() {
		BattleInfo[] infos = new BattleInfo[directions.length];
		RobotInfo[] friends = rc.senseNearbyRobots(myLoc, rc.getType().sensorRadiusSquared, myTeam);
		RobotInfo[] enemies = rc.senseNearbyRobots(myLoc, rc.getType().sensorRadiusSquared, enemyTeam);
		MapLocation[] towers = rc.senseEnemyTowerLocations();
		for(Direction dir : directions) {
			// location of move
			MapLocation m = myLoc.add(dir);
			// whether the terrain is passable
			boolean passable = rc.senseTerrainTile(m).isTraversable();
			
			// find the possible damage received
			double givable[] = new double[enemies.length];
			double receivable = 0;
			int kills = 0;
			for(int i=0; i < enemies.length; i++) {
				RobotInfo robot = enemies[i];
				givable[i] = 0;
				// how much damage can he do to us
				double distanceSquaredToUs = robot.location.distanceSquaredTo(m);
				if(distanceSquaredToUs <= robot.type.attackRadiusSquared) {
					receivable += robot.type.attackPower;
				}
				
				if (distanceSquaredToUs <= rc.getType().attackRadiusSquared) {
					givable[i] += rc.getType().attackPower;		
					for(RobotInfo friend : friends) {
						if(robot.location.distanceSquaredTo(friend.location)  <= friend.type.attackRadiusSquared){ 
							givable[i] += friend.type.attackPower;
						}	
					}
				}
				if(givable[i] >= robot.health){
					kills++; 
				}
			}
			// add tower stuff
			boolean towerDamage = true; 
			for(MapLocation tower : towers) {
				int distanceTo = m.distanceSquaredTo(tower);
				if(distanceTo <= RobotType.TOWER.attackRadiusSquared) {
					receivable += RobotType.TOWER.attackPower * TOWER_DAMAGE_MODIFIER;
				} 
				if(distanceTo <= rc.getType().attackRadiusSquared) {
					towerDamage = true;
				}
			}
			
			int distanceToHQ = m.distanceSquaredTo(rc.senseEnemyHQLocation());
			if(distanceToHQ <= RobotType.HQ.attackRadiusSquared) {
				receivable += RobotType.HQ.attackPower;
			}
			
			
			// if this amount of damage could kill the robot
			boolean canDie = false;
			if(receivable >= rc.getHealth()) {
				canDie = true;
			}
			infos[dir.ordinal()] = new BattleInfo(receivable, givable, kills, canDie, passable, towerDamage, friends, enemies, dir);
		}
		return infos;
	}
	
	public static double scoreBattleInfo(BattleInfo info) { 
		double value = 0;
		
		if(info.POTENTIAL_DEATH()) {
			value -= 10000;
		}
		
		value -= info.POTENTIAL_DAMAGE_RECEIVED()*INCOMING_DAMAGE_MODIFIER;
		//value += info.POTENTIAL_KILLS()*10;
		
		double minDamage = 99999;
		for(double damages : info.POTENTIAL_DAMAGE_DONE()) {
			if(damages < minDamage) {
				minDamage = damages;
			}
		}
		value += minDamage;
		
		if(info.towerDamage()) {
			value += 1000;
		}
		
		return value; 
	}

}
