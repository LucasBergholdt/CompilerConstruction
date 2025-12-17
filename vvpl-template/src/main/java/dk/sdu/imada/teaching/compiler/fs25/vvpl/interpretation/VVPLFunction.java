package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;

/**
 * @author Lasse Arpe Kristensen
 */
class VVPLFunction implements VVPLCallable {
    private final Stmt.FunctionStmt funcDecl;
    
    VVPLFunction(Stmt.FunctionStmt funcDecl) {
        this.funcDecl = funcDecl;
    }

/**
 * Creates a new environment for a function call; together with the arguments.
 * 
 * Follow the book implementatation.
 */
public Object call(Interpreter interpreter, List<Object> arguments) {

    // Globals must be accessible. 
    // CALL BY VALUE - "copy" - the actual global must not change. 
    Environment functionScope = new Environment(interpreter.globals);

    // Define the parameters inside the new scope. 
    for (int i = 0; i < funcDecl.params.size(); i++) {
        functionScope.define(funcDecl.params.get(i).id.lexeme, arguments.get(i));
    }

    try {
        interpreter.executeBlock(funcDecl.body.stmts, functionScope);
    } catch (ReturnExcep returnValue) {
        return returnValue.value;
    }

    return null;
}
}