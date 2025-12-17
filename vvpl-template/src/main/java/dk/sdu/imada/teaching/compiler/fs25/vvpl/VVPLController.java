package dk.sdu.imada.teaching.compiler.fs25.vvpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation.Interpreter;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis.ScopeAnalyzer;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis.TypeAnalyzer;

/**
 * This class coordinates the entire interpretation process by invoking
 * the next stage only if the previous succeeded without errors.
 * It centralizes error handling as it handles collecting and reporting
 * errors back to the user in a sorted list.
 */
public class VVPLController {
	/**
	 * A flag used to detect if an error has been detected in the current stage.
	 */
	private static boolean hadError = false;
	
	/**
	 * The list of error messages stored as {@link ErrorMessage} objects.
	 */
	private static List<ErrorMessage> errorMessages = new ArrayList<>();

	/**
	 * Executes the interpretation pipeline on the given input file.
	 * If errors are encountered at any stage, returns a sorted list of error messages.
	 * @param inputFile the source program to interpret
	 * @return a list of output strings as a result of the source programs execution or error messages.
	 */
	public List<String> execute(String inputFile) {
		// Reset error state for a new run
		hadError = false;
		errorMessages.clear();

		// Perform lexical analysis
		Scanner scanner = new Scanner(inputFile);
		List<Token> tokens = scanner.scanTokens();
		// Proceed ONLY if no failure.
		if (hadError) {
			return sortedErrors();
		}

		// Perform syntactic analysis
		Parser parser = new Parser(tokens);
		List<Stmt> stmts = parser.parse();
		if (hadError) {
			return sortedErrors();
		}

		// Perform semantic analysis: Check scopes
		ScopeAnalyzer scopeanalyzer = new ScopeAnalyzer(stmts);
		scopeanalyzer.analyse();
		if (hadError) {
			return sortedErrors();
		}

		// Perform semantic analysis: Check types
		TypeAnalyzer typeAnalyzer = new TypeAnalyzer(stmts);
		typeAnalyzer.analyse();
		if (hadError) {
			return sortedErrors();
		}

		// Interpret semantically correct program.
		Interpreter interpreter = new Interpreter();
		List<String> result = interpreter.interpret(stmts);
		if (hadError) {
			return sortedErrors();
		}
		
		// Return the result of the program's execution
		return result;
	}

	/**
	 * Helper method for sorting the list of error messages
	 * and converting them to strings.
	 * @return a list of the sorted error message strings.
	 */
	private List<String> sortedErrors() {
		Collections.sort(errorMessages); // ErrorMessage implements Comparable
		return errorMessages.stream().map(ErrorMessage::toString).toList(); // Converting List<ErrorMessage> to List<String>
	}

	/**
	 * Adds the error to the list of errors in the VVPLController.
	 * Called from other classes that detect errors.
	 * Sets the hadError flag to indicate an error was detected.
	 * @param line the line of the error
	 * @param errorType the type of the error
	 * @param message the error message
	 */
	public static void error(int line, String errorType, String message) {
		hadError = true;
		errorMessages.add(new ErrorMessage(line, errorType, message));
	}

}