package ttt.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Represents a complete game, created from a saved file.
 */
public class Result {

	public final int players, winner;
	public final int count;
	private final int[][] moves;

	public Result(File file) throws FileNotFoundException, IOException {
		try (FileInputStream fis = new FileInputStream(file); DataInputStream reader = new DataInputStream(fis)) {
			players = reader.readByte();
			winner = reader.readByte();
			count = reader.readShort();
			moves = new int[count][3];
			for (int i = 0; i < count; i++) {
				moves[i][0] = reader.readByte();
				moves[i][1] = reader.readByte();
				moves[i][2] = reader.readByte();
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
