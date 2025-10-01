package dk.sdu.imada.teaching.compiler.fs25.vvpl.parse;

import java.util.ArrayList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Assign;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Identifier;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Unary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ExprStmt;
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

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
       
        return statements;
    }

    // VVPL Expression:





    private Stmt exprStmt() {
        Expr expr = expression();
        consume(SEMICOLON, null);

        return new Stmt.ExprStmt(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logicalOr();

        if (match(ASSIGN)) {
            // Case: Man kan ikke assigne 5+2 = ...
            if (!(expr instanceof Identifier)) {
                // throw error
            } else {
                // Case: Identifier = ...   assignment er right-associative. 
                Token id = previous();
                expr = assignment();

                return new Expr.Assign(id, expr);
            }
        }
        return expr;
    }


    private Expr logicalOr() { {
        Expr expr = logicalAnd();

        while (match(OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    }

    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }


    private Expr equality() {
        Expr expr = compr();

        while (match(NOT_EQUALS, EQUALS)) {
        Token operator = previous();
        Expr right = compr();
        expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    private Expr compr () {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
        Token operator = previous();
        Expr right = term();
        expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    private Expr term () {
        if (match(MINUS, PLUS, MULT, DIV)) {
            Token operator = previous();
            consume(LEFT_PAREN, null);
            Expr left = term(); 
            Expr right = term();
            consume(RIGHT_PAREN, null);
            return new Expr.Binary(left,operator,right);
        }
        return unary();
    }

    /*  term() i bogen for reference. Assignment1's er dog væsentligt anderledes
        Expr expr = unary();
        while (match(MINUS, PLUS, MULT, DIV)) {
        Token operator = previous();
        Expr right = unary();
        expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    */

    private Expr unary () {
        /*Tillader muligheden for flere fortegn foran unarys, fx. ---5 */
        if (match(NOT) || match(MINUS)) {
            Token op = previous();
            Expr expr = unary();
            return new Unary(op, expr);
        }
        return primary();
    }

    // C-E: Kan også laves med Match (gøres i bogen). For nu tages Niels Erik's løsning.
    private Expr primary() {
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

            /* Ellers regner vi med en grouping. Gøres lidt anderledes i bogen. */
            default:
                consume(LEFT_PAREN, null);
                expr = expression();
                consume(RIGHT_PAREN, null);
                return expr;
        }
    }









    /* Fra øvelses time. Copy-pasted for nu */

/*
    private void synchronise() {
        // todo: what happens with end of file
        while (true) {
            switch (peek().type) {
                case IF:
                case WHILE:
                case PRINT:
                case LEFT_BRACE:
                case VAR:
                    return;

                case SEMICOLON:
                    current++;
                    return;

                default:
                    break;
            }
            current++;
        }
    }


   private Stmt decl() {
        if (match(VAR)) {
            // return vardecl
            consume(IDENTIFIER, "expected Identifier");
            Token id = previous();
            Expr expr = null;

            if (match(ASSIGN)) {
                expr = expr();

            }
            consume(SEMICOLON, "expected semicolon");

            return new VarDecl(id, expr);
        }

        return stmt();
    }

     */









    // -------------------------- Helper Functions -------------------------
    Token peek() {
        return tokens.get(current);
    }


    /* C-E: Gamle Match
    Boolean match(TokenType t) {
        if (peek().type.equals(t)) {
            current++;
            return true;
        }
        return false;
    }

    */


    /* C-E: Taget fra bogen og ændret lidt for ikke at anvende check() og advance(). */
    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (peek().type.equals(t)) {
                    current++;
                    return true;
            }
        }
        return false;
    }


    /* Consumes if given token matches current token, otherwise throws error. */
    void consume(TokenType t, String message) {
        if (peek().type.equals(t)) {
            current++;
            return;
        } else {
            // Spl.error(current, message); // assume we change this to be correct
            throw new ParseError();
        }
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private class ParseError extends RuntimeException {
    }



}