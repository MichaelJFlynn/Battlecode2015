package team372;

import java.util.Comparator;
import java.util.PriorityQueue;

import battlecode.common.*;

public class Skirmisher {
	
	static BattleInfo[] battleInfos;
	static RobotController rc;
	static Team myTeam;
	static Team enemyTeam;
	static MapLocation myLoc;
	static RobotInfo[] friends;
	static RobotInfo[] enemies;
	static MapLocation[] towers;
	static final Direction[] directions = new Direction[] { Direction.NONE, Direction.NORTH, Direction.NORTH_EAST,
		Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
		Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	private static final Comparator<BattleInfo> BattleInfoComparitor = new Comparator<BattleInfo>() {

		@Override
		public int compare(BattleInfo a, BattleInfo b) {
			return (int) (scoreBattleInfo(b) - scoreBattleInfo(a));
		}
		
	};
	static final double INCOMING_DAMAGE_MODIFIER = 1;
	static final double TOWER_DAMAGE_MODIFIER = 3;
	static MapLocation goalLoc;
	static boolean supplied;
	static Direction best_dir;
	
	public static void init(RobotController theRC) {
		rc = theRC;
		myTeam = rc.getTeam();
		enemyTeam = myTeam.opponent();
		goalLoc = null;
	}
	
	
	public static void setGoalLoc(MapLocation m) {
		goalLoc = m;
	}
	
	public static void battlecode() throws GameActionException {
			myLoc = rc.getLocation();
			Direction currentMove = Direction.NONE;
			supplied = rc.getSupplyLevel() > 0;

			battleInfos = getBattleInfoForEachPossibleMove();
			currentMove = tryToMakeBestMove();
			rc.setIndicatorString(1, "Best Dir: " + best_dir + ", score: " + scoreBattleInfo(battleInfos[best_dir.ordinal()]) + " Moving: " + currentMove + ", score: " + scoreBattleInfo(battleInfos[currentMove.ordinal()]));
			rc.setIndicatorString(0, "Receivable: " + battleInfos[currentMove.ordinal()].POTENTIAL_DAMAGE_RECEIVED());
			tryToAttackBestTarget(currentMove);
	}
	
	
	public static void battlecodeDrone() throws GameActionException {
		myLoc = rc.getLocation();
		Direction currentMove = Direction.NONE;
		battleInfos = getBattleInfoForEachPossibleMove();
		
		currentMove = tryToMakeBestMove();
		rc.setIndicatorString(1, "Best Dir: " + best_dir + ", score: " + scoreBattleInfo(battleInfos[best_dir.ordinal()]) + " Moving: " + currentMove + ", score: " + scoreBattleInfo(battleInfos[currentMove.ordinal()]));
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

		if(rc.canMove(best_dir) && rc.isPathable(rc.getType(), rc.getLocation().add(best_dir))) {
			rc.move(best_dir);
			return best_dir;
		}
		
		
		return Direction.NONE;
	}
	
	private static Direction tryToMakeBestMoveDrone() throws GameActionException {
		if(!rc.isCoreReady()) {
			return Direction.NONE;
		}
		PriorityQueue<BattleInfo> pQ = new PriorityQueue<BattleInfo>(directions.length, BattleInfoComparitor);
		for(BattleInfo info : battleInfos) {
			pQ.add(info);
		}
		for(BattleInfo info : pQ) {
			if(rc.canMove(info.moveDirection()) && rc.isPathable(rc.getType(), rc.getLocation().add(info.moveDirection()))) {
				rc.move(info.moveDirection());
				return info.moveDirection();
			}
		}
		
		return Direction.NONE;
	}
	
	private static BattleInfo[] getBattleInfoForEachPossibleMove() throws GameActionException {
		BattleInfo[] infos = new BattleInfo[directions.length];
		enemies = rc.senseNearbyRobots(myLoc, 35, rc.getTeam().opponent());
		towers = rc.senseEnemyTowerLocations();
		best_dir = Direction.NONE;
		myLoc = rc.getLocation(); 
		if(enemies.length > 0) {
			friends = rc.senseNearbyRobots(enemies[0].location, 35, myTeam);
		} else {
			friends = rc.senseNearbyRobots(35, myTeam);
		}
		double best_value = -9999999;
		
		double enemy_health = 0;
		double enemy_damage = 0;
		double friend_health = rc.getHealth();
		double friend_damage = rc.getType().attackPower/rc.getType().attackDelay; 
		
		for(RobotInfo friend : friends) {
			if(friend.type != RobotType.TOWER && friend.type != RobotType.HQ) {  
				friend_health += friend.health;
				friend_damage += friend.type.attackPower/friend.type.attackDelay;
			}
		}
		
		for(RobotInfo enemy : enemies) {
			if(enemy.type == RobotType.TOWER || enemy.type == RobotType.HQ ) continue;
			enemy_health += enemy.health;
			enemy_damage += enemy.type.attackPower/enemy.type.attackDelay;
		}
		
		for(MapLocation tower : towers) {
			if(myLoc.distanceSquaredTo(tower) <= RobotType.TOWER.attackRadiusSquared + 
					2*Math.sqrt(RobotType.TOWER.attackRadiusSquared) + 1) {
				enemy_health += RobotType.TOWER.maxHealth;
				enemy_damage += RobotType.TOWER.attackPower/RobotType.TOWER.attackDelay;
			}
		}
		
		boolean winning = 2*enemy_damage * enemy_health < friend_health * friend_damage; 
		rc.setIndicatorString(2, "Our dh: " + friend_health * friend_damage + ", their dh: " + enemy_health * enemy_damage + ", winning: "  + winning);
		
		if(rc.getType() == RobotType.COMMANDER) {
			if(enemies.length > 0) {
				if(Mover.tryToFlash(enemies[0].location)) {
					myLoc = rc.getLocation();
				}
			}
		}
		
		
		double[] prime_givable = new double[enemies.length];
		for(int j=0; j < enemies.length; j++) {
			for(RobotInfo friend : friends) {
				if(enemies[j].location.distanceSquaredTo(friend.location)  <= friend.type.attackRadiusSquared) { 
					prime_givable[j] += friend.type.attackPower/friend.type.attackDelay;
				}	
			}
		}
		
		if(myLoc.distanceSquaredTo(rc.senseEnemyHQLocation()) <= RobotType.HQ.attackRadiusSquared + 2*Math.sqrt(RobotType.HQ.attackRadiusSquared) + 1) {
			enemy_health += RobotType.HQ.maxHealth;
			enemy_damage += RobotType.HQ.attackPower/RobotType.HQ.attackDelay;
		}
		
		
		for(Direction dir : directions) {
			if(!(rc.canMove(dir) || dir == Direction.NONE)) continue;
			// location of move
			MapLocation m = myLoc.add(dir);
			// whether the terrain is passable
			//boolean passable = rc.senseTerrainTile(m).isTraversable();
			
			// find the possible damage received
			double givable[] = prime_givable.clone();
			//double givable[] = new double[enemies.length];
			double receivable = 0;
			int kills = 0;
			for(int i=0; i < enemies.length; i++) {
				RobotInfo robot = enemies[i];
				
				// how much damage can he do to us
				double distanceSquaredToUs = robot.location.distanceSquaredTo(m);
				if(robot.type != RobotType.TOWER && robot.type != RobotType.HQ && robot.type != RobotType.MISSILE) { // these cases handled elsewhere
					if(!winning || rc.getType() != RobotType.COMMANDER) {
						if(distanceSquaredToUs <= robot.type.attackRadiusSquared) {
							receivable += robot.type.attackPower/robot.type.attackDelay;
						}
					} else {
						if(distanceSquaredToUs > 0) {
							receivable += distanceSquaredToUs/1000;
						}
					}
				}
				if(robot.type == RobotType.LAUNCHER) {
					// add launcher "tiebreaker", we want to be closer
					receivable += distanceSquaredToUs/100;
				}
				
				if(robot.type == RobotType.MISSILE) {
					if(distanceSquaredToUs <= 8) {
						receivable += robot.type.attackPower;
					}
				}
				
				
				if (distanceSquaredToUs <= rc.getType().attackRadiusSquared) {
					givable[i] += rc.getType().attackPower/rc.getType().attackDelay;		
				} else {
					givable[i] = 0;
				}
				if(givable[i] >= robot.health){
					kills++; 
				}
			}
			// add tower stuff
			for(MapLocation tower : towers) {
				if(!winning) {
					if(m.distanceSquaredTo(tower) < RobotType.TOWER.attackRadiusSquared) {
						receivable += RobotType.TOWER.attackPower/RobotType.TOWER.attackDelay;
					}
				} else {
					if(m.distanceSquaredTo(tower) < 36) {
						receivable += m.distanceSquaredTo(tower);
					}
				}
			}
			
			int distanceToHQ = m.distanceSquaredTo(rc.senseEnemyHQLocation());
			if(distanceToHQ <= 35) {
				if(!winning){
					receivable += RobotType.HQ.attackPower/RobotType.HQ.attackDelay;
				} else {
					receivable += m.distanceSquaredTo(rc.senseEnemyHQLocation());
				}
			} 
			
			
			// if this amount of damage could kill the robot
			boolean canDie = false;
			if(receivable >= rc.getHealth()) {
				canDie = true;
			}
			infos[dir.ordinal()] = new BattleInfo(receivable, givable, kills, canDie, winning, false, friends, enemies, dir);
			double value = scoreBattleInfo(infos[dir.ordinal()]);
			if(value > best_value) {
				best_dir = dir;
				best_value = value; 
			}
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
		
		double maxDamage = 0;
		for(double damages : info.POTENTIAL_DAMAGE_DONE()) {
			if(damages > maxDamage) {
				maxDamage = damages;
			}
		}
		
		/*
		if(info.POTENTIAL_DAMAGE_DONE().length == 0) {
			minDamage = 0;
		}*/
		
		//rc.setIndicatorString(0, "MaxDamage: " + maxDamage);
		if(info.winning())
			value += maxDamage;
		
		//if(info.towerDamage()) {
			//value += 1000;
		//}
		
		if(!(goalLoc == null)) {
			value += (myLoc.distanceSquaredTo(goalLoc) - myLoc.add(info.moveDirection()).distanceSquaredTo(goalLoc))/100000;			
		}
		
		return value; 
	}


	public static void moveWithoutAttacking(MapLocation[] towers, RobotInfo[] enemies) throws GameActionException {
		myLoc = rc.getLocation();
		double lowest_damage = 9999999;
		for(Direction dir : directions) {
			if(!(rc.canMove(dir) || dir == Direction.NONE)) continue;
			// location of move
			MapLocation m = myLoc.add(dir);

			//boolean passable = rc.senseTerrainTile(m).isTraversable();
			double receivable = 0;
			
			for(int i=0; i < enemies.length; i++) {
				RobotInfo robot = enemies[i];
				
				// how much damage can he do to us
				double distanceSquaredToUs = robot.location.distanceSquaredTo(m);
				if(distanceSquaredToUs <= robot.type.attackRadiusSquared) {
					receivable += robot.type.attackPower/robot.type.attackDelay;
				}	
			}
			// add tower stuff
			for(MapLocation tower : towers) {
				if(m.distanceSquaredTo(tower) < RobotType.TOWER.attackRadiusSquared) {
					receivable += RobotType.TOWER.attackPower/RobotType.TOWER.attackDelay;
				}
			}	
		
			
			int distanceToHQ = m.distanceSquaredTo(rc.senseEnemyHQLocation());
			if(distanceToHQ <= RobotType.HQ.attackRadiusSquared + 2*Math.sqrt(RobotType.HQ.attackRadiusSquared) + 1) {
				receivable += RobotType.HQ.attackPower/RobotType.HQ.attackDelay;
			} 
			
			if(goalLoc != null) { 
				receivable += m.add(dir).add(dir).add(dir).distanceSquaredTo(goalLoc)/10000;
			}
			
			if(lowest_damage > receivable) {
				lowest_damage = receivable;
				best_dir = dir;
			}
		}

		tryToMakeBestMove();
	}


	public static boolean tryToAttackTower(MapLocation[] towers,
			MapLocation myLoc) throws GameActionException {
		if(!rc.isCoreReady()) return false;
		MapLocation closest_tower = rc.senseEnemyHQLocation();
		if(towers.length > 0) {
			closest_tower = Tank.findClosestTo(towers, myLoc);
		} 
		RobotInfo[] friends = rc.senseNearbyRobots(closest_tower, 35, myTeam);
		RobotInfo[] enemies = rc.senseNearbyRobots(closest_tower, 35, enemyTeam);
		double friend_health = 0, friend_damage = 0, enemy_health = 0, enemy_damage = 0; 
		friend_health += rc.getHealth();
		friend_damage += rc.getType().attackPower/rc.getType().attackDelay;
		for(RobotInfo friend : friends) {
			friend_health += friend.health;
			friend_damage += friend.type.attackPower/friend.type.attackDelay;
		}
		for(RobotInfo enemy : enemies) {
			enemy_health += enemy.health;
			enemy_damage += enemy.type.attackPower/enemy.type.attackDelay;
		}
		if(!rc.canSenseLocation(closest_tower)) {
			enemy_health += RobotType.TOWER.maxHealth;
			enemy_damage += RobotType.TOWER.attackPower/RobotType.TOWER.attackDelay;
		}
		if(friend_damage * friend_health > 2*enemy_health * enemy_damage) {
			rc.setIndicatorString(0, "Moving to tower, " + Clock.getRoundNum() +" our dh: " + friend_health * friend_damage + ", their dh: " + enemy_health * enemy_damage);
			Direction dir = myLoc.directionTo(closest_tower);
			if(rc.canMove(dir)) {
				rc.move(dir);
				return true;
			}
			Direction dirLeft = dir.rotateLeft();
			if(rc.canMove(dirLeft)) {
				rc.move(dirLeft);
				return true;
			}
			Direction dirRight = dir.rotateRight();
			if(rc.canMove(dirRight)) {
				rc.move(dirRight);
				return true;
			}
		} else {
			rc.setIndicatorString(0, "Dh not high enough, " + Clock.getRoundNum() +" our dh: " + friend_health * friend_damage + ", their dh: " + enemy_health * enemy_damage);
		}
		rc.setIndicatorString(1, "Couldn't move to tower " + closest_tower);
		return false;
	}

}
