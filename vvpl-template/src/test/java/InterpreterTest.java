import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Sandra Greiner
 * @version Compiler Construction FT 2025
 */
public class InterpreterTest {

    @Test
    public void testInterpretations() {
        try {
            TestUtil.runTestFiles("src/test/resources/interpret/");
            TestUtil.runTestFiles("src/test/resources/interpret/programs/");
            TestUtil.runTestFiles("src/test/resources/interpret/functions/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
