package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.BOOL_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NOT;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NUMBER_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.STRING_TYPE;

import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;

public class TypeAnalyzer implements ExprVisitor<Type>, StmtVisitor<Void> {

    private SymbolTable currentEnvironment = new SymbolTable();
    private List<Stmt> program;

    public TypeAnalyzer(List<Stmt> program) {
        this.program = program;
    }

    public void analyse() {
        for (Stmt stmt : program) {
            analyse(stmt);
        }
        return;
    }

    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    private Type analyse(Expr expr) {
        return expr.accept(this);
    }

    /* ------------------------------------- Expressions / Statements relateret til Symbol Table. ALL COMPLETED -------------------------  */
    /* Type Error Policy: If new type is not compatible with old type, return the old type. If not possible (e.g. in binary expressions), return Type.UNKNOWN. */

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        currentEnvironment.define(varDecl.id.lexeme, analyse(varDecl.expr));
        return null;
        }

    @Override
    public Type visitIdentifierExpr(Identifier identifier) {
        return currentEnvironment.get(identifier.id.lexeme);
    }

    @Override
    public Type visitAssignExpr(Assign assign) {
        Type currType = currentEnvironment.get(assign.ID.lexeme);
        Type exprType = analyse(assign.expr);

        if (currType == exprType) {
            return currType;   
        }
        else {
            VVPLController.error(assign.ID.line, ErrorTypeStrings.TYPE_ERROR, "current type of identifier [insert name here] does not match the type of the given expression.");
            return currType; // return identifier's current type.
        }
    }

  /* ---------------------------- Expressions, nedefra og op af grammaren. ------------------------ */
    @Override 
    public Type visitLiteralExpr(Literal literal) {
        switch (literal.token.literal) {
            case Double d: 
                return Type.NUMBER;
            case Boolean b:
                return Type.BOOL;
            case String s:
                return Type.STRING;
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override 
    public Type visitUnaryExpr(Unary unary) {
        Type exprType = analyse(unary.expr);

        if (unary.operator.type == NOT && exprType != Type.BOOL) {
            VVPLController.error(unary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator NOT can only precede a boolean expression.");
            return exprType;
        }
        else {
            return exprType;
        }
    }

    @Override 
    public Type visitBinaryExpr(Binary binary) {
        Type left = analyse(binary.left);
        Type right = analyse(binary.right);

        switch (binary.operator.type) {
            case PLUS:
            case MINUS:
            case DIV:
            case MULT:    
                if (left == Type.NUMBER && right == Type.NUMBER) {
                    return Type.NUMBER;
                }
                else {
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] only accepts numbers.");
                    return Type.UNKNOWN;
                }
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
                if (left == Type.NUMBER && right == Type.NUMBER) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] only accepts numbers.");
                    return Type.UNKNOWN;
                }
            case EQUALS:
            case NOT_EQUALS:
                if (left == right) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] can only compare two expressions of the same type.");
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override   
    public Type visitLogicalExpr(Logical logical) {
        Type left = analyse(logical.left);
        Type right = analyse(logical.right);

        switch (logical.operator.type) {
            case OR, AND: 
                if (left == Type.BOOL && right == Type.BOOL) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(logical.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] only accepts booleans.");
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override 
    public Type visitCastExpr(Cast cast) {
        Type cast_from = analyse(cast.expr);    // Type of expression before cast
        TokenType cast_to = cast.typeToken.type;    // The type that we want to cast to. 
        switch (cast_from) {
            case NUMBER:
                if (cast_to == STRING_TYPE) {
                    return Type.STRING;
                }
                if (cast_to == BOOL_TYPE) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast NUMBER to string or bool.");
                    return cast_from;
                }
            case STRING:
                if (cast_to == NUMBER_TYPE) {
                     // TypeAnalyzer assumes this is always possible. Runtime error is returned in interpreter in the case that string contains non-digits.
                    return Type.NUMBER;
                }
                else {
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast string to number.");
                    return cast_from;
                }
            case BOOL:
                if (cast_to == NUMBER_TYPE) {
                    return Type.NUMBER;
                }
                else {
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast bool to number.");
                    return cast_from;
                }
            default:
                // Reachable if current type is UNKNOWN (an error has happened before this clause). 
                return Type.UNKNOWN;
        }
    }

    @Override //SKIP for now: Function related.
    public Type visitCallExpr(Call call) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    /* ------------------------- Statements ---------------------------- */
    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        analyse(exprStmt);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        Type condType = analyse(whileStmt.conditional);
        if (condType != Type.BOOL) {
            VVPLController.error(whileStmt.whileToken.line, ErrorTypeStrings.TYPE_ERROR, "conditional has to be of type Bool");
        }
        analyse(whileStmt.body);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        Type condType = analyse(ifStmt.cond);
        if (condType != Type.BOOL) {
            VVPLController.error(ifStmt.ifToken.line, ErrorTypeStrings.TYPE_ERROR, "conditional has to be of type Bool");
        }
        analyse(ifStmt.thenBlock);
        analyse(ifStmt.elseBlock);
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        Type exprType = analyse(printStmt.expr);
        if (exprType != Type.STRING) {
            VVPLController.error(printStmt.token.line, ErrorTypeStrings.TYPE_ERROR, "PrintStmt only accepts strings.");
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable(currentEnvironment);

        for (Stmt stmt : blockStmt.stmts) {
            analyse(stmt);
        }
        currentEnvironment = oldTable;
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        // TODO Auto-generated method stub
        Type returnType = analyse(returnStmt.returnValue); // skal evt sammenlignes med givne type af funktion.
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
    }
}
