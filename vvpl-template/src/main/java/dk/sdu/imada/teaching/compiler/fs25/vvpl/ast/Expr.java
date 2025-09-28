package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.ExprVisitor;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */
public abstract class Expr {
    public abstract <T> T accept(ExprVisitor<T> visitor);
}
