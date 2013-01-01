import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;
/**
 * @author schuetz
 */
public class NextPieceImagePanel extends JPanel {

	private static final long serialVersionUID = 3607614556541500209L;
	
	private Image pieceImage;
	
	NextPieceImagePanel() {
		Dimension dimesion = new Dimension(30, 30);
		setPreferredSize(dimesion);
	}

	void setImage(Image pieceImage) {
		this.pieceImage = pieceImage;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		if (pieceImage != null) {
			g.drawImage(pieceImage, 7, 0, null);
		}
	}
}
