package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Assign;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Binary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Call;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Cast;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Identifier;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Literal;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Logical;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Unary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.BlockStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ExprStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.FunctionStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.IfStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.PrintStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ReturnStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.VarDecl;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.WhileStmt;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

/* C-E note: Denne implementerer funktionaliteten af Visitor Interfaces. */
public class ASTPrinter implements ExprVisitor<String>, StmtVisitor<String> {
  private int indent = 0;

  public String print(Stmt stmt) {
    return stmt.accept(this);
    // return "";
  }

  @Override
  public String visitExprStmt(ExprStmt exprStmt) {
    System.out.println(" ".repeat(indent) + "ExprStmt");
    indent++;
    exprStmt.expr.accept(this);
    indent--;
    return null;

  }

  @Override
  public String visitWhileStmt(WhileStmt whileStmt) {
    System.out.println(" ".repeat(indent) + "WhileExpr");
    indent++;
    whileStmt.conditional.accept(this);
    whileStmt.body.accept(this);
    indent--;

    return null;
  }

  @Override
  public String visitIfStmt(IfStmt ifStmt) {

    System.out.println(" ".repeat(indent) + "IfStmt");
    indent++;
    ifStmt.cond.accept(this);
    indent--;
    indent++;
    ifStmt.thenBlock.accept(this);
    indent--;
    indent++;
    ifStmt.elseBlock.accept(this);

    indent--;

    return null;
  }

  @Override
  public String visitPrintStmt(PrintStmt printStmt) {
    System.out.println(" ".repeat(indent) + "PrintStmt");
    indent++;
    printStmt.expr.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitBlockStmt(BlockStmt blockStmt) {
    System.out.println(" ".repeat(indent) + "BlockStmt");
    indent++;
    for (Stmt stmt : blockStmt.stmts) {
      stmt.accept(this);
    }
    indent--;
    return null;
  }

  @Override
  public String visitVarDecl(VarDecl varDecl) {
    System.out.println(" ".repeat(indent) + "VarDecl");
    indent++;
    System.out.println(" ".repeat(indent) + varDecl.id.lexeme);
    System.out.println(" ".repeat(indent) + varDecl.type);
    varDecl.expr.accept(this);
    indent--;
    return null;

  }

  @Override
  public String visitAssignExpr(Assign assign) {
    System.out.println(" ".repeat(indent) + "AssignExpr");
    indent++;
    System.out.println(" ".repeat(indent) + assign.ID.lexeme);
    assign.expr.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitLogicalExpr(Logical logical) {
    System.out.println(" ".repeat(indent) + "LogicalExpr");
    indent++;
    logical.left.accept(this);
    indent--;

    System.out.println(" ".repeat(indent) + logical.operator.lexeme);

    indent++;
    logical.right.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitBinaryExpr(Binary binary) {
    System.out.println(" ".repeat(indent) + "LogicalExpr");
    indent++;
    binary.left.accept(this);
    indent--;

    System.out.println(" ".repeat(indent) + binary.operator.lexeme);

    indent++;
    binary.right.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitUnaryExpr(Unary unary) {
    System.out.println(" ".repeat(indent) + "UnaryExpr");
    indent++;
    System.out.println(" ".repeat(indent) + unary.operator.lexeme);
    unary.expr.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitLiteralExpr(Literal literal) {
    System.out.println(" ".repeat(indent) + "LiteralExpr");
    indent++;
    System.out.println(" ".repeat(indent) + literal.token.lexeme);
    indent--;
    return null;
  }

  @Override
  public String visitIdentifierExpr(Identifier identifier) {
    System.out.println(" ".repeat(indent) + "VariableExpr");
    indent++;
    System.out.println(" ".repeat(indent) + identifier.id.lexeme);
    identifier.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitCallExpr(Call call) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
  }

  @Override
  public String visitReturnStmt(ReturnStmt returnStmt) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
  }

  @Override
  public String visitFunctionStmt(FunctionStmt functionStmt) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
  }

  @Override
  public String visitCastExpr(Cast cast) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitCastExpr'");
  }

}
