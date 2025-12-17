package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;

/**
 * Visitor interface for traversing and operating on statement AST nodes.
 * @param <T> the return type of the visitor methods
 * @version CompilerConstruction FT 2025
 */
public interface StmtVisitor<T> {

    T visitExprStmt(ExprStmt exprStmt);

    T visitWhileStmt(WhileStmt whileStmt);

    T visitIfStmt(IfStmt ifStmt);

    T visitPrintStmt(PrintStmt printStmt);

    T visitBlockStmt(BlockStmt blockStmt);

    T visitVarDecl(VarDecl varDecl); 

    T visitReturnStmt(ReturnStmt returnStmt);

    T visitFunctionStmt(FunctionStmt functionStmt);

}