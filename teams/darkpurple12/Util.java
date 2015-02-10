package darkpurple12;

import battlecode.common.MapLocation;
import java.util.*;

public class Util {
	
	private static final Random rand = new Random();

	public static MapLocation[] shuffleMapLocation(MapLocation[] m) {
		for(int i=0; i < m.length; i++) {
			int sub = rand.nextInt(m.length);
			MapLocation temp = m[sub];
			m[sub] = m[i];
			m[i] = temp;
		}
		return m;
	}
	
}
