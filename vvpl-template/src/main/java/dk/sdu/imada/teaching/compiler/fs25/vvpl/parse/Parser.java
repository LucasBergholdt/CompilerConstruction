package dk.sdu.imada.teaching.compiler.fs25.vvpl.parse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Identifier;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Param;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.*;


/**
 * The Parser class performs syntactic analysis of a VVPL program.
 * Consumes a list of {@link Token}s and constructs an AST consiting
 * of {@link Expr} and {@link Stmt} nodes.
 * 
 * If an unexpected token is seen it is reported to the controller via {@link VVPLController#error}
 * and a {@link ParseError} is thrown. The Parser uses synchronization to recover from 
 * the error, so it can finish parsing the program in order to detect any other errors.
 * 
 * @version CompilerConstruction FT 2025
 */
public class Parser {

    /**
     * The input list of tokens produced by the scanner.
     */
    private List<Token> tokens;

    /**
     * Index of current token being parsed.
     */
    int current = 0;

    /**
     * Constructs a new parser for the given token stream.
     * @param tokens the list of tokens to parse
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses the complete token stream, by repeatedly parsing top-level declarations until EOF token.
     * Recovers from syntax errors to allow continued parsing.
     * @return a list of statements forming the AST
     * @author: Carl-Emil Dons Christensen 
     */
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        
        while (!isAtEnd()) {
            try {
                statements.add(decl());
            } catch (ParseError e) {
                synchronise();
            }

        }
        return statements;
    }


    // ------------------ decl := funcDecl | varDecl | statement --------------------

    /**
     * Parses the 'decl' grammar rule.
     * @return the parsed statement
     * @author: Carl-Emil Dons Christensen 
     */
    private Stmt decl() {
        if (match(FUNCTION)) {
            return function();
        } else if (match(VAR)) {
            return varDecl();
        } else {
            return statement();
        }
    }

    /**
     * Parses the 'varDecl' grammar rule.
     * @return a {@link Stmt.VarDecl} AST node
     * @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen
     */
    private Stmt varDecl() {
        // When entering this method "variable" has already been consumed by match in decl()
        
        // We expect an ID (name of variable):
        Token id = consume(IDENTIFIER, "Expected identifier in variable declaration");

        // We expect "has_type":
        consume(TYPE_DEF, "Expected 'has_type' in variable declaration");
        
        // Expecting a NumberType, StringType or BoolType:
        Token typeToken = consumeType();

        // Optional "is" expr:
        Expr expr = null;
        if (match(ASSIGN)) {
            expr = expression();
        }

        // Expecting semicolon at end:
        consume(SEMICOLON, "Expected ';' at the end of variable declaration");

        return new Stmt.VarDecl(id, typeToken, expr);
    }

    /**
     * Parses the 'statement' grammar rule.
     * @return the parsed statement.
     * @author: Carl-Emil Dons Christensen
     */
    private Stmt statement() {
        if (match(PRINT)) return printStmt();

        if (match(IF)) return ifStmt();

        if (match(WHILE)) return whileStmt();

        if (match(LEFT_BRACE)) return new Stmt.BlockStmt(block()); // Wraps result of block() in Stmt.BlockStmt

        if (match(RETURN)) return returnStmt();

        return exprStmt();
    }

    /**
     * Parses the 'printStmt' grammar rule
     * @return a {@link Stmt.PrintStmt} AST node.
     * @author: Carl-Emil Dons Christensen
     */
    private Stmt printStmt() {
        Expr value = expression();
        Token printToken = previous();
        consume(SEMICOLON, "Expected ';' at the end of print statement");
        return new Stmt.PrintStmt(value, printToken);
    }

    /**
     * Parses the 'ifStmt' grammar rule
     * @return a {@link Stmt.IfStmt} AST node
     * @author: Carl-Emil Dons Christensen
     */
    private Stmt ifStmt() {
        Token ifToken = previous();
        consume(LEFT_PAREN, "Expected '(' after if statement");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')' to complete if statement");

        Stmt thenBranch = statement();
        // Optional else branch:
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.IfStmt(ifToken, cond, thenBranch, elseBranch);
    }

    /** 
     * Parses the 'whileStmt' grammar rule
     * @return a {@link Stmt.WhileStmt} AST node
     * @author: Carl-Emil Dons Christensen 
     */
    private Stmt whileStmt() {
        Token whileToken = previous();
        consume(LEFT_PAREN, "Expected '('");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')'");
        Stmt body = statement();
        return new Stmt.WhileStmt(whileToken, cond, body);
    }

    /** 
     * Parses the 'block' grammar rule.
     * @return a list of statements inside the block
     * @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen 
     */
    private List<Stmt> block() {
        List<Stmt> statements = new LinkedList<>();
        
        // Handle all statements in the block:
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(decl());
        }

        consume(RIGHT_BRACE, "Expected '}' to complete block");
        return statements;
    }

    /** 
     * Parses the 'returnStmt' grammar rule.
     * @return a {@link Stmt.ReturnStmt} AST node
     * @author: Lucas Bergholdt Hansen 
     */
    private Stmt returnStmt() {
        Token returnKeyword = previous(); // Kept for later error reporting
        // Optional return value:
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expected ';' at the end of return statement");
        return new Stmt.ReturnStmt(returnKeyword, value);
    }

    /** 
     * Parses the 'exprStmt' grammar rule.
     * @return a {@link Stmt.ExprStmt} AST node
     * @author: Carl-Emil Dons Christensen 
     */
    private Stmt exprStmt() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';' at the end of expression");

        return new Stmt.ExprStmt(expr);
    }

    /** 
     * Parses the 'expr' grammar rule
     * @return the parsed expression
     * @author: Carl-Emil Dons Christensen 
     */
    private Expr expression() {
        return assignment();
    }

    /** 
     * Parses the 'assignment' grammar rule
     * Throws an error if trying to assign something other than an identifier.
     * @return the parsed expression.
     * @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen 
     */
    private Expr assignment() {
        Expr expr = logicalOr();

        if (match(ASSIGN)) {
            Token assignToken = previous(); // The 'is' token
            Expr value = assignment();

            if (expr instanceof Identifier) {
                Token id = ((Identifier)expr).id;
                return new Expr.Assign(id, value);
            } else {
                throw error(assignToken, "Invalid assignment target");
            }
        }
        
        return expr;
    }

    /** 
     * Parses the 'logicalOr' grammar rule
     * @return the parsed expression.
     * @author: Carl-Emil Dons Christensen
     */
    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    /** 
     * Parses the 'logicalAnd' grammar rule
     * @return the parsed expression.
     * @author: Carl-Emil Dons Christensen
     */
    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    /** 
     * Parses the 'equality' grammar rule
     * @return the parsed expression.
     * @author: Carl-Emil Dons Christensen
     */
    private Expr equality() {
        Expr expr = compr();

        while (match(NOT_EQUALS, EQUALS)) {
            Token operator = previous();
            Expr right = compr();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /** 
     * Parses the 'compr' grammar rule
     * @return the parsed expression.
     * @author: Carl-Emil Dons Christensen
     */
    private Expr compr() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right); 
        }

        return expr;
    }

    /** 
     * Parses the 'term' grammar rule
     * @return the parsed expression.
     * @author: Carl-Emil Dons Christensen
     */
    private Expr term () {
        if (match(SUB, PLUS, MULT, DIV)) {
            Token operator = previous();
            consume(LEFT_PAREN, "Expected '(' after operator");
            Expr left = term(); 
            Expr right = term();
            consume(RIGHT_PAREN, "Expected ')' after last term in operator");
            return new Expr.Binary(left, operator, right);
        } else {
            return unary();
        }
    }

    /** 
     * Parses the 'unary' grammar rule
     * @return the parsed expression.
     * @author: Carl-Emil Dons Christensen
     */
    private Expr unary () {
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator, expr);
        } else {
            return call();
        }
    }

    /** 
     * Parses the 'call' grammar rule
     * Throws an error if trying to call something other than an identifier.
     * @return the parsed expression.
     * @author: Lucas Bergholdt Hansen 
     */
    private Expr call() {
        Expr expr = primary();

        if (match(LEFT_PAREN)) {
            if (!(expr instanceof Identifier)) {
                throw error(peek(), "Can't call an expression that is not an identifier."); 
            }
            // If we don't have a closing ')' right after '(', we have arguments:
            List<Expr> arguments = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                arguments = args();
            }
            // Storing right paren token to use it for reporting rumtime errors caused by a function call
            Token paren = consume(RIGHT_PAREN, "Expected ')' to complete function call");
            
            Token id = ((Identifier) expr).id;

            return new Expr.Call(id, expr, paren, arguments);
        }

        return expr;
    }

    /** 
     * Parses the 'args' grammar rule
     * @return a list of argument expressions.
     * @author: Lucas Bergholdt Hansen 
     */
    private List<Expr> args() {
        List<Expr> arguments = new ArrayList<>();
        arguments.add(expression()); // handling first "expr" in grammar

        // Handling ("," expr)*:
        while (match(COMMA)) { 
            arguments.add(expression());
        }
        return arguments;
    }

    /** 
     * Parses the 'function' grammar rule
     * @return a {@link Stmt.FunctionStmt} AST node.
     * @author: Lucas Bergholdt Hansen
     */
    private Stmt function() {
        Token name = consume(IDENTIFIER, "Expected function name");
        consume(LEFT_PAREN, "Expected '(' after function name");

        // If we don't have a closing ')' right after '(', we have params:
        List<Param> params = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            params = params();
        }
        consume(RIGHT_PAREN, "Expected ')' to end function parameters");

        // Check if function "has_type" and handle it:
        Token typeToken = null;
        if (match(TYPE_DEF)) {
            typeToken = consumeType();
        }

        consume(LEFT_BRACE, "Expected '{' to start function body"); // block() assumes { has already been consumed
        
        Stmt.BlockStmt body = new Stmt.BlockStmt(block());
        return new Stmt.FunctionStmt(name, params, typeToken, body);
    }

    /** 
     * Parses the 'params' grammar rule
     * @return a list of function parameters as {@link Param} objects.
     * @author: Lucas Bergholdt Hansen 
     */
    private List<Param> params() {
        // Function parameters are stored as a list of Param objects.
        List<Param> params = new ArrayList<>();

        do {
            Token id = consume(IDENTIFIER, "Expected parameter to be an identifier");
            consume(TYPE_DEF, "Expected 'has_type' after parameter");
            Token typeToken = consumeType();
            params.add(new Param(id, typeToken));
        } while (match(COMMA));

        return params;
    }

    /** 
     * Parses the cast that might appear in the 'primary' grammar rule.
     * @return the parsed expression, optionally wrapped in an {@link Expr.Cast} node.
     * @author: Lucas Bergholdt Hansen 
     */
    private Expr primary() {
        // Check if cast is specified
        if (match(CAST)) {
            // Expecting a NumberType, StringType or BoolType:
            Token typeToken = consumeType();

            // Create new Expr.Cast:
            Expr expr = primaryNoCast();
            return new Expr.Cast(typeToken, expr);

        } else { // Otherwise just parse the primary (literal / identifier / grouping)
            return primaryNoCast();
        }
    }

    /**
    * Parses the non-cast part of the 'primary' grammar rule.
    * End of recursion, if we can't match token throw an error.
    * @return the parsed primary expression
    * @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen
    */
    private Expr primaryNoCast() {
        Expr expr;
        switch (peek().type) {
            case FALSE:
            case TRUE:
            case NUMBER:
            case STRING:
                expr = new Expr.Literal(peek());
                current++;
                return expr;

            case IDENTIFIER:
                expr = new Identifier(peek());
                current++;
                return expr;
            
            case LEFT_PAREN:
                advance(); // Consume the '('
                expr = expression();
                consume(RIGHT_PAREN, "Expected ')' to close parenthesis");
                return expr;

            default:
                throw error(peek(), "Expected expression.");
        }
    }


    
    // -------------------------- Helper Functions -------------------------
    /** @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen */

    /**
     * @return the current token
     */
    Token peek() {
        return tokens.get(current);
    }

    /**
     * Checks whether parser has reached end of token stream.
     * @return true if type of current token is EOF, otherwise false
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Attempts to match and consume one of the given token types.
     * @param types the different token types to match
     * @return true if a token was matched, otherwise false
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the current token matches the given type.
     * @param type the expected token type
     * @return true if types match, otherwise false
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        } else {
            return peek().type == type;
        }
    }

    /**
     * Consumes the current token and advances the parser.
     * @return the consumed token.
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Fetches the previously consumed token
     * @return the previous token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    // Consumes if given token type matches current token, otherwise throws error
    /**
     * Consumes current token if it matches the expected type.
     * Otherwise reports and throws an error.
     * @param type the expected token type
     * @param message the error message if token does not match
     * @return the consumed token
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        } else {
            throw error(previous(), message);
        }
    }

    /**
     * Consumes the current token if it is a type token.
     * Otherwise reports and throws an error.
     * @return the consumed type token
     */
    private Token consumeType() {
        if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
            return previous();
        } else {
            throw error(peek(), "Type specified should be NumberType, StringType or BoolType");
        }
    }



    // -------------------------- Error recovery -------------------------

    /**
     * Exception used to mark an error for parser error recovery through synchronization.
     */
    private static class ParseError extends RuntimeException {}

    /**
     * Reports a parse error to the {@link VVPLController} class.
     * @param token the token at which the error occured
     * @param message the error message
     * @return a {@link ParseError} instance to be thrown.
     * @author: Lucas Bergholdt Hansen
     */
    private ParseError error(Token token, String message) {
        VVPLController.error(token.line, ErrorTypeStrings.PARSE_ERROR, message);
        return new ParseError();
    }

    /** 
     * Synchronizes the parser after an error occured by discarding tokens
     * until reaching a token that can begin a new statement.
     * @author: Lucas Bergholdt Hansen 
     */
    private void synchronise() {
        // Consume the problematic token that triggered the error
        advance();

        // Discard tokens until we find a valid place to continue
        while (!isAtEnd()) {
            // If we just passed a semicolon the next token is the start of a new statement
            if (previous().type == SEMICOLON) return;

            // Check if current token begins a new statement
            switch (peek().type) {
                case FUNCTION:
                case VAR:
                case IF:
                case WHILE:
                case PRINT:
                case LEFT_BRACE:
                case RETURN:
                    return;
                
                default:
                    // Current token is part of the mess. Discard it and continue looping.
                    advance();
                    break;
            }
        }
    }


}