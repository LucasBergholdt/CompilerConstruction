import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Sandra Greiner
 * @version Compiler Construction FT 2025
 */
public class AnalysisTest {

    @Test
    public void testAnalyses() {
        try {
            TestUtil.runTestFiles("src/test/resources/semantic-analysis/");
             TestUtil.runTestFiles("src/test/resources/semantic-analysis/additionalScopeError/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
