package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ExprVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/**
 * Abstract base class for all expression AST nodes.
 * Each subclass represents a specific kind of expression and implemets the 
 * accept method by redirecting the call to the proper visitor method.
 * @version CompilerConstruction FT 2025
 */
public abstract class Expr {
    /**
     * Accepts a visitor that implements operations for this expression node.
     * @param <T> the return type of the visitor
     * @param visitor the visitor to accept
     * @return the result of the visitor's operation
     */
    public abstract <T> T accept(ExprVisitor<T> visitor);

    
    // ------------- Nested Expr Classes -------------

    /** @author: Carl-Emil Dons Christensen */
    public static class Assign extends Expr {
        public final Token ID;
        public final Expr expr;

        public Assign(Token iD, Expr expr) {
            ID = iD;
            this.expr = expr;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitAssignExpr(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    public static class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitBinaryExpr(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    public static class Logical extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitLogicalExpr(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    public static class Unary extends Expr {
        public final Token operator;
        public final Expr expr;

        public Unary(Token operator, Expr expr) {
            this.operator = operator;
            this.expr = expr;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitUnaryExpr(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    public static class Literal extends Expr {
        public final Token token;
        
        public Literal(Token token) {
            this.token = token;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitLiteralExpr(this);
        }
    }

    /** @author: Carl-Emil Dons Christensen */
    public static class Identifier extends Expr {
        public final Token id;

        public Identifier(Token id) {
            this.id = id;
        }

        @Override
        public <T> T accept(ExprVisitor<T> e) {
            return e.visitIdentifierExpr(this);
        }
    }

    /** @author: Lucas Bergholdt Hansen */
    public static class Call extends Expr {
        public final Token id;      // Set if call to a function
        public final Expr callee;
        public final Token paren;   // Set if call to a function
        public List<Expr> arguments;

        public Call(Token id, Expr callee, Token paren, List<Expr> arguments) {
            this.id = id;
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitCallExpr(this);
        }
    }

    /** @author: Lucas Bergholdt Hansen */ 
    public static class Cast extends Expr {
        public final Token typeToken;    // token efter "cast_to"
        public final Expr expr;

        public Cast(Token typeToken, Expr expr) {
            this.typeToken = typeToken;
            this.expr = expr;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitCastExpr(this);
        }
    }
}
