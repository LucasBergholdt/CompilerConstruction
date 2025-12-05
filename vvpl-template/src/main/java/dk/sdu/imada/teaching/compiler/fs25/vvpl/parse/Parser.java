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
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public class Parser {

    private List<Token> tokens;
    int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }


    // program := decl* EOF

    /** @author: Carl-Emil Dons Christensen */
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

    /** @author: Carl-Emil Dons Christensen */
    private Stmt decl() {
        if (match(FUNCTION)) {
            return function();
        } else if (match(VAR)) {
            return varDecl();
        } else {
            return statement();
        }
    }

    /** @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen */
    private Stmt varDecl() {
        // When entering this method "variable" has already been consumed by match in decl()
        
        // Case: "variable ____ 'has_type' ____
        // We expect an ID (name of variable):
        Token id = consume(IDENTIFIER, "Expected identifier");

        // We expect "has_type":
        consume(TYPE_DEF, "Expected 'has_type'");
        
        // Expecting a NumberType, StringType or BoolType:
        TokenType type = null;

        if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
            type = previous().type;
        } else {
            throw error(peek(), "Type specified should be NumberType, StringType or BoolType");
        }

        // Optional "is" expr:
        Expr expr = null;
        if (match(ASSIGN)) {
            // Case: "variable ____ 'has_type' ____ 'is' expression()
            expr = expression();
        }

        // Expecting semicolon at end:
        consume(SEMICOLON, "Expected ';'");

        return new Stmt.VarDecl(id, type, expr);
    }

    // ------------------ All statements in statement except exprStmt -----------------------

    /** @author: Carl-Emil Dons Christensen */
    private Stmt statement() {
        if (match(PRINT))
            return printStmt();

        if (match(IF))
            return ifStmt();

        if (match(WHILE))
            return whileStmt();

        if (match(LEFT_BRACE))
            return new Stmt.BlockStmt(block()); // Wraps result of block() in Stmt.BlockStmt

        if (match(RETURN))
            return returnStmt();

        return exprStmt();
    }

    /** @author: Carl-Emil Dons Christensen */
    private Stmt printStmt() {
        Expr value = expression();
        Token printtoken = previous();
        consume(SEMICOLON, "Expected semicolon");
        return new Stmt.PrintStmt(value, printtoken);
    }


    /** @author: Carl-Emil Dons Christensen */
    private Stmt ifStmt() {
        Token ifToken = previous();
        consume(LEFT_PAREN, "Expected '('");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')'");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.IfStmt(ifToken, cond, thenBranch, elseBranch);
    }

    /** @author: Carl-Emil Dons Christensen */
    private Stmt whileStmt() {
        Token whileToken = previous();
        consume(LEFT_PAREN, "Expected '('");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')'");
        Stmt body = statement();
        return new Stmt.WhileStmt(whileToken, cond, body);
    }

    /** @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen */
    private List<Stmt> block() { // TODO: allow syncs in blocks
        List<Stmt> statements = new LinkedList<>();
        
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(decl());
        }

        consume(RIGHT_BRACE, "Expected '}'");
        return statements;
    }

    /** @author: Lucas Bergholdt Hansen */
    private Stmt returnStmt() {
        Token returnKeyword = previous(); // Kept for later error reporting
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expected ';'");
        return new Stmt.ReturnStmt(returnKeyword, value);
    }


    // ------------ ExprStmt and nested functions -----------------


    /** @author: Carl-Emil Dons Christensen */
    private Stmt exprStmt() {
        Expr expr = expression();
        consume(SEMICOLON, "Expected ';'");

        return new Stmt.ExprStmt(expr);
    }

    /** @author: Carl-Emil Dons Christensen */
    private Expr expression() {
        return assignment();
    }

    /** @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen */
    private Expr assignment() {
        Expr expr = logicalOr();

        if (match(ASSIGN)) {
            Token assignToken = previous(); // The 'is' token
            Expr value = assignment();

            if (expr instanceof Identifier) {
                Token id = ((Identifier)expr).id;
                return new Expr.Assign(id, value);
            }

            //TODO: Book doesn't throw error. Should we?
            //TODO: Jeg tror blot det er en optimization de har lavet, men jeg synes det gør det hele lidt mere forvirrende egentlig.
            throw error(assignToken, "Invalid assignment target.");
        }
        
        return expr;
    }

    /** @author: Carl-Emil Dons Christensen */
    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    /** @author: Carl-Emil Dons Christensen */
    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    /** @author: Carl-Emil Dons Christensen */
    private Expr equality() {
        Expr expr = compr();

        while (match(NOT_EQUALS, EQUALS)) {
            Token operator = previous();
            Expr right = compr();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /** @author: Carl-Emil Dons Christensen */
    private Expr compr() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right); 
        }

        return expr;
    }

    /** @author: Carl-Emil Dons Christensen */
    private Expr term () {
        if (match(SUB, PLUS, MULT, DIV)) {
            Token operator = previous();
            consume(LEFT_PAREN, "Expected '('");
            Expr left = term(); 
            Expr right = term();
            consume(RIGHT_PAREN, "Expected ')'");
            return new Expr.Binary(left, operator, right);
        } else {
            return unary();
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    private Expr unary () {
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator, expr);
        } else {
            return call();
        }
    }

    /** @author: Lucas Bergholdt Hansen */
    private Stmt function() {
        Token name = consume(IDENTIFIER, "Expected function name");
        consume(LEFT_PAREN, "Expected '('");
        // If we don't have a closing ')' right after we have params:
        List<Param> params = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            params = params();
        }

        consume(RIGHT_PAREN, "Expected ')'");
        // Check if function "has_type" and handle it:
        Token type = null;
        if (match(TYPE_DEF)) {
            if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
                type = previous();
            } else {
                throw error(peek(), "Type specified should be NumberType, StringType or BoolType");
            }
        }

        consume(LEFT_BRACE, "Expected '{'"); // block() assumes { has already been consumed
        List<Stmt> body = block();
        return new Stmt.FunctionStmt(name, params, type, body);
    }

    /** @author: Lucas Bergholdt Hansen */
    private List<Param> params() {
        List<Param> params = new ArrayList<>();

        do {
            Token id = consume(IDENTIFIER, "Expected identifier");
            consume(TYPE_DEF, "Expected 'has_type'");
            Token type = null;
            if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
                type = previous();
            } else {
                throw error(peek(), "Type specified should be NumberType, StringType or BoolType");
            }
            params.add(new Param(id, type));
        } while (match(COMMA));

        return params;
    }

    /** @author: Lucas Bergholdt Hansen */
    private Expr call() {
        Expr expr = primary();

        if (match(LEFT_PAREN)) {
            List<Expr> arguments = new ArrayList<>();
            if (!check(RIGHT_PAREN)) {
                arguments = args();
            }
            // Storing right paren token to use it for reporting rumtime errors caused by a function call
            Token paren = consume(RIGHT_PAREN, "Expected ')'");
            
            return new Expr.Call(expr, paren, arguments);
        }

        return expr;
    }

    /** @author: Lucas Bergholdt Hansen */
    private List<Expr> args() {
        List<Expr> arguments = new ArrayList<>();
        arguments.add(expression()); // handling first "expr" in grammar

        // Handling ("," expr)*:
        while (match(COMMA)) { 
            arguments.add(expression());
        }
        return arguments;
    }

    /** @author: Lucas Bergholdt Hansen */
    // Handles the cast that might appear
    private Expr primary() {
        // Check if cast is specified
        if (match(CAST)) {
            // Expecting a NumberType, StringType or BoolType:
            Token typeToken = null;
            if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
                typeToken = previous();
            } else {
                throw error(peek(), "Type specified should be NumberType, StringType or BoolType");
            }

            // Create new Expr.Cast:
            Expr expr = primaryNoCast();
            return new Expr.Cast(typeToken, expr);

        } else { // Otherwise just parse the primary (literal / identifier / grouping)
            return primaryNoCast();
        }
    }

    /** @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen */
    // Handles the rest of primary
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
            
            // TODO: Test om dette fungerer. -> Hvorfor er det nu vi ikke har grouping expression som bogen?
            case LEFT_PAREN:
                advance(); // Consume the '('
                expr = expression();
                consume(RIGHT_PAREN, "Expected ')'");
                return expr;

            // Else we report that we expected an expression
            default:
                throw error(peek(), "Expected expression.");
        }
    }

    // -------------------------- Helper Functions -------------------------
    /** @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen */

    Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // Checks if current token has any of the given types. If so consume it and return true. Otherwise return false.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        } else {
            return peek().type == type;
        }
    }

    // Consume current token and return it
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    // Consumes if given token matches current token, otherwise throws error
    private Token consume(TokenType type, String message) {
        if (check(type)) { // If we recieve the token type we expect, move on. 
            return advance();
        } else {
            throw error(peek(), message);
        }
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


    private static class ParseError extends RuntimeException {}

    //TODO: Currently this just returns the error. They do this in the book because sometimes you want to report an error without throwing. However, we currently don't use this.
    private ParseError error(Token token, String message) {
        // Add the error to the list of errors in VVPLController and return a new ParseError
        VVPLController.error(token.line, ErrorTypeStrings.PARSE_ERROR, message);
        return new ParseError();
    }

    private void synchronise() {
        // Consume the problematic token that triggered the error
        advance();

        // Discard tokens until we find a safe place to continue
        while (!isAtEnd()) {
            // If we just passed a semicolon the next token is very likely the start of a new statement
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