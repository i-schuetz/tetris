/**
 * @author schuetz
 */
public enum Removed {
	
	Single(10), Double(30), Triple(120), Tetris(600);
	
	private final int points;
	
	Removed(int points) {
		this.points = points;
	}
	
	int getPoints() {
		return points;
	}
}