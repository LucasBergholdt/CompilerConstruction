package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ExprVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */
public abstract class Expr {
    public abstract <T> T accept(ExprVisitor<T> visitor);

    
    // --------- Nested Expr Classes -------------
    public class Assign extends Expr {
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

    public class Binary extends Expr {
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

    public class Logical extends Expr {
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


    public class Unary extends Expr {
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

    public class Literals extends Expr {
        public final Token token;

        public Literals(Token token) {
            this.token = token;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitLiteralsExpr(this);
        }
    }




    // C-E: what is this? Skal den ikke i Literal? Jeg har blot taget fra øvelsestimen.
    public class Identifier extends Expr {
        public final Token id;

        public Identifier(Token id) {
            this.id = id;
        }

        @Override
        public <T> T accept(ExprVisitor<T> e) {
            return e.visitIdentifierExpr(this);
        }
    }
}
