import java.awt.Point;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

/** 
 * @author schuetz
 */

public class Game implements Runnable {

	public static void main(String[] args) {
		JFrame frameDisplay = new JFrame();
		new Thread(new Game(new GUI(frameDisplay))).start();
	}
	
	private GUI gui;
	volatile private boolean running = true;

	static final int ROWS = 21;
	static final int COLS = 11;
	
	private static final String DEFAULT_PLAYER_NAME = "Anonymous";
	private String playerName = DEFAULT_PLAYER_NAME;
	
	private Piece fallingPiece;
	private Piece nextPiece;
	
	private PieceType[][] occupiedFields = new PieceType[ROWS][COLS];
	
	private Random random;

	private Piece[] pieces = new Piece[PieceType.values().length];
	
	private static final Point START_POINT = new Point(COLS/2, 0);
	
	volatile private boolean pause;
	private boolean terminated;
	
	private int level;
	private int score;
	private int removeCounter;
	private static final int REMOVES_TO_LEVEL = 6;
	private static final int MAX_LEVEL = 9;
	private static final int[] levelDelays = new int[] {700, 650, 600, 550, 500, 450, 400, 300, 200, 100};
//	private static final int[] levelDelays = new int[] {700, 700, 700, 700}; //for tests

	private static final Removed[] removedArr = {Removed.Single, Removed.Double, Removed.Triple, Removed.Tetris};
	
	private boolean useScore = false; //TODO make scores work in applet
	private ScoreDAO scoreDAO;
	
	private int sameTypeCounter = 0;

	private static final String BG_MUSIC = "Tetrisb.mid";
	private static final String WIN_TRACK = "win.mid";
	private static final String GAME_OVER_TRACK = "wbgover.mid";
	
	private boolean shutDownMidisLoader = false;
	private MidisLoader midisLoader;
	private SoundsWatcher soundsWatcher;
	private String nextTrackToPlay;
	private boolean nextTrackLoop;
	
	Game(GUI gui) {
		this.gui = gui;
	}

	public void run() {
		initScore();
		initRandom();
		initPieces();
		initGui();
		initSound();
		setRandomNextPiece();
		startPiece();
		while(running) {
			if (!pause) {
				delay(levelDelays[level]);
				if (!movePiece(Direction.down)) {
					addPieceToBottom();
					startPiece();
				}
				gui.render(occupiedFields);
			}
			try {
				Thread.sleep(20);
			}
			catch (InterruptedException e) { }
		}
		safeExit(); //FIXME necessary? quick solution for applet
	}
	
	private void safeExit() {
		gui = null;
		fallingPiece = null;
		nextPiece = null;
		random = null;
		occupiedFields = null;
		pieces = null;
		scoreDAO = null;
		shutDownMidisLoader = true;
		midisLoader.stop();
		soundsWatcher = null;
		nextTrackToPlay = null;
	}

	private void initScore() {
		if (useScore) {
			scoreDAO = new ScoreDAO();
		}
	}

