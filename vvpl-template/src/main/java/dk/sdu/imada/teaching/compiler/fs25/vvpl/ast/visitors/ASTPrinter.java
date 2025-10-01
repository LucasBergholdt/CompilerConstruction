package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */


 /* C-E note: Denne implementerer funktionaliteten af Visitor Interfaces. */
public class ASTPrinter implements ExprVisitor<String>, StmtVisitor<String> {

  public String print(Stmt stmt) {
    return "";
  }
}
