package dk.sdu.imada.teaching.compiler.fs25.vvpl.scan;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, 
    LEFT_BRACE, RIGHT_BRACE, 
    COMMA, SEMICOLON,

    // Logical operators:
    NOT, OR, AND, 
    TRUE, FALSE,

    // Comparison:
    EQUALS, NOT_EQUALS,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Arithmetic:
    PLUS, SUB, DIV, MULT,

    // Declarations:
    VAR, // "variable"
    TYPE_DEF, // "has_type"
    ASSIGN, // "is"
    CAST, // "cast_to"
    MINUS, // negative number
    NUMBER_TYPE, STRING_TYPE, BOOL_TYPE, // variable types

    // 'Functional' keywords: TODO: (idk hvad det skal hedde)
    IF,
    ELSE,
    WHILE,
    PRINT,
    RETURN,
    FUNCTION,

    // Literals
    IDENTIFIER, // User given names
    NUMBER, // Some numeric value
    STRING, // Some string

    // TODO add the token types
    // End-of-file
    EOF
}
