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
        //TODO: Jeg ved ikke hvorfor hun har instantiated den her scannedTokens liste her. Ville være nemmere hvis den var attribut som i exercises --Lucas.
        List<Token> scannedTokens =  new LinkedList<>();
        // TODO add here logic to scan tokens
        char c = advance();
        switch (c) {
            case '(': addToken(scannedTokens, TokenType.LEFT_PAREN); break;
            case ')': addToken(scannedTokens, TokenType.RIGHT_PAREN); break;
            case '{': addToken(scannedTokens, TokenType.LEFT_BRACE); break;
            case '}': addToken(scannedTokens, TokenType.RIGHT_BRACE); break;
            case ',': addToken(scannedTokens, TokenType.COMMA); break;
            case ';': addToken(scannedTokens, TokenType.SEMICOLON); break;

            case '#':
                while (peek() != '\n' && !isAtEnd()) advance();
                break;

            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespaces.
                break;
            
            case '\n':
                line++;
                break;

            case '"':
                string(scannedTokens);
                break;


            default:
                error(line, "Unexpected character.");
                break;
        }
        
        return scannedTokens;
    }

    private void addToken(List<Token> tokenList, TokenType type) {
        addToken(tokenList, type, null);
    }

    private void addToken(List<Token> tokenList, TokenType type, Object literal) {
        String text = inputString.substring(start, current);
        tokenList.add(new Token(type, text, literal, line));
    }

    // TODO: handle error --Lucas
    private void error(int line, String message) {
        System.err.println("[line " + line + "] Error: " + message);
    }

    private void string(List<Token> tokenList) {
        int newlineCount = 0;

        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                newlineCount++;
                if (newlineCount > 1) {
                    error(line, "String spans too many lines");

                    // Scanner still continues on rest of inputString even though we saw an error. So we consume the rest of the invalid string.
                    while (peek() != '"' && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }

                    // When exiting loop we either reached closing " or reach EOF. If reason is ew reached closing " we need to consume it.
                    if (!isAtEnd()) advance();
                    return;
                }
            }
            advance();
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.");
        }
        
        // Consume closing "
        advance();

        String value = inputString.substring(start + 1, current - 1);
        addToken(tokenList, TokenType.STRING, value);
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
