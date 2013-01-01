import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.border.Border;
/**
 * @author schuetz
 */
public class GUI {

	private static final long serialVersionUID = 3220362418287239243L;
	
	private Game game;
	private GamePanel gamePanel;
	
	private Font defaultFont = new Font("Helvetica", Font.PLAIN, 12);
	
	private JPanel infoPanel;
	private JPanel dataPanel;
	private JPanel scorePanel;
	private JPanel nextPanel;
	private NextPieceImagePanel nextImagePanel;
	
	private JMenuBar menuBar;
	private JMenuItem quitMenuItem;
	private JMenuItem changePlayerMenuItem;
	private JMenuItem viewScoresMenuItem;
	private JMenuItem newMenuItem;
	
	private JLabel playerNameLabel;
	private JLabel levelLabel;
	private JLabel removedLabel;
	private JLabel scoreLabel;
	private JLabel scoreOutputLabel;
	
	private static final int FIELD_SIZE = 15;
	
	private Map<Character, Action> actions;
	private Map<PieceType, Color> pieceColors;
	private Map<PieceType, Image> nextPanelImages;
	
	private Runnable removedLabelClearer;
	
	private static final Color DEFAULT_BACKGROUND_COLOR = Color.lightGray;
	private static final Color DEFAULT_FOREGROUND_COLOR = Color.black;
	
	private final Container display;
	
	GUI(Container display) {
		this.display = display;
	}
	
	void init(final Game game, Map<PieceType, List<Point>> initPositions, String startPlayerName) {
		this.game = game;
		initColors();
		nextPanelImages = initNextPanelImages(initPositions);
		
		removedLabelClearer = new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				removedLabel.setText("");
			}
		};
		
		Dimension gamePanelDimension = calculateDimensions(Game.COLS, Game.ROWS);
		
		KeyListener keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				processKey(e);
			}
		};
		
	    UIManager.put("Label.font", defaultFont.deriveFont(Font.PLAIN, 14));
	    UIManager.put("Menu.font", defaultFont);
	    UIManager.put("MenuItem.font", defaultFont);
	      
		JPanel panel = new JPanel();
		((RootPaneContainer)display).getContentPane().add(panel, BorderLayout.CENTER);
		BoxLayout panelLayout = new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(panelLayout);
		
		menuBar = new JMenuBar();
		
		if (display instanceof JFrame) {
			JFrame frame = (JFrame)display;
			frame.setTitle("Tetris");
			frame.setJMenuBar(menuBar);
		} else if (display instanceof JApplet) {
			JApplet applet = (JApplet)display;
			applet.setJMenuBar(menuBar);
		}
		
		JMenu gameMenu = new JMenu();
		menuBar.add(gameMenu);
		gameMenu.setText("Game");
		
		newMenuItem = new JMenuItem();
		gameMenu.add(newMenuItem);
		newMenuItem.setText("New");
		newMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				game.restart();
			}
		});
		
		if (game.isUsingScore()) {
			final JTextPane changePlayerTextPane = new JTextPane();
//			changePlayerTextPane.setContentType("text/html");
//			changePlayerTextPane.setText("<div align= center>" +
//					"Set a name to identify yourself in the score table<br>" +
//			"<b>Warning:</b> setting a new playername restarts the game. Your current game will be lost.</div>");
			
			changePlayerMenuItem = new JMenuItem();
			gameMenu.add(changePlayerMenuItem);
			changePlayerMenuItem.setText("Set Player");
			changePlayerMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String playerName = JOptionPane.showInputDialog(display, changePlayerTextPane, "Set player", JOptionPane.PLAIN_MESSAGE);
					if (playerName != null && !playerName.equals("") && playerName.length() < 40) {
						game.setPlayer(playerName);
					}
				}
			});
			
			viewScoresMenuItem = new JMenuItem();
			gameMenu.add(viewScoresMenuItem);
			viewScoresMenuItem.setText("View Scores");
			viewScoresMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					String scores = game.getScores();
					final JTextPane scoresTextPane = new JTextPane();
//					scoresTextPane.setContentType("text/html");
					scoresTextPane.setText(scores + "\n");
					game.setPause(true);
					JOptionPane.showMessageDialog(display, scoresTextPane, "Scores", JOptionPane.PLAIN_MESSAGE, null);
					game.setPause(false);
				}
			});
		
			quitMenuItem = new JMenuItem();
			gameMenu.add(quitMenuItem);
			quitMenuItem.setText("Quit");
			quitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					game.quit();
				}
			});
		}
		
		JMenu soundMenu = new JMenu();
		menuBar.add(soundMenu);
		soundMenu.setText("Sound");
		
		final JMenuItem toggleSoundMenuItem = new JMenuItem();
		soundMenu.add(toggleSoundMenuItem);
		toggleSoundMenuItem.setText("Mute");
		toggleSoundMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (toggleSoundMenuItem.getText().equals("Mute")) {
					game.mute();
					toggleSoundMenuItem.setText("Activate");
				} else {
					game.activateSound();
					toggleSoundMenuItem.setText("Mute");
				}
			}
		});
		
