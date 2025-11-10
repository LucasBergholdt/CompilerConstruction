package dk.sdu.teaching.compiler.fs24.spl.ast;

public abstract class Stmt {
	// public abstract <T> T accept(IRVisitor visitor);

    public abstract <T> T accept(StmtVisitor<T> visitor);
}
