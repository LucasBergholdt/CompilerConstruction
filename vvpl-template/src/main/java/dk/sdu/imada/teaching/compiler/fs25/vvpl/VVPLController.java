/* C-E: Ikke tag denne fil for gode varer. Har blot kopieret SPL koden herind (de har somewhat samme funktionalitet) */

package dk.sdu.imada.teaching.compiler.fs25.vvpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation.Interpreter;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class VVPLController {

    public List<String> execute(String inputFile) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

	private static boolean errorHappened = false;

	// Expects a single file that comprises a SPL program as argument
	public static void main(String[] args) throws IOException {
		runFile(args[0]);
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes));
	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		// // print the tokens
		// for (Token token : tokens) {
		// System.out.println(token);
		// }

		// Parser ...
		Parser parser = new Parser(tokens);
		List<Stmt> stmts = parser.parse();

		//intpret and perform semantic analysis

		//C-E: har lavet disse 2 linjer. Inden da skal vi nok scope/type checke.
		Interpreter interpreter = new Interpreter();
		interpreter.interpret(stmts);
		
	}

	public static void error(int line, String message) {
		report(line, "", message);
	}

	private static void report(int line, String where, String message) {
		errorHappened = true;
		System.err.println("[line " + line + "] Error" + where + ": " + message);
	}

}