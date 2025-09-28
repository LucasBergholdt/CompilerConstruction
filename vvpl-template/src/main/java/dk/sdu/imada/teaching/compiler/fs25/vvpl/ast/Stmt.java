package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.StmtVisitor;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public abstract class Stmt {
    public abstract <T> T accept(StmtVisitor<T> visitor);
}
