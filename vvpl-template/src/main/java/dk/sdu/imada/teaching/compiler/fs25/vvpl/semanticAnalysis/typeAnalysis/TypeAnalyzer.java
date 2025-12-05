package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.BOOL_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.FALSE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NOT;
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

public class TypeAnalyzer implements ExprVisitor<Type>, StmtVisitor<Void> {

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

    /* ------------------------------------- Expressions / Statements relateret til Symbol Table. ALL COMPLETED -------------------------  */
    /* Type Error Policy: If new type is not compatible with old type, return the old type. If not possible (e.g. in binary expressions), return Type.UNKNOWN. */

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        if (varDecl.expr == null) {
            typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + varDecl.id.line
                    + ": VarDecl must be initialized with a value.");
            return null;
        }
        else {
            currentEnviroment.define(varDecl.id.lexeme, varDecl.expr.accept(this));
            return null;
        }
    }

    @Override
    public Type visitIdentifierExpr(Identifier identifier) {
        return currentEnviroment.get(identifier.id.lexeme);
    }

    @Override
    public Type visitAssignExpr(Assign assign) {
        Type currType = currentEnviroment.get(assign.ID.lexeme);
        Type exprType = assign.expr.accept(this);

        if (currType == exprType) {
            return currType;   
        }
        else {
            typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + assign.ID.line
                    + ": current type of identifier [insert name here] does not match the type of the given expression");
            return currType; // return identifier's current type.
        }
    }

  /* ---------------------------- Expressions, nedefra og op af grammaren. ------------------------ */
    @Override // COMPLETED
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

    @Override // COMPLETED
    public Type visitUnaryExpr(Unary unary) {
        Type exprType = unary.expr.accept(this);

        if (unary.operator.type == NOT && exprType != Type.BOOL) {
            typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + unary.operator.line
                    + ": operator NOT can only precede a boolean expression.");
            return exprType;
        }
        else {
            return exprType;
        }
    }

    @Override // COMPLETED
    public Type visitBinaryExpr(Binary binary) {
        Type left = binary.left.accept(this);
        Type right = binary.right.accept(this);

        switch (binary.operator.type) {
            case PLUS:
            case MINUS:
            case DIV:
            case MULT:    
                if (left == Type.NUMBER && right == Type.NUMBER) {
                    return Type.NUMBER;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] only accepts numbers.");
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
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] only accepts numbers.");
                    return Type.UNKNOWN;
                }
            case EQUALS:
            case NOT_EQUALS:
                if (left == right) {
                    return Type.BOOL;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] can only compare two expressions of the same type.");
                    return Type.UNKNOWN;
                }

            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override   // COMPLETED
    public Type visitLogicalExpr(Logical logical) {
        Type left = logical.left.accept(this);
        Type right = logical.right.accept(this);

        switch (logical.operator.type) {
            case OR, AND: 
                if (left == Type.BOOL && right == Type.BOOL) {
                    return Type.BOOL;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + logical.operator.line
                    + ": operator [insert name here] only accepts booleans.");
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override // COMPLETED
    public Type visitCastExpr(Cast cast) {
        Type cast_from = cast.expr.accept(this);    // Type of expression before cast
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
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.typeToken.line
                    + ": can only cast NUMBER to string or bool.");
                    return cast_from;
                }
            case STRING:
                if (cast_to == NUMBER_TYPE) {
                     // TypeAnalyzer assumes this is always possible. Runtime error is returned in interpreter in the case that string contains non-digits.
                    return Type.NUMBER;
                    }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.typeToken.line
                    + ": can only cast string to number");
                    return cast_from;
                }
            case BOOL:
                if (cast_to == NUMBER_TYPE) {
                    return Type.NUMBER;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.typeToken.line
                    + ": can only cast string to number");
                    return cast_from;
                }
            default:
                // Reachable if current type is UNKNOWN (an error has happened before this clause). 
                return Type.UNKNOWN;
        }
    }

    @Override //SKIP: Function related.
    public Type visitCallExpr(Call call) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    /* ------------------------- Statements ---------------------------- */
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
        Type exprType = printStmt.expr.accept(this);
            if (exprType != Type.STRING) {
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
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
    }
}
