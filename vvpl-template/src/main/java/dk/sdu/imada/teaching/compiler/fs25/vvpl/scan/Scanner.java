package dk.sdu.imada.teaching.compiler.fs25.vvpl.scan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.*;

/**
 * @author Lucas Bergholdt Hansen
 * @version CompilerConstruction FT 2025
 */
public class Scanner {
    private static final Map<String, TokenType> keywords;
    	static {
		keywords = new HashMap<>();
        keywords.put("variable", VAR);
        keywords.put("has_type", TYPE_DEF);
        keywords.put("String", STRING_TYPE);
        keywords.put("Number", NUMBER_TYPE);
        keywords.put("Bool", BOOL_TYPE);
        keywords.put("is", ASSIGN);

        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("AND", AND);
        keywords.put("OR", OR);
        keywords.put("NOT", NOT);
        keywords.put("EQUALS", EQUALS);
        keywords.put("NOT_EQUALS", NOT_EQUALS);

        keywords.put("add", PLUS);
        keywords.put("subtract", SUB);
        keywords.put("divide", DIV);
        keywords.put("multiply", MULT);

        keywords.put("GREATER", GREATER);
        keywords.put("GREATER_EQUAL", GREATER_EQUAL);
        keywords.put("LESS", LESS);
        keywords.put("LESS_EQUAL", LESS_EQUAL);

        keywords.put("write_to_console", PRINT);
        keywords.put("loop_while", WHILE);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("cast_to", CAST);

        keywords.put("function", FUNCTION);
        keywords.put("return", RETURN);
        
	}

    private final List<Token> scannedTokens =  new LinkedList<>();
    private final String inputString;
    // Scanning state
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String inputString) {
        this.inputString = inputString;
    }

    public List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken();
		}

		scannedTokens.add(new Token(EOF, "", null, line));
		return scannedTokens;
	}

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case ';': addToken(SEMICOLON); break;
            case '-': addToken(MINUS); break;

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
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    VVPLController.error(line, ErrorTypeStrings.SCAN_ERROR, "Unexpected character.");
                }
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = inputString.substring(start, current);
        scannedTokens.add(new Token(type, text, literal, line));
    }

    // TODO: Denne version virker med hendes tests, men mytest filerne viser en weird implikation af denne implementering.
    private void string() {
        int startLine = line; // keeping track of the line the string starts at
        int newlineCount = 0;
        
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                newlineCount++;
                if (newlineCount > 1) {
                    VVPLController.error(startLine, ErrorTypeStrings.SCAN_ERROR, "String spans too many lines.");
                    advance(); // consume the newline before returning
                    return;
                }
            }
            advance();
        }

        if (isAtEnd()) {
            VVPLController.error(startLine, ErrorTypeStrings.SCAN_ERROR, "Unterminated string.");
            return;
        }
        
        // Consume closing "
        advance();

        String value = inputString.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'

            while (isDigit(peek())) advance(); // consume rest of number
        }

        addToken(NUMBER, Double.parseDouble(inputString.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = inputString.substring(start, current);
        TokenType type = keywords.get(text); // returns keyword if in map or null otherwise
        if (type == null) type = IDENTIFIER;
        addToken(type);
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
