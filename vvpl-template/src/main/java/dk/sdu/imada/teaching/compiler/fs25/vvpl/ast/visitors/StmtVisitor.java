package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;

/**
 * @author Sandra Greiner
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