package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.BOOL_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.FALSE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NUMBER;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NUMBER_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.STRING;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.STRING_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.TRUE;

import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;


public class TypeAnalyzer implements ExprVisitor<Token>, StmtVisitor<Void> {

    private SymbolTable currentEnviroment = new SymbolTable();
    private List<String> typeErrors = new LinkedList<>();
    private List<Stmt> program;

    public TypeAnalyzer(List<Stmt> program) {
        this.program = program;
    }

    public List<String> analyse() {
        for (Stmt stmt : program) {
            analyse(stmt);
        }
        return this.typeErrors;
    }

    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    private void analyse(Expr expr) {
        expr.accept(this);
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitExprStmt'");
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWhileStmt'");
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStmt'");
    }

    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        Token exprtoken = printStmt.expr.accept(this); // e.g. STRING_TYPE
            if (exprtoken.type != STRING) {
                typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + printStmt.token.line
                    + ": PrintStmt only accepts strings.");
            }
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBlockStmt'");
    }

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitVarDecl'");
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
    }

    @Override
    public Token visitAssignExpr(Assign assign) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitAssignExpr'");
    }

    @Override
    public Token visitLogicalExpr(Logical logical) {
        Token left = logical.left.accept(this);
        Token right = logical.right.accept(this);

        switch (logical.operator.type) {
            case NOT, OR, AND: 
                if (left.type != BOOL_TYPE || right.type != BOOL_TYPE) {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + logical.operator.line
                    + ": operator [insert name here] only accepts booleans.");
                }
        }
        return left;
    }

    @Override
    public Token visitBinaryExpr(Binary binary) {
        Token left = binary.left.accept(this);
        Token right = binary.right.accept(this);

        switch (binary.operator.type) {
            case PLUS, MINUS, DIV, MULT, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL:
                if (left.type != NUMBER_TYPE || right.type != NUMBER_TYPE) {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] only accepts numbers.");
                }
            case NOT_EQUALS, EQUALS:
                if (left.type != right.type) {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] can only compare two literals of the same type.");
                }
        }
        return left;    // Even if correct or not, we return the left token. We have to return one token in either case.
    }


    @Override //TODO
    public Token visitUnaryExpr(Unary unary) {
        return 
    }

    @Override
    public Token visitLiteralExpr(Literal literal) {
        return literal.token;
    }

    @Override
    public Token visitIdentifierExpr(Identifier identifier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIdentifierExpr'");
    }

    @Override
    public Token visitCallExpr(Call call) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    @Override
    public Token visitCastExpr(Cast cast) {
        Token cast_from = cast.expr.accept(this);    // Type of expression before cast
        Token cast_to = cast.type;                  // Token of the type we want to cast to.
        switch (cast_from.type) {
            case NUMBER_TYPE:
                if (cast_to.type == STRING_TYPE) {
                    // Return a STRING_TYPE.
                    return cast_to; //C-E Problem: Denne Token vil KUN have en type, e.g. "NUMBER_TYPE", men ikke 
                }
                if (cast_to.type == BOOL_TYPE) {
                    // Return a STRING_TYPE.
                    return cast_to;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.type.line
                    + ": can only cast NUMBER to string or bool.");
                }
            case STRING_TYPE:
                if (cast_to.type == NUMBER_TYPE) {
                    // prøver at caste fra String til Number. Kræver at alle elementer i String er tal.

                    if !(string_to_number_possible(cast_from.literal)) {
                        // go through string and see if it consists of digits only. If not, error
                        typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.type.line
                    + ": not possible to cast string to number.");
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.type.line
                    + ": can only cast string to number");
                }
        }
    }
}

    Boolean string_to_number_possible(Object string) {
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
        // Return true if string only consists of digits.
    }
}
