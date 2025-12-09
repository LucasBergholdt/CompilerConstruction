package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;

// Skriv i rapport: "Handling function calls is an important design decision when implementing an interpreter"

// OPtion 1: Outsourcing execution logic
/* Seperate function class 
   
*/ 

// Option 2: Executing function calls directly using the functions AST node
/* Executed in place, no wrapping in runtime object 
   Function call interprets AST node directly (new env, bind arguments, execute body) - no separet function class
*/

/* Write pros and cons i report.  
    Argument for choosing option 1: clean, extensible, less complex visitCallExpr, reuse callable interface to make native functions, søg på flere 
*/


class VVPLFunction implements VVPLCallable {
    private final Stmt.FunctionStmt funcDecl;
    
    VVPLFunction(Stmt.FunctionStmt funcDecl) {
        this.funcDecl = funcDecl;
    }


public Object call(Interpreter interpreter, List<Object> arguments) {

    // De globale variable skal kunne tilgås i dette nye environment.
    // CALL BY VALUE - the actual value of the original global must not change. 
    
    Environment functionScope = new Environment(interpreter.globals);

    for (int i = 0; i < funcDecl.params.size(); i++) {
        functionScope.define(funcDecl.params.get(i).id.lexeme, arguments.get(i)); // Define function parameter in this scope. 
    }

    interpreter.executeBlock(funcDecl.body.stmts, functionScope);

    return null;
}
}