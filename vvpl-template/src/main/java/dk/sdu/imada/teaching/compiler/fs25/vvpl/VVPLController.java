/* C-E: Ikke tag denne fil for gode varer. Har blot kopieret SPL koden herind (de har somewhat samme funktionalitet) */

package dk.sdu.imada.teaching.compiler.fs25.vvpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation.Interpreter;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis.ScopeAnalyzer;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis.TypeAnalyzer;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

public class VVPLController {
	private static boolean hadError = false;
	private static List<ErrorMessage> errorMessages = new ArrayList<>();

	public List<String> execute(String inputFile) {
		// Reset error state for a new run
		hadError = false;
		errorMessages.clear();

		Scanner scanner = new Scanner(inputFile);
		List<Token> tokens = scanner.scanTokens();
		// Proceed ONLY if no failure.
		if (hadError) {
			Collections.sort(errorMessages); // Possible because errorMessages implements Comparable
			//! https://stackoverflow.com/questions/4581407/how-can-i-convert-arraylistobject-to-arrayliststring
			return errorMessages.stream().map(ErrorMessage::toString).toList(); // Converting List<ErrorMessage> to List<String>
		}

		// // print the tokens
		// for (Token token : tokens) {
		// System.out.println(token);
		// }


		// Parser ...
		Parser parser = new Parser(tokens);
		List<Stmt> stmts = parser.parse();
		if (hadError) {
			Collections.sort(errorMessages);
			return errorMessages.stream().map(ErrorMessage::toString).toList();
		}

		// ScopeAnalyzer
		ScopeAnalyzer scopeanalyzer = new ScopeAnalyzer(stmts);
		scopeanalyzer.analyse();
		if (hadError) {
			Collections.sort(errorMessages);
			return errorMessages.stream().map(ErrorMessage::toString).toList();
		}

		// TypeAnalyzer
		TypeAnalyzer typeAnalyzer = new TypeAnalyzer(stmts);
		typeAnalyzer.analyse();
		if (hadError) {
			Collections.sort(errorMessages);
			return errorMessages.stream().map(ErrorMessage::toString).toList();
		}

		// Interpret semantically correct programs.
		Interpreter interpreter = new Interpreter();
		interpreter.interpret(stmts);


		List<String> errormessages = new LinkedList();
		return errormessages;
	}

	// Custom error handler that sets hadError to true and adds the error message to the list of error messages
	public static void error(int line, String errorType, String message) {
		hadError = true;
		errorMessages.add(new ErrorMessage(line, errorType, message));
	}

	
	// ERROR HANDLERS FROM THE BOOK:
	// NOT USED CURRENTLY
/* 	static void error(int line, String message) {
		report(line, "", message);
	}

	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

	static void error(Token token, String message) {
		if (token.type  == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "'" , message);
		}
	} */


}














	/*
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

*/