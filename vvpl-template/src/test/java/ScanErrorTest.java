import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * @author Sandra Greiner
 * @version Compiler Construction FT 2025
 */
public class ScanErrorTest {

    @Test
    public void testerrors() {
        try {
            TestUtil.runTestFiles("src/test/resources/scan/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}