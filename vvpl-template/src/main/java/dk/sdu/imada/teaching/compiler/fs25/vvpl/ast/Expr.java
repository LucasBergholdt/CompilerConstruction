package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ExprVisitor;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */
public abstract class Expr {
    public abstract <T> T accept(ExprVisitor<T> visitor);

    
    // --------- Nested Expr Classes -------------
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




    // C-E: what is this? Skal den ikke i Literal? Jeg har blot taget fra øvelsestimen.
    // LA: Som jeg ser det, bruger vi begge i parseren, hvor Identifier så er navnet på variablen (ID), og Literal selve værdien (false,true,string,int).
    
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

    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public List<Expr> arguments;

        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitCallExpr(this);
        }
    }

    public static class Cast extends Expr {
        public final Token type;
        public final Expr expr;

        public Cast(Token type, Expr expr) {
            this.type = type;
            this.expr = expr;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visitCastExpr(this);
        }
    }
}
