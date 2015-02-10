package darkpurple1;

import battlecode.common.*;

public class BuildItem {

	private RobotType type;
	private int quantity;
	private boolean parallel;
	
	public BuildItem(RobotType p_type, int p_quantity, boolean p_parallel) {
		type = p_type;
		quantity = p_quantity;
		parallel = p_parallel;
	}
	
	public RobotType type() {
		return type;
	}
	
	public int quantity() {
		return quantity;
	}
	
	public boolean parallel() {
		return parallel;
	}
}
