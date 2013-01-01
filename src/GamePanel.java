import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.KeyListener;
import java.util.Map;
import javax.swing.JPanel;
/**
 * @author schuetz
 */
public class GamePanel extends JPanel {

	private static final long serialVersionUID = -2411355421839944222L;
	
	private Image dbImage = null;
	final int fieldSize;
	Map<PieceType, Image> pieceImages;
	Color backgroundColor;
	
	boolean frozen; //FIXME quick solution
	
	GamePanel(Dimension dimension, int fieldSize, Map<PieceType, Image> pieceImages, Color backgroundColor) {
		this(dimension, fieldSize, null, pieceImages, backgroundColor);
	}
	
	GamePanel(Dimension dimension, int fieldSize, KeyListener keyListener, Map<PieceType, Image> pieceImages, Color backgroundColor) {
		super();
		this.fieldSize = fieldSize;
		this.pieceImages = pieceImages;
		this.backgroundColor = backgroundColor;
		
		dbImage = createBufferedImage(dimension.width, dimension.height);
		
		setDoubleBuffered(false);
		setBackground(Color.white);
		setFocusable(true);
		requestFocus();
		
		setPreferredSize(dimension);
		setMinimumSize(dimension);
		setMaximumSize(dimension);
		
		if (keyListener != null) {
			addKeyListener(keyListener);
		}
	}
	
	void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	void render(PieceType[][] pieceTypes) {
		if (!frozen) {
			Graphics dbg = dbImage.getGraphics();
			dbg.setColor(backgroundColor);
			dbg.fillRect(0, 0, dbImage.getWidth(null), dbImage.getHeight(null));
			PieceType type;
			Image image;
			for (int row = 0; row < pieceTypes.length; row++) {
				for (int col = 0; col < pieceTypes[row].length; col++) {
					type = pieceTypes[row][col];
					if (type != null) {
						image = pieceImages.get(type);
						dbg.drawImage(image, col * fieldSize, row * fieldSize, null);
					}
					dbg.setColor(Color.lightGray);
					dbg.drawLine(col * fieldSize, 0, col * fieldSize, dbImage.getHeight(null));
				}
				dbg.setColor(Color.lightGray);
				dbg.drawLine(0, row * fieldSize, dbImage.getWidth(null), row * fieldSize);
			}
			dbg.drawLine(0, pieceTypes.length * fieldSize, dbImage.getWidth(null), pieceTypes.length * fieldSize);
		}
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(dbImage, 0, 0, null);
	}
	
	Image createBufferedImage(int width, int height) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
		GraphicsDevice gs = ge.getDefaultScreenDevice(); 
		GraphicsConfiguration gc = gs.getDefaultConfiguration(); 
		return gc.createCompatibleImage(width, height, Transparency.OPAQUE); 
	}

	public void showWin() {
		drawCenterString("Winner!");
	}

	public void showLose() {
		drawCenterString("Game over");
	}
	
	private void drawCenterString(String string) {
		Graphics dbg = dbImage.getGraphics();
		dbg.setColor(Color.black);
		Font font = new Font("Helvetica", Font.BOLD, 25);
		dbg.setFont(font);
		dbg.drawString(string, 30, 100);
		setFrozen(true);
		repaint();
	}
	
	void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
}
