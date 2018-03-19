import java.io.IOException;
import java.nio.file.Paths;

public class CompilePlayer {

	public static void main(String[] args) throws IOException, InterruptedException {

	}

	public static void compile(String pathInSrc) throws IOException, InterruptedException {
		final String command = "javac " + Paths.get("").toAbsolutePath().toString() + "\\src\\" + pathInSrc;
		System.out.println("Execute: '" + command + "'");

		final Process process = Runtime.getRuntime().exec(command);
		System.out.println(process);
		System.out.println("Exited With: " + process.waitFor());
	}

}
