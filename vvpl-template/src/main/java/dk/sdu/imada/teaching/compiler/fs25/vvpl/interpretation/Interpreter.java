package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;

public class Interpreter implements StmtVisitor<Void>, ExprVisitor<Object> {

    private Environment env = new Environment(null);

    public void interpret(List<Stmt> stmts) {
        // execute
        try {

            for (Stmt s : stmts) {
                execute(s);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    private void execute(Stmt s) {
        s.accept(this);
    }

    private Object evaluate(Expr e) {
        return e.accept(this);
    }

    @Override
    public Object visitAssignExpr(Assign assign) {
        Object value = evaluate(assign.expr);
        env.assign(assign.ID.lexeme, value);
        return value;
    }

    // C-E: Skal returnere den Expr som assignment er bundet til.
    @Override
    public Object visitIdentifierExpr(Identifier identifier) {
        return env.get(identifier.id.lexeme);

    }

    @Override
    public Object visitLiteralExpr(Literal literal) {
        return literal.token.literal;   // Retrieve literal value from token.
    }

// C-E: TODO. Ikke færdig.
    @Override
    public Object visitLogicalExpr(Logical logical) {

        Object left = evaluate(logical.left);
        Object right = evaluate(logical.right);

        switch (logical.operator.type) {
            default:
                break;
        }

        // Unreachable
        return null;
    }


    @Override
    public Object visitBinaryExpr(Binary binary) {
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        switch (binary.operator.type) {
            case MINUS:
                return (double)left - (double)right;
            case PLUS:
                return (double)left + (double)right;
            case DIV:
                return (double)left / (double)right;
            case MULT:
                return (double)left * (double)right;
            case NOT_EQUALS:
                return !isEqual(left, right);
            case EQUALS: 
                return isEqual(left, right);
            case GREATER:
                return (double) left > (double) right;
        }
        return null;
    }
        private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;
        return left.equals(right);
    }




    @Override
    public Object visitUnaryExpr(Unary unary) {
        Object right = evaluate(unary.expr);

        switch (unary.operator.type) {
            case MINUS:
                return -(double)right;
            case NOT:
                return !isTruthy(right);
        }
    // Unreachable.
        return null;
    }

    // C-E: Hjælpefunktion til visitUnary. Gør at alle datatyper kan anses som Booleans.
    private boolean isTruthy(Object object) {
        if (object == null) 
            return false;
        if (object instanceof Boolean) 
            return (boolean)object;
        return true;
    }

    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        Environment prev = env;
        this.env = new Environment(prev);
        for (Stmt s : blockStmt.stmts) {
            execute(s);
        }
        this.env = prev;
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        Object value = null;
        /* Hvis VarDecl har fået en expr med, så evaluér denne. */
        if (varDecl.expr != null) {
            value = evaluate(varDecl.expr);
        }
        env.define(varDecl.id.lexeme, value);
        return null;
    }

    @Override
    public Object visitCallExpr(Call call) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    @Override
    public Object visitCastExpr(Cast cast) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCastExpr'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPrintStmt'");
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

    /* CE: Dette havde hun i sin interpreter
    @Override
    public Object visitVar(Variable variable) {
        return null;
        // return env.get(variable.name);
    }
     */
}

