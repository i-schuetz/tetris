import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * @author schuetz
 */
public class Piece {
	
	private PieceType type;
	private final Point startPoint;
	private Point centralPoint;
	private List<Point> startPositions;
	private List<Point> currentPositions;
	private List<List<Point>> rotations = new ArrayList<List<Point>>();
	private int currentRotation = 0;
	
	private Direction testDirection;
	private Point testCentralPoint;
	private List<Point> testPositions;
	
	private List<Point> testRotation;
	
	Piece(PieceType type, Point startPoint) {
		this.type = type;
		this.startPoint = startPoint;
		centralPoint = new Point(startPoint.x, startPoint.y);
		testCentralPoint = new Point(centralPoint.x, centralPoint.y);
		testPositions = new ArrayList<Point>();
		initRotations();
		initPositions();
	}
	
	void restart() {
		centralPoint = new Point(startPoint.x, startPoint.y);
		currentRotation = 0;
		resetCurrentPositions();
	}
	
	Point getCentralPoint() {
		return centralPoint;
	}
	
	private void resetCurrentPositions() {
		currentPositions.clear();
		testPositions.clear();
		Point point;
		for(int i = 0; i < startPositions.size(); i++) {
			point = startPositions.get(i);
			currentPositions.add(point);
		}
	}
	
	void initRotations() {
		switch(type) {
		case I:
			rotations.add(Arrays.asList(new Point[]{new Point(0, -2), new Point(0, -1), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, 0), new Point(1, 0), new Point(2, 0)}));
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(0, 1), new Point(0, 2)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-2, 0), new Point(-1, 0), new Point(1, 0)}));
			break;
		case J:
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(-1, 1), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, -1), new Point(-1, 0), new Point(1, 0)}));
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(1, -1), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, 0), new Point(1, 0), new Point(1, 1)}));
			break;
		case L:
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(0, 1), new Point(1, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, 0), new Point(1, 0), new Point(-1, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, -1), new Point(0, -1), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, 0), new Point(1, 0), new Point(1, -1)}));
			break;
		case O:
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(1, -1), new Point(1, 0)}));
			break;
		case S:
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(-1, 0), new Point(-1, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, -1), new Point(0, -1), new Point(1, 0)}));
			rotations.add(Arrays.asList(new Point[]{new Point(1, -1), new Point(1, 0), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, 0), new Point(0, 1), new Point(1, 1)}));
			break;
		case T:
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(1, 0), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, 0), new Point(1, 0), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(-1, 0), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(-1, 0), new Point(1, 0)}));
			break;
		case Z:
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(1, 0), new Point(1, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(1, 0), new Point(-1, 1), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(-1, -1), new Point(-1, 0), new Point(0, 1)}));
			rotations.add(Arrays.asList(new Point[]{new Point(0, -1), new Point(1, -1), new Point(-1, 0)}));
			break;
		}
	}
	
	void initPositions() {
		startPositions = calculateNewPoints(currentRotation);
		currentPositions = new ArrayList<Point>();
		Point point;
		for(int i = 0; i < startPositions.size(); i++) {
			point = startPositions.get(i);
			currentPositions.add(point);
		}
	}
	
	List<Point> testRotate() {
		int rotation = currentRotation + 1;
		rotation = rotation == rotations.size() ? 0 : rotation;
		testRotation = calculateNewPoints(rotation);
		testCentralPoint = new Point(centralPoint.x, centralPoint.y);
		return testRotation;
	}
	
	void rotate() {
		currentPositions.clear();
		currentRotation = currentRotation == rotations.size() - 1 ? 0 : currentRotation + 1;
		Point point;
		for(int i = 0; i < testRotation.size(); i++) {
			point = testRotation.get(i);
			currentPositions.add(new Point(point.x, point.y));
		}
	}
	
	private List<Point> calculateNewPoints(int rotation) {
		List<Point> newPoints = new ArrayList<Point>();
		if (rotation < rotations.size()) {
			List<Point> offsets = rotations.get(rotation);
			Point point;
			for(int i = 0; i < offsets.size(); i++) {
				point = offsets.get(i);
				newPoints.add(new Point(centralPoint.x + point.x, centralPoint.y + point.y));
			}
		}
		newPoints.add(new Point(centralPoint.x, centralPoint.y));
		return newPoints;
	}

	List<Point> testMove(Direction direction) {
		testDirection = direction;
		testPositions.clear();
		int offset_x = 0;
		int offset_y = 0;
		switch (direction) {
			case down: offset_y = 1; break;
			case left: offset_x = -1; break;
			case right: offset_x = 1; break;
//			default: throw new RuntimeException("Not handled direction");
		}
		Point point;
		for(int i = 0; i < currentPositions.size(); i++) {
			point = currentPositions.get(i);
			testPositions.add(new Point(point.x + offset_x, point.y + offset_y));
		}
		testCentralPoint.x = centralPoint.x + offset_x;
		testCentralPoint.y = centralPoint.y + offset_y;
		
		return testPositions;
	}
	
	void move(Direction direction) {
		if (direction != testDirection) {
//			throw new RuntimeException("Direction to move not tested:" + direction + " " + testDirection);
			System.err.println("Direction to move not tested: direction: " + direction + " testdirection: " + testDirection);
		}
		centralPoint.x = testCentralPoint.x;
		centralPoint.y = testCentralPoint.y;
		currentPositions.clear();
		Point p;
		for (int i = 0; i < testPositions.size(); i++) {
			p = testPositions.get(i);
			currentPositions.add(new Point(p.x, p.y));
		}
	}
	
	List<Point> getPositions() {
		return currentPositions;
	}
	
	List<Point> getInitPositions() {
		return rotations.get(0);
	}
	
	PieceType getType() {
		return type;
	}
}