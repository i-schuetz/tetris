import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * @author schuetz
 */
public class ScoreDAO {
	
	private final String SCORE_FILE = "scores.txt";
	private final File scoreFile;
	
//	private final String READ_ERROR_MSG_NO_FILE = "The Score file: " + SCORE_FILE + " could not be found.";
//	private final String READ_ERROR_MSG_IO = "A problem ocurred reading the file: " + SCORE_FILE;
//	private final String WRITE_ERROR_MSG = "A problem occurred writing to the file: " + SCORE_FILE;
	private final String CORRUPTED_MSG = "Contents of score file were corrupted.";
	
	ScoreDAO() {
		scoreFile = new File(SCORE_FILE);
		if (!scoreFile.exists()) {
			try {
				scoreFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	void saveScore(String playerName, int level, int score) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(scoreFile));
		String line;
		final String separator = ":";
		
		List<ScoreEntry> scoreEntries = new ArrayList<ScoreEntry>();
		String[] entryParts;
		
		boolean corruptedContents = false;
		
		while ((line = bufferedReader.readLine()) != null) {
			entryParts = line.split(separator);
			if (entryParts.length != 3) {
				if (!line.equals(CORRUPTED_MSG)) {
					corruptedContents = true;
				}
				break;
			}
			try {
				scoreEntries.add(new ScoreEntry(entryParts[0], Integer.parseInt(entryParts[1]), Integer.parseInt(entryParts[2])));
			} catch(NumberFormatException nfe) {
				corruptedContents = true;
				break;
			}
		}
		bufferedReader.close();

		PrintWriter printWriter = new PrintWriter(scoreFile);
		
		if (corruptedContents) {
			printWriter.print(CORRUPTED_MSG);
		} else {
			scoreEntries.add(new ScoreEntry(playerName, level, score));
			Collections.sort(scoreEntries);
			
			for (ScoreEntry entry : scoreEntries) {
				printWriter.println(entry.getPlayer() + separator + entry.getLevel() + separator + entry.getScore());
			}
		}
		printWriter.close();
	}
	
	String getScores() throws Exception {
//		try {
			StringBuilder scores = new StringBuilder();
			BufferedReader bufferedReader = new BufferedReader(new FileReader(scoreFile));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				scores.append(line + "<br>");
			}
			bufferedReader.close();
			return scores.toString();
//		} catch (FileNotFoundException e) {
//			return READ_ERROR_MSG_NO_FILE;
//		} catch (IOException e) {
//			return READ_ERROR_MSG_IO;
//		}
	}
	private static class ScoreEntry implements Comparable<ScoreEntry> {
		
		ScoreEntry(String player, int level, int score) {
			this.player = player;
			this.level = level;
			this.score = score;
		}
		private String player;
		private int level;
		private int score;
		
		public String getPlayer() {
			return player;
		}
//		public void setPlayer(String player) {
//			this.player = player;
//		}
		public int getLevel() {
			return level;
		}
//		public void setLevel(int level) {
//			this.level = level;
//		}
		public int getScore() {
			return score;
		}
//		public void setScore(int score) {
//			this.score = score;
//		}
		public int compareTo(ScoreEntry scoreEntry) {
			return Integer.valueOf(score).compareTo(Integer.valueOf(scoreEntry.getScore()));
		}
	}
}