//		JMenuItem setMusicMenuItem = new JMenuItem();
//		gameMenu.add(newMenuItem);
//		newMenuItem.setText("Sound");
//		newMenuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				game.setBackgroundMusic();
//			}
//		});
		
		menuBar.add(Box.createHorizontalGlue());
		JMenu helpMenu = new JMenu();
		helpMenu.setIcon(new ImageIcon(getClass().getClassLoader().getResource("help.png")));
		menuBar.add(helpMenu);
		
		final JTextPane aboutTextPane = new JTextPane();
//        aboutTextPane.setContentType("text/html");
        aboutTextPane.setText("Programming and graphics by Ivan Schuetz\n" +
        		"Sound tracks: Background, Win: Anthony Bouchereau - anthony.bouchereau@caramail.co\n" +
        		"Game over: MatthewCollinson - douglas.winchester@btopenworld.com");

		JMenuItem aboutMenuItem = new JMenuItem();
		helpMenu.add(aboutMenuItem);
		aboutMenuItem.setText("About");
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				game.setPause(true);
				JOptionPane.showMessageDialog(display, aboutTextPane, "About", JOptionPane.PLAIN_MESSAGE, null);
				game.setPause(false);
			}
		});
		
		if (display instanceof JFrame) {
			gamePanel = new GamePanel(gamePanelDimension, FIELD_SIZE, keyListener, initImages(), Color.white); //FIXME dimension von fieldsize abhŠngig!!, initimages in panel--- backgroundcolor in map statt switch
		} else if (display instanceof JApplet) {
			display.addKeyListener(keyListener);
			gamePanel = new GamePanel(gamePanelDimension, FIELD_SIZE, initImages(), Color.white); //FIXME dimension von fieldsize abhŠngig!!, initimages in panel--- backgroundcolor in map statt switch
			display.setFocusable(true);
			display.requestFocus();
		}
		panel.add(gamePanel);
		
		infoPanel = new JPanel();
		panel.add(infoPanel);

		Border line = BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(100, 100, 100));
		infoPanel.setBorder(line);
		
		GridBagLayout InfoPanelLayout = new GridBagLayout();
		InfoPanelLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1};
		InfoPanelLayout.rowHeights = new int[] {3, 7, 7, 7};
		InfoPanelLayout.columnWeights = new double[] {0.1};
		InfoPanelLayout.columnWidths = new int[] {5};
		infoPanel.setLayout(InfoPanelLayout);
		Dimension infoPanelDimension = new Dimension(120, 120);
		infoPanel.setPreferredSize(infoPanelDimension);
		infoPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
		
		dataPanel = new JPanel();
		BoxLayout dataPanelLayout = new BoxLayout(dataPanel, javax.swing.BoxLayout.Y_AXIS);
		dataPanel.setLayout(dataPanelLayout);
		dataPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
		
		JLabel tetrisLabel = new JLabel("T E T R I S");
		tetrisLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		tetrisLabel.setFont(defaultFont.deriveFont(Font.BOLD, 16));
		dataPanel.add(tetrisLabel);
		
		if(game.isUsingScore()) {
			JLabel playerLabel = new JLabel("Player:");
			playerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			playerLabel.setFont(defaultFont.deriveFont(Font.PLAIN, 10));
			dataPanel.add(playerLabel);
			
			playerNameLabel = new JLabel(startPlayerName);
			playerNameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
			playerNameLabel.setFont(defaultFont.deriveFont(Font.PLAIN, 12));
			dataPanel.add(playerNameLabel);
		}
		
		infoPanel.add(dataPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		levelLabel = new JLabel("Level 0");
		infoPanel.add(levelLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 0, 0, 0), 0, 0));
		
		scorePanel = new JPanel();
		scorePanel.setBackground(DEFAULT_BACKGROUND_COLOR);
		removedLabel = new JLabel();
		removedLabel.setForeground(Color.red);
		removedLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		removedLabel.setFont(defaultFont.deriveFont(Font.BOLD));
		scorePanel.add(removedLabel);
		BoxLayout ScorePanelLayout = new BoxLayout(scorePanel, javax.swing.BoxLayout.Y_AXIS);
		scorePanel.setLayout(ScorePanelLayout);
		infoPanel.add(scorePanel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		scoreLabel = new JLabel("Score:");
		scoreLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		scorePanel.add(scoreLabel);

		scoreOutputLabel = new JLabel("0");
		scoreOutputLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		scorePanel.add(scoreOutputLabel);

		nextPanel = new JPanel();
		nextPanel.setBackground(DEFAULT_BACKGROUND_COLOR);
		BoxLayout nextPanelLayout = new BoxLayout(nextPanel, javax.swing.BoxLayout.Y_AXIS);
		nextPanel.setLayout(nextPanelLayout);
		infoPanel.add(nextPanel, new GridBagConstraints(0, 3, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		JLabel nextLabel = new JLabel("Next:");
		nextPanel.add(nextLabel);

		nextImagePanel = new NextPieceImagePanel();
		nextImagePanel.setBackground(DEFAULT_BACKGROUND_COLOR);
		nextPanel.add(nextImagePanel);

		initNextPanelImages(initPositions);
		
		if (display instanceof JFrame) {
			JFrame frame = (JFrame)display;
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
		} else if (display instanceof JApplet) {
			//TODO heightOffset at runtime
			int heightOffset = 23;
			Dimension frameDimension = new Dimension(gamePanelDimension.width + infoPanelDimension.width, gamePanelDimension.height + heightOffset);
			display.setPreferredSize(frameDimension);
			display.setMinimumSize(frameDimension);
			display.setMaximumSize(frameDimension);
			display.setSize(frameDimension);
		}
		display.setVisible(true);
		
		actions = new HashMap<Character, Action>();
		actions.put('a', Action.move_left);
		actions.put('d', Action.move_right);
		actions.put('x', Action.move_down);
		actions.put('s', Action.rotate);
		actions.put('p', Action.pause);
	}
	
	private Dimension calculateDimensions(int width, int height) {
		return new Dimension(width * FIELD_SIZE, height * FIELD_SIZE + 1); //1 px more for bottom line
	}
	
	private void initColors() {
		pieceColors = new HashMap<PieceType, Color>();
		pieceColors.put(PieceType.I, Color.red);
		pieceColors.put(PieceType.J, Color.orange);
		pieceColors.put(PieceType.L, Color.magenta);
		pieceColors.put(PieceType.O, Color.blue);
		pieceColors.put(PieceType.S, Color.green);
		pieceColors.put(PieceType.T, new Color(100, 150, 150));
		pieceColors.put(PieceType.Z, Color.cyan);
	}

	void processKey(KeyEvent e) {
		char keyChar = e.getKeyChar();
		if (actions.containsKey(keyChar)) {
			game.processAction(actions.get(keyChar));
		}
	}
	
	void render(PieceType[][] occupiedFields) {
		gamePanel.render(occupiedFields);
		display.repaint();
	}
	
	void showLevel(int level) {
		Color backgroundColor;
		switch (level) {
			case 1: backgroundColor = new Color(200, 200, 255); break;
			case 2: backgroundColor = new Color(200, 255, 200); break;
			case 3: backgroundColor = new Color(255, 200, 200); break;
			case 4: backgroundColor = new Color(200, 200, 220); break;
			case 5: backgroundColor = new Color(200, 220, 200); break;
			case 6: backgroundColor = new Color(220, 200, 200); break;
			case 7: backgroundColor = new Color(170, 170, 200); break;
			case 8: backgroundColor = new Color(170, 200, 170); break;
			case 9: backgroundColor = new Color(200, 170, 170); break;
			default: backgroundColor = Color.white;
		}
		gamePanel.setBackgroundColor(backgroundColor);
		levelLabel.setText("Level " + level);
	}
	
	void showScore(int score) {
		scoreOutputLabel.setText(score + "");
	}
	
	public void paint(Graphics g) {
		display.paint(g);
	}
	
	Map<PieceType, Image> initImages() { //FIXME should be in gamepanel
		Map<PieceType, Image> pieceImages = new HashMap<PieceType, Image>();
		PieceType[] pieceTypes = PieceType.values();
		Image image = null;
		for (PieceType type : pieceTypes) {
			image = createFieldImage(pieceColors.get(type));
			pieceImages.put(type, image);
		}	
		return pieceImages;
	}
	
	Image createFieldImage(Color fieldColor) {
		Image image = createBufferedImage(FIELD_SIZE, FIELD_SIZE);
		Graphics g = image.getGraphics();
		g.setColor(fieldColor.darker());
		g.fillRect(0, 0, FIELD_SIZE, FIELD_SIZE);
		g.setColor(fieldColor);
		int darkerSize = 3;
		double squareSide = FIELD_SIZE - (darkerSize * 2);
		Graphics2D g2d = (Graphics2D)g;
		g2d.fill(new Rectangle2D.Double(darkerSize, darkerSize,squareSide, squareSide));
		return image;
	}
	
	Image createBufferedImage(int width, int height) {  //FIXME method twice
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
		GraphicsDevice gs = ge.getDefaultScreenDevice(); 
		GraphicsConfiguration gc = gs.getDefaultConfiguration(); 
		return gc.createCompatibleImage(width, height, Transparency.OPAQUE); 
	}

	Map<PieceType, Image> initNextPanelImages(Map<PieceType, List<Point>> piecePoints) {
		Map<PieceType, Image> nextPanelImages = new HashMap<PieceType, Image>();
		Image pieceImage;
		int squareSize = 10;
		int squareCount;
		int imageSize = squareSize * 3;

		final int centralSquareX = squareSize;
		int centralSquareY;
		
		int squareX;
		int squareY;
		
		int darkerSize = 1;
		double darkerSquareSide = squareSize - (darkerSize * 2);
		
		for (PieceType type : piecePoints.keySet()) {
			if (type == PieceType.I) {
				squareCount = 4;
				centralSquareY = squareSize * 2;
			} else {
				squareCount = 3;
				centralSquareY = squareSize;
			}
			pieceImage = createBufferedImage(imageSize, imageSize);
			Graphics g = pieceImage.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, imageSize, imageSize);
			
			Color pieceColor = pieceColors.get(type);
			g.setColor(pieceColor.darker());
			g.fillRect(centralSquareX, centralSquareY, squareSize, squareSize);
			g.setColor(pieceColor);
			Graphics2D g2d = (Graphics2D)g;
			g2d.fill(new Rectangle2D.Double(centralSquareX + darkerSize + 1, centralSquareY + darkerSize + 1, darkerSquareSide - 1, darkerSquareSide - 1)); //FIXME
			
			for (Point p : piecePoints.get(type)) {
				squareX = centralSquareX + (p.x * squareSize);
				squareY = centralSquareY + (p.y * squareSize);
				g.setColor(pieceColor.darker());
				g.fillRect(squareX, squareY, squareSize, squareSize);
				g.setColor(pieceColor);
				g2d.fill(new Rectangle2D.Double(squareX + darkerSize + 1, squareY + darkerSize + 1, darkerSquareSide - 1, darkerSquareSide - 1)); //FIXME
			}
			
			g.setColor(Color.lightGray);
			for (int row = 0; row < squareCount; row++) {
				for (int col = 0; col < squareCount; col++) {
					g.drawLine(col * squareSize, 0, col * squareSize, imageSize);
				}
				g.drawLine(0, row * squareSize, imageSize, row * squareSize);
			}
			nextPanelImages.put(type, pieceImage);
		}
		return nextPanelImages;
	}
	
	void showNextPiece(PieceType type) {
		nextImagePanel.setImage(nextPanelImages.get(type));
		nextImagePanel.repaint();
	}
	
	public void showWin() {
		gamePanel.showWin();
		setBGColor(Color.lightGray);
	}

	public void showLose() {
		gamePanel.showLose();
		setBGColor(Color.black);
		setFGColor(Color.red);
	}

	public void setDefaultColors() {
		setBGColor(DEFAULT_BACKGROUND_COLOR);
		setFGColor(DEFAULT_FOREGROUND_COLOR);
	}
	
	private void setBGColor(Color color) {
		infoPanel.setBackground(color);
	    dataPanel.setBackground(color);
		scorePanel.setBackground(color);
		nextPanel.setBackground(color);
		nextImagePanel.setBackground(color);
	}
	
	private void setFGColor(Color color) {
		if (game.isUsingScore()) {
			playerNameLabel.setForeground(color);
		}
		levelLabel.setForeground(color);
		removedLabel.setForeground(color);
		scoreLabel.setForeground(color);
		scoreOutputLabel.setForeground(color);
	}
	
	public void showRemoved(Removed removed, int added) {
		String text = removed.toString() + "! (+" + removed.getPoints() + "+" + added + ")";
		Font font = new Font(defaultFont.getName(), defaultFont.getStyle(), defaultFont.getSize());
		Color color = null;
		switch (removed) {
			case Single: font = defaultFont.deriveFont(Font.PLAIN, 10); color = Color.black; break;
			case Double: font = defaultFont.deriveFont(Font.BOLD, 10); color = new Color(0, 150, 0); break;
			case Triple: font = defaultFont.deriveFont(Font.BOLD, 11); color = new Color(200, 50, 50); break;
			case Tetris: font = defaultFont.deriveFont(Font.BOLD, 12); color = new Color(150, 0, 0); break;
		}
		removedLabel.setFont(font);
		removedLabel.setForeground(color);
		
		removedLabel.setText(text);
		new Thread(removedLabelClearer).start();
	}

	public void showPlayerName(String playerName) {
		if (game.isUsingScore()) {
			playerNameLabel.setText(playerName);
		}
	}

	public void setFrozen(boolean frozen) {
		gamePanel.setFrozen(frozen);
	}
}