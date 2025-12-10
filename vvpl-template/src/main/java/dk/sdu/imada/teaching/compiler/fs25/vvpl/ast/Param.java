package dk.sdu.imada.teaching.compiler.fs25.vvpl.ast;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/** @author Lucas Bergholdt Hansen 
 * This class is used to store the parameters of a function.
 * See class FunctionStmt in Stmt.java.
*/
public class Param {
    public final Token id;
    public final Token typeToken;

    public Param(Token id, Token typeToken) {
        this.id = id;
        this.typeToken = typeToken;
    }
}
