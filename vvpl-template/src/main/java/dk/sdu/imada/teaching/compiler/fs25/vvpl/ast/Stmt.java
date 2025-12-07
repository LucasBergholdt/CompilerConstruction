package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.StmtVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public abstract class Stmt {
    public abstract <T> T accept(StmtVisitor<T> visitor);


    // --------- Nested Stmt Classes -------------


    /** @author: Carl-Emil Dons Christensen */
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

    /** @author: Carl-Emil Dons Christensen */
    public static class IfStmt extends Stmt {
        public final Token ifToken; /* Literal "IF". */
        public final Expr cond;
        public final Stmt thenBlock;
        public final Stmt elseBlock;

        public IfStmt(Token ifToken, Expr cond, Stmt thenBlock, Stmt elseBlock) {
            this.ifToken = ifToken;
            this.cond = cond;
            this.thenBlock = thenBlock;
            this.elseBlock = elseBlock;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitIfStmt(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    public static class WhileStmt extends Stmt {
        public final Token whileToken;
        public final Expr conditional;
        public final Stmt body;

        public WhileStmt(Token whileToken, Expr conditional, Stmt body) {
            this.whileToken = whileToken;
            this.conditional = conditional;
            this.body = body;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitWhileStmt(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    public static class PrintStmt extends Stmt {
        public final Token token;
        public final Expr expr;

        public PrintStmt(Expr expr, Token token) {
            this.expr = expr;
            this.token = token;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitPrintStmt(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
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

    /** @author: Carl-Emil Dons Christensen */
    public static class VarDecl extends Stmt {
        public final Token id;
        public final Expr expr;
        public final Token typeToken;

        public VarDecl(Token id, Token typeToken, Expr expr) {
            this.id = id;
            this.typeToken = typeToken;
            this.expr = expr;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitVarDecl(this);
        }
    }

    /** @author: Lucas Bergholdt Hansen */
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

    /** @author: Lucas Bergholdt Hansen */
    public static class FunctionStmt extends Stmt {
        public final Token name;
        public final List<Param> params;
        public final Token typeToken;
        public final BlockStmt body;

        public FunctionStmt(Token name, List<Param> params, Token typeToken, BlockStmt body) {
            this.name = name;
            this.params = params;
            this.typeToken = typeToken;
            this.body = body;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visitFunctionStmt(this);
        }
    }

}

