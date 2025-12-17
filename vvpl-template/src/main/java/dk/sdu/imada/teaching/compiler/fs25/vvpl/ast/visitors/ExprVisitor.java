package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;

/**
 * Visitor interface for traversing and operating on expression AST nodes.
 * @param <T> the return type of the visitor methods
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