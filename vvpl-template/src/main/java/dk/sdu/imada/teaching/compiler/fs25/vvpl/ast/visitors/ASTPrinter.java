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
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.*;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

 /* C-E note: Denne implementerer funktionaliteten af Visitor Interfaces. */

/*Lasse: Skal vi sørge for alle tal implementeres som doubles? jf. Sandras besked på ItsLearning */
// LA: Overvej desuden om alle optionals er tilstrækkeligt opfyldt. 
// Eksempel på dette i visitVarDecl.

// Problemer:
// 1. Fra "BOOL_TYPE" TIL "Bool". Se mine noter til dette i visitVarDecl.
// 2. At få den rigtige rækkefølge ift. Cast_To og LiteralExpression.
// 3. Lave alle tal til doubels? E.g "123.0".


 public class ASTPrinter implements ExprVisitor<String>, StmtVisitor<String> {
  private int indent = 0;

  public String print(Stmt stmt) {
    return stmt.accept(this);
    // return "";
  }

  @Override
  public String visitExprStmt(ExprStmt exprStmt) {
    System.out.println("  ".repeat(indent) + "ExprStmt");
    indent++;
    exprStmt.expr.accept(this);
    indent--;
    return null;

  }

  @Override
  public String visitWhileStmt(WhileStmt whileStmt) {
    System.out.println("  ".repeat(indent) + "WhileExpr");
    indent++;
    whileStmt.conditional.accept(this);
    whileStmt.body.accept(this);
    indent--;

    return null;
  }

  @Override
  public String visitIfStmt(IfStmt ifStmt) {

    System.out.println("  ".repeat(indent) + "IfStmt");
    indent++;
    ifStmt.cond.accept(this);
    // indent--;
    // indent++;
    ifStmt.thenBlock.accept(this);
    // indent--;
    // indent++;
    if (ifStmt.elseBlock != null) {
      ifStmt.elseBlock.accept(this);
    }


    indent--;

    return null;
  }

  @Override
  public String visitPrintStmt(PrintStmt printStmt) {
    System.out.println("  ".repeat(indent) + "PrintStmt");
    indent++;
    printStmt.expr.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitBlockStmt(BlockStmt blockStmt) {
    System.out.println("  ".repeat(indent) + "BlockStmt");
    indent++;
    for (Stmt stmt : blockStmt.stmts) {
      stmt.accept(this);
    }
    indent--;
    return null;
  }

  @Override
  public String visitVarDecl(VarDecl varDecl) {
    System.out.println("  ".repeat(indent) + "VarDecl");
    indent++;
    System.out.println("  ".repeat(indent) + varDecl.id.lexeme);
    // TODO: Finde en måde, hvorpå den reelle type som fx "BOOL_TYPE", kan
    // repræsenteres som "Bool".

    // Idé: Lave en hjælpefunktion, som kan "mappe tilbage" fra value til key.
    // Kræver vi importerer keywords til denne fil, og laver en funktion, e.g "getKeyByValue(E value)".
    // Denne skal så bruges i alle tilfælde, hvor vi skal vise "typen".

    //La: Nogen bedre bud? :)

    System.out.println("  ".repeat(indent) + varDecl.type);

    if (varDecl.expr != null) {
      varDecl.expr.accept(this);
    }

    indent--;
    return null;

  }

  public String visitVarDecl2(VarDecl varDecl) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("VarDecl"));
    indent++;
    sb.append(indentString(varDecl.id.lexeme));

    sb.append(indentString(varDecl.type.toString()));

    if (varDecl.expr != null) {
      varDecl.expr.accept(this);
    }

    indent--;
    return sb.toString();

  }

  private String indentString(String text) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append("  ");
    }
    sb.append(text).append(System.lineSeparator()); // System.lineSeperator = \n men specifik for brugerens OS system.
    return sb.toString();
  }



  @Override
  public String visitAssignExpr(Assign assign) {
    System.out.println("  ".repeat(indent) + "AssignExpr");
    indent++;
    System.out.println("  ".repeat(indent) + assign.ID.lexeme);
    assign.expr.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitLogicalExpr(Logical logical) {
    System.out.println("  ".repeat(indent) + "LogicalExpr");
    indent++;
    logical.left.accept(this);
    // indent--;

    System.out.println("  ".repeat(indent) + logical.operator.lexeme);

    // indent++;
    logical.right.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitBinaryExpr(Binary binary) {
    System.out.println("  ".repeat(indent) + "BinaryExpr");
    indent++;
    binary.left.accept(this);
    // indent--;

    System.out.println("  ".repeat(indent) + binary.operator.lexeme);

    // indent++;
    binary.right.accept(this);
    indent--;
    return null;
  }

  @Override
  public String visitUnaryExpr(Unary unary) {
    System.out.println("  ".repeat(indent) + "UnaryExpr");
    indent++;
    System.out.println("  ".repeat(indent) + unary.operator.lexeme);
    unary.expr.accept(this);
    indent--;
    return null;
  }
  
  // Leaf.

 // Der er måske en bug i rækkefølgen hvorpå vi modtager hhv. casts og literals?
 // Men så ville der også være en fejl i vores eksempel, så nok ikke.
// Det er som om, at Cast_To bliver kaldt inden Literal.

  @Override
  public String visitLiteralExpr(Literal literal) {

    System.out.println("  ".repeat(indent) + "LiteralExpr");
    indent++;

    // HER BURDE CAST_TO PRINTE FØR VORES LEXEME

    System.out.println("  ".repeat(indent) + literal.token.lexeme);
  
    indent--;
    return null;
  }

  // Leaf.
  @Override
  public String visitIdentifierExpr(Identifier identifier) {
    System.out.println("  ".repeat(indent) + "VariableExpr");
    indent++;
    System.out.println("  ".repeat(indent) + identifier.id.lexeme);
    indent--;
    return null;
  }

  // ---Lasse: Problematisk ift. rækkefølgen vi modtager tokens på, og printe det rigtigt? ---.
  @Override
  public String visitCastExpr(Cast cast) {
    // indent++;
    System.out.println("  ".repeat(indent) + "Cast_To " + cast.type.lexeme);
    cast.expr.accept(this);

    // indent--;
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

}
