import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Scanner;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */
public class ScannerTest {

    private static String inputFile;
    private static String inputByteString;
    private static String expectedFile;

    @BeforeAll
    public static void prepareFiles() {
        inputFile = "src/test/resources/sample-input.in";
        expectedFile = "src/test/resources/sample-input-scan.out";
        try {
            inputByteString = new String(Files.readAllBytes(Paths.get(inputFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testEquivalenceOfEachLine() {
        Scanner lexer = new Scanner(inputByteString);
        List<Token> tokens = lexer.scanTokens();

        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token + System.lineSeparator());
            if (token.lexeme.equals(";"))
                builder.append(System.lineSeparator());
        }

        String fileActual = builder.toString();
        List<String> fileActualLines = Arrays.asList(fileActual.split("\\R"));

        try {
            List<String> fileExpectedLines = Files.readAllLines(Paths.get(expectedFile));
            for (int i = 0; i < fileExpectedLines.size(); i++) {
                String actualLine = fileActualLines.get(i);
                String expectedLine = fileExpectedLines.get(i);
                assertTrue(actualLine.equals(expectedLine),
                        "line " + i + " of source and target mismatch. " + System.lineSeparator() +
                                "expected: " + expectedLine + System.lineSeparator() +
                                " got: " + actualLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}