import javax.swing.JApplet;
/**
 * @author schuetz
 */
public class AppletDisplay extends JApplet {

	private Game game;

	public void start() {
	    game.setPause(false);
	}

	public void init() {
		game = new Game(new GUI(this));
		new Thread(game).start();
	 }
	
	public void stop() {
		game.setPause(true);
	}
	
	public void destroy() {
		game.setRunning(false);
	}
}
