package dk.sdu.imada.teaching.compiler.fs25.vvpl.scan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public class Scanner {
    private static final Map<String, TokenType> keywords;
    	static {
		keywords = new HashMap<>();
        keywords.put("variable", TokenType.VAR);
        keywords.put("has_type", TokenType.TYPE_DEF);
        keywords.put("String", TokenType.STRING_TYPE);
        keywords.put("Number", TokenType.NUMBER_TYPE);
        keywords.put("Bool", TokenType.BOOL_TYPE);
        keywords.put("is", TokenType.ASSIGN);

        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("AND", TokenType.AND);
        keywords.put("OR", TokenType.OR);
        keywords.put("NOT", TokenType.NOT);
        keywords.put("EQUALS", TokenType.EQUALS);
        keywords.put("NOT_EQUALS", TokenType.NOT_EQUALS);

        keywords.put("add", TokenType.PLUS);
        keywords.put("subtract", TokenType.SUB);
        keywords.put("divide", TokenType.DIV);
        keywords.put("multiply", TokenType.MULT);

        keywords.put("GREATER", TokenType.GREATER);
        keywords.put("GREATER_EQUAL", TokenType.GREATER_EQUAL);
        keywords.put("LESS", TokenType.LESS);
        keywords.put("LESS_EQUAL", TokenType.LESS_EQUAL);

        keywords.put("writeToConsole", TokenType.PRINT);
        keywords.put("loop_while", TokenType.WHILE);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("cast_to", TokenType.CAST);

        keywords.put("function", TokenType.FUNCTION);
        keywords.put("return", TokenType.RETURN);
        
	}

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

		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken(scannedTokens);
		}

		scannedTokens.add(new Token(TokenType.EOF, "", null, line));
		return scannedTokens;
	}

    private void scanToken(List<Token> scannedTokens) {
        char c = advance();
        switch (c) {
            case '(': addToken(scannedTokens, TokenType.LEFT_PAREN); break;
            case ')': addToken(scannedTokens, TokenType.RIGHT_PAREN); break;
            case '{': addToken(scannedTokens, TokenType.LEFT_BRACE); break;
            case '}': addToken(scannedTokens, TokenType.RIGHT_BRACE); break;
            case ',': addToken(scannedTokens, TokenType.COMMA); break;
            case ';': addToken(scannedTokens, TokenType.SEMICOLON); break;
            case '-': addToken(scannedTokens, TokenType.MINUS); break;

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
                if (isDigit(c)) {
                    number(scannedTokens);
                } else if (isAlpha(c)) {
                    identifier(scannedTokens);
                } else {
                    error(line, "Unexpected character.");
                }
        }
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

                    // When exiting loop we either reached closing " or reach EOF. If reason is we reached closing " we need to consume it.
                    if (!isAtEnd()) advance();
                    return;
                }
            }
            advance();
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.");
            return;
        }
        
        // Consume closing "
        advance();

        String value = inputString.substring(start + 1, current - 1);
        addToken(tokenList, TokenType.STRING, value);
    }

    private void number(List<Token> tokenList) {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'

            while (isDigit(peek())) advance(); // consume rest of number
        }

        addToken(tokenList, TokenType.NUMBER, Double.parseDouble(inputString.substring(start, current)));
    }

    private void identifier(List<Token> tokenList) {
        while (isAlphaNumeric(peek())) advance();

        String text = inputString.substring(start, current);
        TokenType type = keywords.get(text); // returns keyword if in map or null otherwise
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(tokenList, type);
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

    private char peekNext() {
        if (current + 1 >= inputString.length()) {
            return '\0';
        } else {
            return inputString.charAt(current + 1);
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

}
