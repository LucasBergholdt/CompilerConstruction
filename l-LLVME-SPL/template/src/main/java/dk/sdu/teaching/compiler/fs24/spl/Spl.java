package dk.sdu.teaching.compiler.fs24.spl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import dk.sdu.teaching.compiler.fs24.spl.ast.Stmt;
import dk.sdu.teaching.compiler.fs24.spl.codegen.LLVMEmitter;
import dk.sdu.teaching.compiler.fs24.spl.codegen.LLVMEmitter;
import dk.sdu.teaching.compiler.fs24.spl.parse.Parser;
import dk.sdu.teaching.compiler.fs24.spl.scan.Scanner;
import dk.sdu.teaching.compiler.fs24.spl.scan.Token;

public class Spl {

	// Expects a single file that comprises SPL' program as argument
	public static void main(String[] args) throws IOException {

		runFile(args[0] );
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		new Spl().run(new String(bytes, Charset.defaultCharset()));
	}

	private void run(String source) throws IOException {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();

		// Save the created statements to a .ll file //

		PrintStream originalOut = System.out;
		File output = new File("../output.ll"); // Save to the users default open directory.
		output.createNewFile();

		try (FileOutputStream outStream = new FileOutputStream(output);
		PrintStream fileOut = new PrintStream(outStream)) {
		
		System.setOut(fileOut);
		
		// Printing the actual l-llvm to the redirected output stream (our file).
		LLVMEmitter emitter = new LLVMEmitter();
		emitter.generateCode(statements);


		} finally {
			System.setOut(originalOut); // Redirect back to default stream when done. 
		} 
	
	}

}
