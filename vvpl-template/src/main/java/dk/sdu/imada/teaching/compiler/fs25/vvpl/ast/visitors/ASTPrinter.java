package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public class ASTPrinter implements ExprVisitor<String>, StmtVisitor<String> {

  public String print(Stmt stmt) {
    return "";
  }
}
