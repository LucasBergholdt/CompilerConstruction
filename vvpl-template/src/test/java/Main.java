import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ASTPrinter;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class Main {
    public static void main(String[] args) {
        String inputByteString;
        String inputFile  =  "/vvpl-interpreter/vvpl-template/src/test/resources/sample-input-our-version.in";
        // String expectedFile  = "src/test/resources/sample-input-scan.out";
        try {
            inputByteString = new String(Files.readAllBytes(Paths.get(inputFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scanner lexer = new Scanner(inputByteString);
        List<Token> tokens = lexer.scanTokens();

        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token + System.lineSeparator());
            if (token.lexeme.equals(";")) builder.append(System.lineSeparator());
        }

        // Print scan output
        String fileActual = builder.toString();
        List<String> fileActualLines = Arrays.asList(fileActual.split("\\R"));

        for (String line: fileActualLines) {
            System.out.println(line);
        }

        
        System.out.println();

        Parser parser = new Parser(tokens);
        List<?> statements = parser.parse();

        // Print parsing
        String fileActual2 = getASTString(statements);
        List<String> fileActualLines2 = Arrays.asList(fileActual2.split(System.lineSeparator()));

        for (String line: fileActualLines2) {
            System.out.println(line);
        }
    }


        /**
     * this is one proposed way to print the AST
     * implement an AST-Visitor which prints relevant AST nodes
     * you can find the relevant/expected AST nodes that should be printed in the
     * file
     * sample-ast-expected.out in the test resources
     */
    protected static String getASTString(List<?> statements) {        // LA: List<?> - does not care about the kind of object stored.
        StringBuilder builder = new StringBuilder();        
        ASTPrinter printer = new ASTPrinter();
        for (var stmt : statements) {
            builder.append(printer.print((Stmt) stmt));
        }
        return builder.toString();
    }
}
