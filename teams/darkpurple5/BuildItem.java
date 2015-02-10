package darkpurple5;

import battlecode.common.*;

public class BuildItem {

	private RobotType type;
	private int quantity;
	
	public BuildItem(RobotType p_type, int p_quantity) {
		type = p_type;
		quantity = p_quantity;
	}
	
	public RobotType type() {
		return type;
	}
	
	public int quantity() {
		return quantity;
	}
	
}
