package dk.sdu.imada.teaching.compiler.fs25.vvpl.scan;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public class Scanner {

    private final String inputString;
    // Scanning state
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String inputString) {
        this.inputString = inputString;
    }

    public List<Token> scanTokens() {
        List<Token> scannedTokens =  new LinkedList<>();
        // TODO add here logic to scan tokens
        
        return scannedTokens;
    }

    //////////////////////////////////////////////////////////////////////
    // helper methods
    //////////////////////////////////////////////////////////////////////
    private char advance() {
        return inputString.charAt(current++);
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return inputString.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= inputString.length();
    }
}
