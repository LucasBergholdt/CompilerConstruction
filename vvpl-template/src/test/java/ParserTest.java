import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ASTPrinter;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.parse.Parser;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sandra Greiner
 * @version Compiler Construction FT 2025
 */
public class ParserTest {

    private static String inputFile;
    /** input file contents as a string of bytes (all in one line) */
    private static String sampleInputString;
    /** path to the file which contains the expected output */
    private static String expectedFile;

    @BeforeAll
    public static void prepareFiles() {
        inputFile = "src/test/resources/sample-input.in";
        expectedFile = "src/test/resources/sample-ast-expected.out";
        try {
            sampleInputString = new String(Files.readAllBytes(Paths.get(inputFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * this is one proposed way to print the AST
     * implement an AST-Visitor which prints relevant AST nodes
     * you can find the relevant/expected AST nodes that should be printed in the
     * file
     * sample-ast-expected.out in the test resources
     */
    protected String getASTString(List<?> statements) {
        StringBuilder builder = new StringBuilder();
        ASTPrinter printer = new ASTPrinter();
        for (var stmt : statements) {
            builder.append(printer.print((Stmt) stmt));
        }
        return builder.toString();
    }

    @Test
    public void testEquivalenceOfEachLine() {
        Scanner scanner = new Scanner(sampleInputString);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<?> statements = parser.parse();

        String fileActual = getASTString(statements);
        List<String> fileActualLines = Arrays.asList(fileActual.split(System.lineSeparator()));

        try {
            List<String> fileExpectedLines = Files.readAllLines(Paths.get(expectedFile));
            for (int i = 0; i < fileExpectedLines.size(); i++) {
                String actualLine = fileActualLines.get(i);
                String expectedLine = fileExpectedLines.get(i);
                assertTrue(actualLine.equals(expectedLine),
                        "line " + i + " of source and target mismatch." + System.lineSeparator() +
                                " Expected the content: " + expectedLine + System.lineSeparator() +
                                "got: " +
                                actualLine + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}