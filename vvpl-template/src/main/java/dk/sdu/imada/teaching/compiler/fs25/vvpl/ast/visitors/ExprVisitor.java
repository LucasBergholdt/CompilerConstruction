package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
//import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Assign;



/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public interface ExprVisitor<T> {

    T visitAssignExpr(Assign assign);

    T visitLogicalExpr(Logical logical);

    T visitBinaryExpr(Binary binary);

    T visitUnaryExpr(Unary unary);

    T visitLiteralExpr(Literal literal);

    T visitIdentifierExpr(Identifier identifier);

    T visitCallExpr(Call call);

    T visitCastExpr(Cast cast);    
 
}