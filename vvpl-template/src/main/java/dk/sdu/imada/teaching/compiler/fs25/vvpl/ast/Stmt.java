package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import java.beans.Expression;
import java.util.List;
import java.util.function.Function;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.StmtVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public abstract class Stmt {
    public abstract <T> T accept(StmtVisitor<T> visitor);


    // --------- Nested Stmt Classes -------------
    public static class ExprStmt extends Stmt {
        public final Expr expr;

        public ExprStmt(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitExprStmt(this);
        }
    }

    public static class IfStmt extends Stmt {
        public final Expr cond;
        public final Stmt thenBlock;
        public final Stmt elseBlock;

        public IfStmt(Expr cond, Stmt thenBlock, Stmt elseBlock) {
            this.cond = cond;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitIfStmt(this);
        }
    }

    public static class WhileStmt extends Stmt {
        public final Expr conditional;
        public final Stmt body;

        public WhileStmt(Expr conditional, Stmt body) {
            this.conditional = conditional;
            this.body = body;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitWhileStmt(this);
        }
    }

    public static class PrintStmt extends Stmt {
        public final Expr expr;

        public PrintStmt(Expr expr) {
            this.expr = expr;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitPrintStmt(this);
        }
    }

    public static class BlockStmt extends Stmt {
        public final List<Stmt> stmts;

        public BlockStmt(List<Stmt> stmts) {
            this.stmts = stmts;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitBlockStmt(this);
        }
    }


    public static class VarDecl extends Stmt {
        public final Token id;
        public final Expr expr;
        public final TokenType type;

        public VarDecl(Token id, TokenType type, Expr expr) {
            this.id = id;
            this.type = type;
            this.expr = expr;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitVarDecl(this);
        }
    }

    public static class ReturnStmt extends Stmt {
        public final Token returnKeyword;
        public final Expr returnValue;

        public ReturnStmt(Token returnKeyword, Expr returnValue) {
            this.returnKeyword = returnKeyword;
            this.returnValue = returnValue;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitReturnStmt(this);
        }
    }

    public static class FunctionStmt extends Stmt {
        public final Token name;
        public final List<Token> params;
        public final Token type;
        public final List<Stmt> body;

        public FunctionStmt(Token name, List<Token> params, Token type, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.type = type;
            this.body = body;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitFunctionStmt(this);
        }
    }


        
}


    


