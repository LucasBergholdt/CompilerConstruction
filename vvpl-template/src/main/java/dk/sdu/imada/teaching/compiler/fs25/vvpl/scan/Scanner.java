package dk.sdu.imada.teaching.compiler.fs25.vvpl.scan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.*;

/**
 * The Scanner class performs lexical analysis of a VVPL source program.
 * It transforms the raw character stream into a sequence of {@link Token} objects
 * according to the lexical rules of the VVPL language.
 * 
 * @author Lucas Bergholdt Hansen
 * @version CompilerConstruction FT 2025
 */
public class Scanner {
    /**
     * Mapping from reserved keywords to their corresponding token types.
     */
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

    /**
     * The list of tokens produced by the scanner.
     */
    private final List<Token> scannedTokens =  new LinkedList<>();

    /**
     * The complete source program string.
     */
    private final String inputString;

    /**
     * Index of the first character of the current lexeme.
     */
    private int start = 0;
    /**
     * Index of the character currently being examined.
     */
    private int current = 0;
    /**
     * Current line number in the source program used for error reporting.
     */
    private int line = 1;

    /**
     * Constructs a new scanner for the given input string.
     * @param inputString the string to scan (VVPL source program)
     */
    public Scanner(String inputString) {
        this.inputString = inputString;
    }

    /**
     * Iterates the entire input string recognizing different types of tokens.
     * @return the list of all the scanned tokens.
     */
    public List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken();
		}

        // Add an end of file token at the end
		scannedTokens.add(new Token(EOF, "", null, line));
		return scannedTokens;
	}

    /**
     * The heart of the scanner.
     * Scans a single token by advancing the input pointer and classifying the character(s).
     */
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
                // Skip comment by consuming all of its characters
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

    /**
     * Adds a token without an associated literal.
     * @param type the token type.
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Adds a token with an associated literal.
     * @param type the token type
     * @param literal the literal value
     */
    private void addToken(TokenType type, Object literal) {
        String text = inputString.substring(start, current);
        scannedTokens.add(new Token(type, text, literal, line));
    }

    /**
     * Scans the character sequence of a string literal consuming until the closing " is found.
     * Unterminated strings at EOF or strings spanning too many lines result in a scan error.
     */
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

        // Unterminated string at EOF
        if (isAtEnd()) {
            VVPLController.error(startLine, ErrorTypeStrings.SCAN_ERROR, "Unterminated string.");
            return;
        }
        
        // Consume closing "
        advance();

        // Add token with its literal value without the ""
        String value = inputString.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Scans a seires of digits potentially followed by a decimpal point.
     * Leading and trailing decimal points are not allowed.
     */
    private void number() {
        while (isDigit(peek())) advance();

        // Only consume decimal point if it is followed by a number
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'
            while (isDigit(peek())) advance(); // consume rest of number
        }

        // Literal value is stored as a Double.
        addToken(NUMBER, Double.parseDouble(inputString.substring(start, current)));
    }

    /**
     * Scans an identifier or keyword.
     * If a reserved keyword is matched, the corresponding keyword token is produced.
     * Otherwise, an IDENTIFIER token is produced.
     */
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

    /**
     * Consumes the current character by advancing the {@link #current} pointer.
     * @return
     */
    private char advance() {
        return inputString.charAt(current++);
    }

    /**
     * Returns the current character in inputString.
     * @return the current character or '\0' if at EOF.
     */
    private char peek() {
        if (isAtEnd())
            return '\0';
        return inputString.charAt(current);
    }

    /**
     * Checks if scanner has reached EOF
     * @return true if all characters have been consumed, otherwise false.
     */
    private boolean isAtEnd() {
        return current >= inputString.length();
    }

    /**
     * Returns the next character in the inputString.
     * @return the next character, or '\0' if looking beyond EOF.
     */
    private char peekNext() {
        if (current + 1 >= inputString.length()) {
            return '\0';
        } else {
            return inputString.charAt(current + 1);
        }
    }

    /**
     * Checks wheter a character is a digit.
     * @param c the character to test
     * @return true if character is a digit, otherwise false
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks whether a character is a letter or an underscore
     * @param c the character to test
     * @return true if the character is a letter or underscore, otherwise false
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    /**
     * Checks whether character is a digit or letter/underscore.
     * @param c the character to test
     * @return true if character is letter, digit or underscore
     */
    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

}
