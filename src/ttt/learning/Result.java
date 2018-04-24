package ttt.learning;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Represents a complete game, created from a saved file.
 */
public class Result {

	public final int players, winner, count;
	private final int[][] moves;

	public Result(File file) throws FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(file); DataInputStream reader = new DataInputStream(fis)) {
			players = reader.readInt();
			winner = reader.readInt();
			count = reader.readInt();
			moves = new int[count][3];
			for (int i = 0; i < count; i++) {
				moves[i][0] = reader.readInt();
				moves[i][1] = reader.readInt();
				moves[i][2] = reader.readInt();
			}
		}
	}

	public int getMovePlayer(int move) {
		return moves[move][0];
	}

	public int getMoveX(int move) {
		return moves[move][1];
	}

	public int getMoveY(int move) {
		return moves[move][2];
	}

}