	private void initRandom() {
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} 
		catch (NoSuchAlgorithmException e) {
			random = new Random();
		}
	}

	private void initSound() {
		midisLoader = new MidisLoader();
		midisLoader.load(BG_MUSIC, BG_MUSIC);
		midisLoader.load(GAME_OVER_TRACK, GAME_OVER_TRACK);
		midisLoader.load(WIN_TRACK, WIN_TRACK);
		midisLoader.play(BG_MUSIC, true);
		soundsWatcher = new SoundsWatcher() {
			 public void atSequenceEnd(String filename, int status) {
				 if (status == SoundsWatcher.STOPPED) {
					 if (shutDownMidisLoader == true) {
						 midisLoader = null;
					 } else if (nextTrackToPlay != null) {
						 midisLoader.play(nextTrackToPlay, nextTrackLoop);
						 nextTrackToPlay = null;
					 }
				 }
			 }
		};
		midisLoader.setWatcher(soundsWatcher);
	}

	private void delay(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) { }
	}
	
	void initGui() {
		Map<PieceType, List<Point>> initPositions = new HashMap<PieceType, List<Point>>();
		Piece piece;
		for (int i = 0; i < pieces.length; i++) {
			piece = pieces[i];
			initPositions.put(piece.getType(), piece.getInitPositions());
		}
		gui.init(this, initPositions, DEFAULT_PLAYER_NAME);
	}
	
	private boolean movePiece(Direction direction) {
		List<Point> currentPositions = fallingPiece.getPositions();
		List<Point> testPoints = fallingPiece.testMove(direction);
		
		if (!canMovePiece(direction, currentPositions, testPoints)) {
			return false;
		}
		
		clearPositions(currentPositions);
		
		fallingPiece.move(direction);
		PieceType type = fallingPiece.getType();
		Point point;
		for (int i = 0; i < testPoints.size(); i++) {
			point = testPoints.get(i);
			if (point.y >= 0) {
				occupiedFields[point.y][point.x] = type;
			}
		}
		return true;
	}
	
	private boolean canMovePiece(Direction direction) {
		List<Point> currentPositions = fallingPiece.getPositions();
		List<Point> testPoints = fallingPiece.testMove(direction);
		Point point;
		for (int i = 0; i < testPoints.size(); i++) {
			point = testPoints.get(i);
			if (point.x >= COLS || point.y >= ROWS || point.x < 0
					|| (!currentPositions.contains(point) && point.x >= 0 && point.y >= 0 && occupiedFields[point.y][point.x] != null)
					) {
				return false;
			}
		}
		return true;
	}
	
	private boolean canMovePiece(Direction direction, List<Point> currentPositions, List<Point> testPoints) {
		Point point;
		for (int i = 0; i < testPoints.size(); i++) {
			point = testPoints.get(i);
			if (point.x >= COLS || point.y >= ROWS || point.x < 0
					|| (!currentPositions.contains(point) && point.x >= 0 && point.y >= 0 && occupiedFields[point.y][point.x] != null)
					) {
				return false;
			}
		}
		return true;
	}
	
	private void rotatePiece() {
		List<Point> currentPoints = fallingPiece.getPositions();
		List<Point> testPoints = fallingPiece.testRotate();
		Point point;
		for (int i = 0; i < testPoints.size(); i++) {
			point = testPoints.get(i);
			if (point.x >= COLS || point.y >= ROWS || point.x < 0
					|| (!currentPoints.contains(point) && point.x >= 0 && point.y >= 0 && occupiedFields[point.y][point.x] != null)
					) {
				return;
			}
		}
		clearPositions(currentPoints);
		fallingPiece.rotate();
		PieceType type = fallingPiece.getType();
		for (int i = 0; i < testPoints.size(); i++) {
			point = testPoints.get(i);
			if (point.y >= 0) {
				occupiedFields[point.y][point.x] = type;
			}
		}
	}
	
	private void clearPositions(List<Point> positions) {
		Point p;
		for (int i = 0; i < positions.size(); i++) {
			p = positions.get(i);
			if (p.x >= 0 && p.x < COLS && p.y >= 0 && p.y < ROWS) { //TODO checkbounds - gehts besser?
				occupiedFields[p.y][p.x] = null;
			}
		}
	}
	
	private void addPieceToBottom() {
		Point p;
		List<Point> positions = fallingPiece.getPositions();
		for (int i = 0; i < positions.size(); i++) {
			p = positions.get(i);
			if (p.x >= 0 && p.x < COLS && p.y >= 0 && p.y < ROWS) { //TODO checkbounds - gehts besser?
				occupiedFields[p.y][p.x] = fallingPiece.getType();
			}
		}
		removeRowsIfPossible();
	}

	private void removeRowsIfPossible() {
		boolean rowFull;
		int removedRows = 0;
		for (int row = 0; row < ROWS; row++) {
			rowFull = true;
			for (int col = 0; col < COLS; col++) {
				if (occupiedFields[row][col] == null) {
					rowFull = false;
				}
			}
			if (rowFull) {
				for (int col = 0; col < COLS; col++) {
					occupiedFields[row][col] = null;
				}
				for (int upperRow = row - 1; upperRow > 0; upperRow--) {
					for (int col = 0; col < COLS; col++) {
						occupiedFields[upperRow + 1][col] = occupiedFields[upperRow][col];
						occupiedFields[upperRow][col] = null;
					}
				}
				removedRows++;
			}
		}
		if (removedRows > 0) {
			removeCounter++;
			Removed removed = removedArr[removedRows - 1];
			int points = level * 100 / 2;
			score += removed.getPoints() + points;
			gui.showRemoved(removed, points);
			if (removeCounter == REMOVES_TO_LEVEL) {
				if (level == MAX_LEVEL) {
					win();
				} else {
					levelUp();
					removeCounter = 0;
				}
			}
			gui.showScore(score);
		}
	}

	private void levelUp() {
			level++;
			gui.showLevel(level);
	}
	
	private void win() {
		score += 10000;
		setPause(true);
		terminated = true;
		gui.showWin();
		playTrack(WIN_TRACK, true);
		if (useScore) {
			saveScore();
		}
	}
	
	private void lose() {
		setPause(true);
		terminated = true;
		gui.showLose();
		playTrack(GAME_OVER_TRACK, false);
		if (useScore) {
			saveScore();
		}
	}
	
	private void saveScore() {
		try {
			scoreDAO.saveScore(playerName, level, score);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//FIXME maybe better showScores and method call to gui to open score dialog
	public String getScores() {
		String scores = null;
		try {
			scores = scoreDAO.getScores();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scores;
	}
	
	private void initPieces() {
		PieceType[] pieceTypes = PieceType.values();
		for (int i = 0; i < pieceTypes.length; i++) {
			pieces[i] = new Piece(pieceTypes[i], START_POINT);
		}
	}
	
	private void startPiece() {
		fallingPiece = nextPiece;
		fallingPiece.restart();
		if(!canMovePiece(Direction.down)) {
			lose();
		}
		setRandomNextPiece();
	}

	private void setRandomNextPiece() {
		Piece piece = pieces[random.nextInt(pieces.length)];
		if (fallingPiece != null && piece.getType() == fallingPiece.getType()) {
			if (sameTypeCounter == 1) {
				while (piece.getType() == fallingPiece.getType()) {
					piece = pieces[random.nextInt(pieces.length)];
				}
				sameTypeCounter = 0;
			} else {
				sameTypeCounter++;
			}
		}
		nextPiece = piece;
		gui.showNextPiece(nextPiece.getType());
	}

	void setPause(boolean pause) {
		if (!terminated) { //FIXME quick solution to avoid applet exception
			this.pause = pause;
		}
	}
	
	public void processAction(Action action) {
		if (action == Action.pause) {
			setPause(!pause);
		}
		if (!pause) {
			switch (action) {
				case move_left: movePiece(Direction.left); break;
				case move_right: movePiece(Direction.right); break;
				case move_down: movePiece(Direction.down); movePiece(Direction.down); break;
				case rotate: rotatePiece(); break;
			}
			gui.render(occupiedFields);
		}
	}

	public void quit() {
		running = false;
	}

	public void restart() {
		for (int i = 0; i < ROWS; i++) {
			Arrays.fill(occupiedFields[i], null);
		}
		gui.setFrozen(false);
		gui.setDefaultColors();
		gui.render(occupiedFields);
		gui.showPlayerName(playerName);
		level = 0;
		gui.showLevel(0);
		score = 0;
		gui.showScore(0);
		removeCounter = 0;
		playTrack(BG_MUSIC, true);
		terminated = false;
		setPause(false);
		setRandomNextPiece();
		startPiece();
	}

	private void playTrack(String name, boolean loop) {
		nextTrackToPlay = name;
		nextTrackLoop = loop;
		boolean stopped = midisLoader.stop();
		if (!stopped) {
			midisLoader.play(name, loop);
		}
	}
	
	public void setPlayer(String playerName) {
		this.playerName = playerName;
		restart();
	}

	public void mute() {
		midisLoader.pause();
	}
	
	public void activateSound() {
		midisLoader.resume();
	}
	
	void setRunning(boolean running) {
		this.running = running;
	}
	
	boolean isUsingScore() {
		return useScore;
	}
}