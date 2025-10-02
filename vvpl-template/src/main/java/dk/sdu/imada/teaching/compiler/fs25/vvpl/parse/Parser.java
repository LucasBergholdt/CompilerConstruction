package dk.sdu.imada.teaching.compiler.fs25.vvpl.parse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Assign;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Identifier;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Unary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.BlockStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ExprStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.IfStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.PrintStmt;
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
    List<Stmt> program = new LinkedList<>();    /* C-E: gammel? skal måske fjernes */

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }



    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        
        // C-E: Her version fra bog. Niels-Erik lavede den anderledes. Jeg synes bogen giver mere mening
        while (!isAtEnd()) {    //tjekker for EOF
            statements.add(decl());
        }
        return statements;
    }



    // ------------------ decl: VarStmt | Statement | FuncDecl --------------------

    private Stmt decl() {

        if(match(VAR)) {
            // Case: "variable ____ 'has_type' ____
            consume(IDENTIFIER, "expected Identifier");
            Token id = previous();
            Expr expr = null;
            consume(TYPE_DEF, "expected 'has_type'");
            if (!match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
                // return error...
            }
            if (match(ASSIGN)) {
                // Case: "variable ____ 'has_type' ____ 'is' expression()
                expr = expression();
            }
            consume(SEMICOLON, "expected semicolon");
            return new Stmt.VarDecl(id, expr);
        }

        // Hvis ikke en VarDecl, så er case statement(). funcDecl er ikke supported endnu.
        return statement();
    }


        // ------------------ All statements in statement except exprStmt -----------------------

        //Taget fra øvelsestimen. Matcher fint.
    private Stmt statement() {
        if (match(PRINT))
            return print();

        if (match(IF))
            return ifStmt();

        if (match(WHILE))
            return whileStmt();

        if (match(LEFT_BRACE))
            return block();

        return exprStmt();
    }

     // Taget fra øvelsestimen. Matcher fint.
    private Stmt print() {
        Expr value = expression();
        consume(SEMICOLON, "");
        return new PrintStmt(value);
    }

    // Taget fra øvelsestimen. Matcher fint.
    private Stmt ifStmt() {
        consume(LEFT_PAREN, "");
        Expr cond = expression();
        consume(RIGHT_PAREN, "");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new IfStmt(cond, thenBranch, elseBranch);
    }

    private Stmt whileStmt() {
        consume(LEFT_PAREN,"");
        Expr cond = expression();
        consume(RIGHT_PAREN,"");
        Stmt body = statement();
        return new Stmt.WhileStmt(cond, body);
    }


    // C-E: Fra øvelsestimen med en lille undtagelse (left_brace er allerede matchet)
    private Stmt block() { // todo: allow syncs in blocks (Niels-Note)

        List<Stmt> statements = new LinkedList<>();
        while (!match(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(decl());
        }
        consume(RIGHT_BRACE, "");
        return new BlockStmt(statements);
    }


    // C-E: ------------ ExprStmt and nested functions -----------------
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


    // C-E: isAtEnd taget fra bog.
    private boolean isAtEnd() {
    return peek().type == EOF; // ser om næste token er EOF.
  }


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