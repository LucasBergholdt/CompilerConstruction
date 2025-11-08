import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dk.sdu.imada.teaching.compiler.fs24.verbosepl.VVPLController;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */
public class TestUtil {

    public static List<String> listFiles(String dir) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    public static List<String> getExpectedLines(String fileName, String dir) throws IOException {
        String expectedName = fileName.substring(0, fileName.indexOf(".")) + ".output";
        return Files.readAllLines(Paths.get(dir + expectedName));
    }

    public static void runTestFiles(String dir) throws IOException {
        List<String> filePaths = TestUtil.listFiles(dir);
        for (String fileName : filePaths) {
            if (fileName.endsWith(".in")) {
                String fileMsg = "examined file: " + fileName; // for potential error messaged
                System.out.println(fileMsg);
                String inputFile = new String(Files.readAllBytes(Paths.get(dir + fileName)));

                VVPLController controller = new VVPLController();
                List<String> actualLines = controller.execute(inputFile);
                List<String> expectedLines = TestUtil.getExpectedLines(fileName, dir);

                for (int i = 0; i < expectedLines.size(); i++) {
                    if (i >= actualLines.size())
                        fail(fileMsg
                                + ": the expected number of lines is " + expectedLines.size()
                                + System.lineSeparator() + "got the actual: " + actualLines.size()
                                + System.lineSeparator());

                    String actualLine = actualLines.get(i);
                    String expectedLine = expectedLines.get(i);
                    String error = System.lineSeparator()
                            + " Expected line " + i + " to start with: ''" + expectedLine
                            + System.lineSeparator()
                            + " got: ''" + actualLine + System.lineSeparator();
                    assertTrue(actualLine.startsWith(expectedLine), fileMsg + error);
                }
                if (expectedLines.size() < actualLines.size())
                    fail(fileMsg + ". You report more numbers than expected. next line: "
                            + actualLines.get(expectedLines.size()));
            }
        }
    }
}
