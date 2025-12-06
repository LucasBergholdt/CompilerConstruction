package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

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

    private SymbolTable currentEnvironment = new SymbolTable(true);
    private Boolean is_function_env = false;

    private FuncSymbolTable functionsTable = new FuncSymbolTable();

    private List<String> scopeErrors = new LinkedList<>();
    private List<Stmt> program;
    
    public ScopeAnalyzer(List<Stmt> program) {
        this.program = program;
    }

    public List<String> analyse() {
        // Preprocessing: Analyse Function Statements first by going through the top-level program (e.g. not nested blocks) and modify List<Stmt> program by analysing and removing FunctionStmts.
        ListIterator<Stmt> stmts = program.listIterator();

        while (stmts.hasNext()) {
            Stmt currentStmt = stmts.next();
            if(currentStmt instanceof FunctionStmt) {
                analyse(currentStmt);
                stmts.remove(); 
            }
        }

        // Analyse rest of program
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
            return null;    // TODO: Skal test stoppe her? Antag ja for nu.
        }
        
        if (varDecl.expr != null) {
            analyse(varDecl.expr);
        }
        else {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + varDecl.id.line
                    + ": declared variable must be initialized with a value");
        }
        return null;
    }

    public Void visitAssignExpr(Assign assign) {
        if (!currentEnvironment.contains(assign.ID.lexeme)) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + assign.ID.line
                    + ": variable [insert name here] does not exist in scope or any parent scopes.");
            return null;
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

        // analyze each statement. error handle returnStatements.
        int num_statements = blockStmt.stmts.size();

        for (int i = 0; i < num_statements; i++) {
            // If returnstatement occurs in non-function context, return error
            if (blockStmt.stmts.get(i) instanceof ReturnStmt && !is_function_env) {
                scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + ((ReturnStmt)blockStmt.stmts.get(i)).returnKeyword.line
                    + ": Return statement cannot occur in block outside of function");
                return null;    //TODO: Skal vi error handle ting som kommer efter return stmt?
            }
            // If returnstatement is not the last statement of the block, return error
            if (blockStmt.stmts.get(i) instanceof ReturnStmt && i != num_statements - 1 ) {
                scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + ((ReturnStmt)blockStmt.stmts.get(i)).returnKeyword.line
                    + ": Return statement must be last statement of block.");
                return null;
            }
            else {
                analyse(blockStmt.stmts.get(i));
            }
        }

        currentEnvironment = oldTable;
        return null;
    }

    /* ------------------------------------- Functions Related (visitBlockStmt are also related to this section.) ------------------------------------- */
    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        // Check if current scope is global scope
        if (!currentEnvironment.isGlobal) {
                scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + functionStmt.name.line
            + ": Attempting to define function [insert name here] in a non-global scope");
            return null;
        }
        try {
            // Check for uniqueness of function name
            functionsTable.define(functionStmt.name.lexeme, functionStmt);
        } 
        catch (SymbolTableException e) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + functionStmt.name.line
                    + ": Function name [insert name here] already exist in scope.");
        }
        return null;
    }
    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        analyse(returnStmt.returnValue);
        return null;
    }


    @Override
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

        // see if #args == #params
        if (params.size() != args.size()) {
            scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + call.id.line
                    + ": function [ name here] takes exactly [params.size()] parameters.");
            return null;
        }

        // analyse given arguments (check if they are in scope).
        for (int i = 0; i < args.size(); i++) {
            analyse(args.get(i));   
        } // #TODO: fortsætter selv hvis parameters IKKE er i scope. dette modsiger hvad vi plejer at gøre.

        // ------------------ Create new environment and shadow parameters inside new environment. ------------------
        is_function_env = true;
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable();

        // define (potentially shadowing) parameters in new environment.
        for (int i = 0; i < params.size(); i++) {
            Token paramToken = params.get(i).id;
            try { 
                currentEnvironment.define(paramToken.lexeme, paramToken);
            } 
            catch (SymbolTableException e) {
                // Unreachable. Dette nye environment har ikke noget outer environment og parametrene er derfor de første variabler defineret nogensinde i dette env.
                scopeErrors.add(ErrorTypeStrings.SCOPE_ERROR + ", line " + call.id.line
                    + ": function [insert name here] does not exist.");
                return null;    // #TODO return NULL eller vil vi blive ved med at finde fejl?
            }
        }

        // analyse body
        analyse(functionStmt.body);
  
        // ------------- Reset state ------------
        currentEnvironment = oldTable;
        is_function_env = false;

        return null;
    }
}