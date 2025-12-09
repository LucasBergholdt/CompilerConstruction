package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;

public class ScopeAnalyzer implements ExprVisitor<Void>, StmtVisitor<Void> {

    private SymbolTable currentEnvironment = new SymbolTable(true);
    private Boolean is_function_env = false;

    private FuncSymbolTable functionsTable = new FuncSymbolTable();
    private List<Stmt> program;
    
    public ScopeAnalyzer(List<Stmt> program) {
        this.program = new ArrayList<>(program);    // New object is created so that we can remove statements from this program as we go.
    }

    public void analyse() {
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
        return;
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
            VVPLController.error(varDecl.id.line, ErrorTypeStrings.SCOPE_ERROR, "variable [insert name here] already exist in scope.");
            return null;    // TODO: Skal test stoppe her? Antag ja for nu.
        }
        
        if (varDecl.expr != null) {
            analyse(varDecl.expr);
        }
        else {
            VVPLController.error(varDecl.id.line, ErrorTypeStrings.SCOPE_ERROR, "declared variable must be initialized with a value.");
        }
        return null;
    }

    public Void visitAssignExpr(Assign assign) {
        if (!currentEnvironment.contains(assign.ID.lexeme)) {
            VVPLController.error(assign.ID.line, ErrorTypeStrings.SCOPE_ERROR, "variable [insert name here] does not exist in scope or any parent scopes.");
            return null;
        }

        /* C-E: Måske skal vi opdatere ID'et til at pege på en ny token i currentEnvironment. Udeladt for nu da Niels ikke gjorde det. En ekstra note er at vi ikke engang bruger Token værdien som det er nu: vi kunne faktisk blot lave SymbolTable til at være en SymbolList... Men ved ikke om det spiller sammen med vores implementering af functions etc senere.*/
        analyse(assign.expr);
        return null;
    }

    public Void visitIdentifierExpr(Identifier identifier) {
        if (!currentEnvironment.contains(identifier.id.lexeme)) {
            VVPLController.error(identifier.id.line, ErrorTypeStrings.SCOPE_ERROR, "variable [insert name here] does not exist in scope or any parent scopes.");
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
        if (ifStmt.elseBlock != null) {
            analyse(ifStmt.elseBlock);
        }
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
            // If returnstatement is not the last statement of the block, return error
            if (blockStmt.stmts.get(i) instanceof ReturnStmt && i != num_statements - 1 ) {
                VVPLController.error(((ReturnStmt)blockStmt.stmts.get(i)).returnKeyword.line, ErrorTypeStrings.SCOPE_ERROR, "Return statement must be last statement of block.");
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
            VVPLController.error(functionStmt.name.line, ErrorTypeStrings.SCOPE_ERROR, "Attempting to define function [insert name here] in a non-global scope.");
            return null;
        }
        try {
            // Check for uniqueness of function name, otherwise define function in table.
            functionsTable.define(functionStmt.name.lexeme, functionStmt);
        } 
        catch (SymbolTableException e) {
            VVPLController.error(functionStmt.name.line, ErrorTypeStrings.SCOPE_ERROR, "Function name [insert name here] already exist in scope.");
        }

        // Analyse function body.
        // ------------------ Create new environment and shadow parameters inside new environment. ------------------
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable();

        List<Param> params = functionStmt.params;
        // define (potentially shadowing) parameters in new environment.
        for (int i = 0; i < params.size(); i++) {
            Token paramToken = params.get(i).id;
            try { 
                currentEnvironment.define(paramToken.lexeme, paramToken);
            } 
            catch (SymbolTableException e) {
                VVPLController.error(paramToken.line, ErrorTypeStrings.SCOPE_ERROR, "Parameter has already been defined.");
                // TODO: evt returnér null her og exit.
            }
        }
        is_function_env = true;
        // analyse body
        analyse(functionStmt.body);
        is_function_env = false;

        // ------------- Reset state ------------
        currentEnvironment = oldTable;

        return null;
    }
    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        // If returnstatement occurs in non-function context, return error
        if (!is_function_env) {
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.SCOPE_ERROR, "Return statement cannot occur outside of function.");
        } else if (returnStmt.returnValue != null) {
            analyse(returnStmt.returnValue);
        }
        return null;
    }

    @Override
    public Void visitCallExpr(Call call) {
        // If Call Expr is not a function call.
        if (call.paren == null) {
            analyse(call.callee);
            return null;
        }

        // Else if Call Expr is a function call
        FunctionStmt functionStmt;

        // ------------------ Fetch function ------------------
        try {
            functionStmt = functionsTable.get(call.id.lexeme);
        } 
        catch (SymbolTableException e) {
            VVPLController.error(call.id.line, ErrorTypeStrings.SCOPE_ERROR, "function [insert name here] does not exist.");
            return null;
        }

        // ------------------ Fetch parameters ------------------
        List<Param> params = functionStmt.params;   
        List<Expr> args = call.arguments;

        // see if #args == #params
        if (params.size() != args.size()) {
            VVPLController.error(call.id.line, ErrorTypeStrings.SCOPE_ERROR, "function [name here] takes exactly [params.size()] parameters.");
            return null;
        }

        // analyse given arguments (check if they are in scope).
        for (int i = 0; i < args.size(); i++) {
            analyse(args.get(i));   
        }


        return null;
    }
}