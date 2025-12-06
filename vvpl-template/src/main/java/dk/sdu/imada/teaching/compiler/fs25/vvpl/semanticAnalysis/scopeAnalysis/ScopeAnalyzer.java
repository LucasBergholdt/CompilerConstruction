package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import java.util.ArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;

public class ScopeAnalyzer implements ExprVisitor<Void>, StmtVisitor<Void> {

    private SymbolTable currentEnvironment = new SymbolTable();
    private Boolean is_env_function = false;

    private FuncSymbolTable functionsTable = new FuncSymbolTable(); // #TODO lav denne klasse.

    private List<String> scopeErrors = new LinkedList<>();
    private List<Stmt> program;
    

    public ScopeAnalyzer(List<Stmt> program) {
        this.program = program;
    }

    public List<String> analyse() {
        for (Stmt stmt : program) {
            analyse(stmt);
        }

        return this.scopeErrors;
    }

    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    private void analyse(Expr expr) {
        expr.accept(this);
    }

    /* ------------------------------------- Declarations / References ------------------------------------- */
    public Void visitVarDecl(VarDecl varDecl) {
        try {
            currentEnvironment.define(varDecl.id.lexeme, varDecl.id); 
        } catch (SymbolTableException e) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + varDecl.id.line
                    + ": variable [insert name here] already exist in scope.");
        }
        if (varDecl.expr != null) {
            analyse(varDecl.expr);
        }

        return null;
    }

    public Void visitAssignExpr(Assign assign) {
        if (!currentEnvironment.contains(assign.ID.lexeme)) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + assign.ID.line
                    + ": variable [insert name here] does not exist in scope or any parent scopes.");
        }

        /* C-E: Måske skal vi opdatere ID'et til at pege på en ny token i currentEnvironment. Udeladt for nu da Niels ikke gjorde det. En ekstra note er at vi ikke engang bruger Token værdien som det er nu: vi kunne faktisk blot lave SymbolTable til at være en SymbolList... Men ved ikke om det spiller sammen med vores implementering af functions etc senere.*/
        analyse(assign.expr);

        return null;
    }

    public Void visitIdentifierExpr(Identifier identifier) {
        if (!currentEnvironment.contains(identifier.id.lexeme)) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + identifier.id.line
                    + ": variable [insert name here] does not exist in scope or any parent scopes.");
        }
        return null;
    }

    /* ------------------------------------- Expressions bottom-up ------------------------------------- */
    public Void visitLiteralExpr(Literal literals) {
        return null;
    }

    public Void visitUnaryExpr(Unary unary) {
        analyse(unary.expr);
        return null;
    }

    public Void visitBinaryExpr(Binary binary) {
        analyse(binary.left);
        analyse(binary.right);
        return null;
    }

    public Void visitLogicalExpr(Logical logical) {
        analyse(logical.left);
        analyse(logical.right);
        return null;
    }

    @Override // TOPPRIO nu
    public Void visitCallExpr(Call call) {

        // ------------------ Fetch function ------------------
        FunctionStmt functionStmt;
        try {
            functionStmt = functionsTable.get(call.id.lexeme);
        } catch (SymbolTableException e) {
             scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + call.id.line
                    + ": function [insert name here] does not exist.");
            return null;
        }

        // ------------------ Fetch parameters ------------------
        List<Param> params = functionStmt.params;   
        List<Expr> args = call.arguments;

        if (params.size() != args.size()) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + call.id.line
                    + ": function [ name here] takes exactly [params.size()] parameters.");
            return null;
        }

        // ------------------ Create new environment and shadow parameters inside new environment. Check if parameters are in scope. ------------------
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable(true);

        for (int i = 0; i < params.size(); i++) { // shadower params i nye environment.
            Token paramToken = params.get(i).id;
            try { 
                currentEnvironment.define(paramToken.lexeme, paramToken);
            } 
            catch (SymbolTableException e) {
                // Unreachable. Dette nye environment har ikke noget outer environment og parametrene er derfor de første variabler defineret nogensinde i dette env.
                return null;
            }
        }

        // Analyse statements within body. These statements CANNOT see values defined in outer Environment.
        analyse(functionStmt.body);
  
        // ------------- Reset state ------------
        currentEnvironment = oldTable;
        return null;
    }

    @Override
    public Void visitCastExpr(Cast cast) {
        // TODO Auto-generated method stub
        analyse(cast.expr);
        return null;
    }

    /* ------------------------------------- Statements ------------------------------------- */
    public Void visitExprStmt(ExprStmt exprStmt) {
        analyse(exprStmt.expr);
        return null;
    }
    
    public Void visitWhileStmt(WhileStmt whileStmt) {
        analyse(whileStmt.conditional);
        analyse(whileStmt.body);
        return null;
    }
    
    public Void visitIfStmt(IfStmt ifStmt) {
        analyse(ifStmt.cond);
        analyse(ifStmt.thenBlock);
        analyse(ifStmt.elseBlock);
        return null;
    }
    
    public Void visitPrintStmt(PrintStmt printStmt) {
        analyse(printStmt.expr);
        return null;
    }

    public Void visitBlockStmt(BlockStmt blockStmt) {
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable(currentEnvironment);


        for (Stmt stmt : blockStmt.stmts) {
            analyse(stmt);
        }
        currentEnvironment = oldTable;
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        /*
        Fremgangsmåde:
        - Tjek om Function er i Global Scope .
        - Definér funktion i global scope environment (Checker for uniqueness af name)
        - Definér nyt environment for funktion og initialisér parametre
            - Kør block stmts inden for det nye environment
        - Rollback til gamle environment.
        */

        // Functions can only be defined in global scope.
        if (currentEnvironment.outer != null) {
                        scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + functionStmt.name.line
                    + ": Attempting to define function [insert name here] in a non-global scope");
            return null;
        }

        // ---------- Define function in current environment. (checks for unique name) ---------
        try {
            currentEnvironment.define(functionStmt.name.lexeme, functionStmt.name);
        } catch (SymbolTableException e) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + functionStmt.name.line
                    + ": Function name [insert name here] already exist in scope.");
                return null;
        }
        return null;

    }
}