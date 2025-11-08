import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * @author Sandra Greiner
 * @version Compiler Construction FT 2025
 */
public class ParseErrorTest {
    @Test
    public void testErrors() {
        try {
            TestUtil.runTestFiles("src/test/resources/parse/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}