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
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.VarDecl;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.*;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public class Parser {

    private List<Token> tokens;
    int current = 0;
    // List<Stmt> program = new LinkedList<>();    /* C-E: gammel? skal måske fjernes */

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }


        // program := decl* EOF
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        
        // C-E: Her version fra bog. Niels-Erik lavede den anderledes. Jeg synes bogen giver mere mening
        while (!isAtEnd()) {    //tjekker for EOF
            try {
                statements.add(decl());
            } catch (ParseError e) {
                // synchronise();
            }

        }
        return statements;
    }


    // ------------------ decl := VarStmt | Statement | FuncDecl --------------------

    private Stmt decl() {
        if (match(FUNCTION)) {
            return function();
        } else if (match(VAR)) {
            return varDecl();
        } else {
            return statement();
        }
    }


    // --------- Lasse: Muligvis her min error optræder. Evt. brug min "fail(...)" til at undersøge det --------- //
    // Hvis i bruger denne, kan i se, at den formår at detektere en literal i ".expr". Derfor burde den ikke evaluere til null?

    private Stmt varDecl() {
        // When entering this method "variable" has already been consumed by match in decl()
        
        // Case: "variable ____ 'has_type' ____
        // We expect an ID (name of variable):
        Token id = consume(IDENTIFIER, "Expected identifier");

        // We expect "has_type":
        consume(TYPE_DEF, "Expected 'has_type'");
        
        // Expecting a NumberType, StringType or BoolType:
        TokenType type = null;       // TODO: Måske skal det her være hele token vi gemmer og ikke bare dens type?

        if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
            type = previous().type;
        } else {
            throw new ParseError();
        }

        // Optional "is" expr:

        Expr expr = null;
        if (match(ASSIGN)) {
            // Case: "variable ____ 'has_type' ____ 'is' expression()
            expr = expression();
        }

        // Expecting semicolon at end:
        consume(SEMICOLON, "Expected ';'");

        // FOR DEBUGGING ONLY //
        // fail(id.toString() + " " + type.toString() + " " + expr.toString());

        return new Stmt.VarDecl(id, type, expr);
    }



        // ------------------ All statements in statement except exprStmt -----------------------

        //Taget fra øvelsestimen. Matcher fint.
    private Stmt statement() {
        if (match(PRINT))
            return printStmt();

        if (match(IF))
            return ifStmt();

        if (match(WHILE))
            return whileStmt();

        if (match(LEFT_BRACE))
            return new Stmt.BlockStmt(block()); // Lucas: Står nu for at wrappe resultat af block() i en Stmt.BlockStmt.

        if (match(RETURN))
            return returnStmt();

        return exprStmt();
    }

     // Taget fra øvelsestimen. Matcher fint.
    private Stmt printStmt() {
        Expr value = expression();
        consume(SEMICOLON, "Expected semicolon");
        return new Stmt.PrintStmt(value);
    }

    // Taget fra øvelsestimen. Matcher fint.
    private Stmt ifStmt() {
        consume(LEFT_PAREN, "Expected '('");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')'");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.IfStmt(cond, thenBranch, elseBranch);
    }

    private Stmt whileStmt() {
        consume(LEFT_PAREN, "Expected '('");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')'");
        Stmt body = statement();
        return new Stmt.WhileStmt(cond, body);
    }


    // C-E: Fra øvelsestimen med en lille undtagelse (left_brace er allerede matchet)
    /* Lucas: Har ændret så den her returner List<Stmt> i stedet. Det er for at den kan bruges i function().
              Det er nu i stedet statement(), der wrapper den her liste ind i en Stmt.Block. 
              Det er sådan bogen gør det, men de noterer dog også at det ser lidt underligt ud at gøre det sådan her.
              Men det er altså for at man kan bruge block() i function().*/
    private List<Stmt> block() { // todo: allow syncs in blocks (Niels-Note)
        List<Stmt> statements = new LinkedList<>();
        
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(decl());
        }

        consume(RIGHT_BRACE, "Expected '}'");
        return statements;
    }

    private Stmt returnStmt() {
        Token returnKeyword = previous(); // Kept for error reporting
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expected ';'");
        return new Stmt.ReturnStmt(returnKeyword, value);
    }


    // ------------ ExprStmt and nested functions -----------------
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
                // TODO: kald error funktion (bogen siger man ikke skal throw error her)
            } else {
                // Case: Identifier = ... 
                Token id = ((Identifier)expr).id;
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

    private Expr compr() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right); 
        }

        return expr;
    }

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
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr expr = unary();
            return new Expr.Unary(operator, expr);
        } else {
            return call();
        }
    }

    private Stmt function() {
        Token name = consume(IDENTIFIER, "Expected function name");
        consume(LEFT_PAREN, "Expected '('");
        // If we don't have a closing ')' right after we have params:
        List<Token> params = new ArrayList<>();
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
                throw new ParseError();
            }
        }

        consume(LEFT_BRACE, "Expected '{'"); // block() assumes { has already been consumed
        List<Stmt> body = block();
        return new Stmt.FunctionStmt(name, params, type, body);
    }

    // TODO: Virker ikke lige nu. Skal have valgt hvordan vi opbevarer params.
    private List<Token> params() {
        List<Token> params = new ArrayList<>();

        Token id = consume(IDENTIFIER, "Expected identifier");
        consume(TYPE_DEF, "Expected 'has_type'");
        Token type = null;
        if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
            type = previous();
        } else {
            throw new ParseError();
        }

        while(match(COMMA)) {
            Token id2 = consume(IDENTIFIER, "Expected identifier");
            consume(TYPE_DEF, "Expected 'has_type'");
            Token type2 = null;
            if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
                type = previous();
            } else {
                throw new ParseError();
            }
        }
        return null;
    }

    private Expr call() {
        Expr expr = primary();

        if (match(LEFT_PAREN)) {
            List<Expr> arguments;
            if (peek().type == RIGHT_PAREN) {
                arguments = new ArrayList<>(); // Empty list
            } else {
                arguments = args();
            }
            // Book stores right paren token to use it for reporting rumtime errors caused by a function call
            Token paren = consume(RIGHT_PAREN, "Expected ')'");
            
            return new Expr.Call(expr, paren, arguments);
        }

        return expr;
    }

    private List<Expr> args() {
        List<Expr> arguments = new ArrayList<>();
        arguments.add(expression()); // handling first "expr" in grammar

        // Handling ("," expr)*:
        while (match(COMMA)) { 
            arguments.add(expression());
        }
        return arguments;
    }

    // Handle the cast that might appear
    private Expr primary() {
        // Check if cast is specified
        if (match(CAST)) {
            // Expecting a NumberType, StringType or BoolType:
            Token typeToken = null;
            if (match(NUMBER_TYPE, STRING_TYPE, BOOL_TYPE)) {
                typeToken = previous();
            } else {
                throw new ParseError();
            }

            // Create new Expr.Cast:
            Expr expr = primaryNoCast();
            return new Expr.Cast(typeToken, expr);

        } else { // Otherwise just parse the primary (literal / identifier / grouping)
            return primaryNoCast();
        }
    }

    // C-E: Kan også laves med Match (gøres i bogen). For nu tages Niels Erik's løsning.
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

            /* Ellers regner vi med en grouping. Gøres lidt anderledes i bogen. */
            default:
                consume(LEFT_PAREN, "Expected '('");
                expr = expression();
                consume(RIGHT_PAREN, "Expected ')'");
                return expr;
        }
    }




    /* Fra øvelses time. Copy-pasted for nu */


    // private void synchronise() {
    //     // todo: what happens with end of file
    //     while (true) {
    //         switch (peek().type) {
    //             case IF:
    //             case WHILE:
    //             case PRINT:
    //             case LEFT_BRACE:
    //             case VAR:
    //                 return;

    //             case SEMICOLON:
    //                 current++;
    //                 return;

    //             default:
    //                 break;
    //         }
    //         current++;
    //     }
    // }
     









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


    // C-E: Taget fra bogen.
    private boolean isAtEnd() {
    return peek().type == EOF; // ser om næste token er EOF.
  }


    /* C-E: Taget fra bogen og ændret lidt for ikke at anvende check() og advance(). */
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


    /* Consumes if given token matches current token, otherwise throws error. */
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

    private Token consume(TokenType type, String message) {
        if (check(type)) { // If we recieve the token type we expect, move on. 
            return advance();
        } else {
            System.out.println(message);
            throw new ParseError();
        }
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private class ParseError extends RuntimeException {
    }



}