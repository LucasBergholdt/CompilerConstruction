package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Assign;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Binary;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Call;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Cast;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Identifier;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Literal;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Logical;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.Unary;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.BlockStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ExprStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.FunctionStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.IfStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.PrintStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.ReturnStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.VarDecl;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.WhileStmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

/**
 * @author Lasse Arpe Kristensen
 * @version CompilerConstruction FT 2025
 */

 public class ASTPrinter implements ExprVisitor<String>, StmtVisitor<String> {
  private int indent = 0;

    private static final Map<TokenType, String> reverse_keywords;
      static {
		reverse_keywords = new HashMap<>();
        reverse_keywords.put(TokenType.BOOL_TYPE, "Bool");
        reverse_keywords.put(TokenType.NUMBER_TYPE, "Number");
        reverse_keywords.put(TokenType.STRING_TYPE, "String");        
	}

  public String print(Stmt stmt) {
    return stmt.accept(this);
  }

  @Override
  public String visitExprStmt(ExprStmt exprStmt) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("ExprStmt"));
    indent++;
    sb.append(exprStmt.expr.accept(this));
    indent--;
    return sb.toString();
  }

  @Override
  public String visitWhileStmt(WhileStmt whileStmt) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("WhileStmt"));

    indent++;
    sb.append(whileStmt.conditional.accept(this));
    sb.append(whileStmt.body.accept(this));
    indent--;

    return sb.toString();
  }

  @Override
  public String visitIfStmt(IfStmt ifStmt) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("IfStmt"));

    indent++;
    sb.append(ifStmt.cond.accept(this));

    sb.append(ifStmt.thenBlock.accept(this));

    if (ifStmt.elseBlock != null) {
      sb.append(ifStmt.elseBlock.accept(this));
    }
    indent--;

    return sb.toString();
  }

  @Override
  public String visitPrintStmt(PrintStmt printStmt) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("PrintStmt"));
    indent++;
    sb.append(printStmt.expr.accept(this));
    indent--;
    return sb.toString();
  }

  @Override
  public String visitBlockStmt(BlockStmt blockStmt) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("BlockStmt"));

    indent++;
    for (Stmt stmt : blockStmt.stmts) {
      sb.append(stmt.accept(this));
    }
    indent--;
    return sb.toString();
  }


  public String visitVarDecl(VarDecl varDecl) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("VarDecl"));
    indent++;
    sb.append(indentString(varDecl.id.lexeme));

    sb.append(indentString(reverse_keywords.get(varDecl.typeToken.type)));

    if (varDecl.expr != null) {
      sb.append(varDecl.expr.accept(this));
    }

    indent--;
    return sb.toString();

  }

  /*Prepends the appropriate amount of indentation to strings.*/
  private String indentString(String text) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append("  ");
    }
    sb.append(text).append(System.lineSeparator());
    return sb.toString();
  }



  @Override
  public String visitAssignExpr(Assign assign) {
      StringBuilder sb = new StringBuilder();

    sb.append(indentString("AssignExpr"));
    indent++;
    sb.append(indentString(assign.ID.lexeme));
    sb.append(assign.expr.accept(this));
    indent--;

    return sb.toString();
  }

  @Override
  public String visitLogicalExpr(Logical logical) {

    StringBuilder sb = new StringBuilder();

    sb.append(indentString("LogicalExpr"));

    indent++;
    sb.append(logical.left.accept(this));

    sb.append(indentString(logical.operator.lexeme));

    sb.append(logical.right.accept(this));
    indent--;
    return sb.toString();
  }

  @Override
  public String visitBinaryExpr(Binary binary) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("BinaryExpr"));
    indent++;
    sb.append(binary.left.accept(this));

    sb.append(indentString(binary.operator.lexeme));
    
    sb.append(binary.right.accept(this));

    indent--;
    return sb.toString();
  }

  @Override
  public String visitUnaryExpr(Unary unary) {
    StringBuilder sb = new StringBuilder();
    
    sb.append(indentString("UnaryExpr"));
    indent++;
    sb.append(indentString(unary.operator.lexeme));
    sb.append(unary.expr.accept(this));
    indent--;

    return sb.toString();
  }
  

  // Leaf.
  @Override
  public String visitLiteralExpr(Literal literal) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("LiteralExpr"));

    indent++;

    String lexeme = literal.token.lexeme;

    // Converts to double if digit. (And back to string).
    if (isDigit(lexeme.charAt(0))) {
      double newNum = Double.parseDouble(lexeme);
      sb.append(indentString(newNum+""));
    } else {
      sb.append(indentString(lexeme));
    }

    indent--;

    return sb.toString();
  }

  // Leaf.
  @Override
  public String visitIdentifierExpr(Identifier identifier) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("VariableExpr"));

    indent++;

    sb.append(indentString(identifier.id.lexeme));
    indent--;
    return sb.toString();
  }

  
  @Override
  public String visitCastExpr(Cast cast) {
    StringBuilder sb = new StringBuilder();

    sb.append(indentString("Cast_To " + cast.typeToken.lexeme));    
    indent++;
    sb.append(cast.expr.accept(this));
    indent--;

    return sb.toString();

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

 /*Helpers */

  // Checks if string is a digit (via first digit).
    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

}
