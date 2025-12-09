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
}

public Object call(Interpreter interpreter, List<Object> arguments) {
    // Skal environment virkelig gøres "package"? 
    // De om du kan finde yderligere info om det. 
    Environment environment = new Environment(interpreter.env);

    for (int i = 0; i < funcDecl.params.size(); i++) {
        environment.define(funcDecl.params.get(i).id.lexeme, arguments.get(i));
    }

    interpreter.executeBlock(funcDecl.body.stmts, environment);

    return null;
}
