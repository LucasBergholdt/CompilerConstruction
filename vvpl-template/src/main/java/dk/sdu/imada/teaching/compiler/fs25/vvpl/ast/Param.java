package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/** 
 * This class is used to store the parameters of a 
 * function in the {@link Stmt.FunctionStmt} node.
 * @author Lucas Bergholdt Hansen 
*/
public class Param {
    public final Token id;
    public final Token typeToken;

    public Param(Token id, Token typeToken) {
        this.id = id;
        this.typeToken = typeToken;
    }
}
