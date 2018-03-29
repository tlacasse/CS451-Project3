package ttt.agents;

import java.io.IOException;

public interface SocketReadWrite {

	public void close() throws IOException;

	public void flush() throws IOException;

	public void writeInt(int x) throws IOException;

	public void writeByte(byte x) throws IOException;

	public int readInt() throws IOException;

	public byte readByte() throws IOException;

}
