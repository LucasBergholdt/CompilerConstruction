package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.SymbolTableException;
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

/**
 * ScopeAnalyzer class that implements the analyse() method. 
 * 
 * @author Carl-Emil Dons Christensen
 */
public class ScopeAnalyzer implements ExprVisitor<Void>, StmtVisitor<Void> {

    /**
     * Symbol table used for the global environment. This is the only time this constructor is used.
     */
    private SymbolTable currentEnvironment = new SymbolTable(true);

    /**
     * True when evaluating a function, otherwise false.
     */
    private Boolean is_function_env = false;

    /**
     * Symbol table for functions only.
     */
    private FuncSymbolTable functionsTable = new FuncSymbolTable();
    
    /**
     * List of statements to be analysed.
     */
    private List<Stmt> program;
    
    /**
     * Initializes the scope analyser with a set of statements to analyse
     * @param program the list of statements to be analysed
     */
    public ScopeAnalyzer(List<Stmt> program) {
        this.program = new ArrayList<>(program);
    }

    /**
     * Performs scope analysis for the entire program.
     * 
     * This method analyzes all top-level function declarations first,
     * and then analyzes the remaining statements in program order.
     *
     * Any scope-related errors are reported via {@link VVPLController#error}.
     */
    public void analyse() {
        ListIterator<Stmt> stmts = program.listIterator();
        while (stmts.hasNext()) {
            Stmt currentStmt = stmts.next();
            if(currentStmt instanceof FunctionStmt) {
                analyse(currentStmt);
                stmts.remove(); 
            }
        }
        for (Stmt stmt : program) {
            analyse(stmt);
        }
        return;
    }

    /**
     * Performs scope analysis for a single statement node using the visitor interface.
     *
     * @param stmt the statement to analyze
     */
    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Performs scope analysis for a single expression node using the visitor interface.
     *
     * @param stmt the expression to analyze
     */
    private void analyse(Expr expr) {
        expr.accept(this);
    }

    /* ------------------------------------- Identifier Declarations, Assignments and Referencing ------------------------------------- */
    
    /**
     * Analyses a variable declaration.
     * Error is raised if:
     *  - declared variable already exists in scope 
     *  - variable is not declared with a value
     * @param varDecl the variable declaration statement
     */
    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        try {
            currentEnvironment.define(varDecl.id.lexeme, varDecl.id); 
        } catch (SymbolTableException e) {
            VVPLController.error(varDecl.id.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Variable %s already exists in scope", varDecl.id.lexeme));
            return null;
        }
        
        if (varDecl.expr != null) {
            analyse(varDecl.expr);
        }
        else {
            VVPLController.error(varDecl.id.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Variable %s must be initialized with a value", varDecl.id.lexeme));
        }
        return null;
    }

    /**
     * Analyses an assignment expression.
     * An error is raised if variable does not exist in scope
     * @param assign the assignment expression
     */
    @Override
    public Void visitAssignExpr(Assign assign) {
        if (!currentEnvironment.contains(assign.ID.lexeme)) {
            VVPLController.error(assign.ID.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Variable %s does not exist in scope or any parent scopes.", assign.ID.lexeme));
            return null;
        }
        analyse(assign.expr);
        return null;
    }

    /**
     * Analyses an identifier reference expression.
     * An error is raised if variable does not exist in scope.
     * @param identifier the identifier expression
     */
    @Override
    public Void visitIdentifierExpr(Identifier identifier) {
        if (!currentEnvironment.contains(identifier.id.lexeme)) {
            VVPLController.error(identifier.id.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Variable %s does not exist in scope or any parent scopes.", identifier.id.lexeme));
        }
        return null;
    }

    /* ------------------------------------- Expressions ------------------------------------- */
    /**
     * Analyses a literal expression
     * @param literal the literal expression
     */
    @Override
    public Void visitLiteralExpr(Literal literal) {
        return null;
    }

    /**
     * Analyses a unary expression
     * @param unary the unary expression
     */
    @Override
    public Void visitUnaryExpr(Unary unary) {
        analyse(unary.expr);
        return null;
    }
    /**
     * Analyses a binary expression
     * @param binary the binary expression
     */
    @Override
    public Void visitBinaryExpr(Binary binary) {
        analyse(binary.left);
        analyse(binary.right);
        return null;
    }

    /**
     * Analyses a logical expression
     * @param logical the logical expression
     */
    @Override
    public Void visitLogicalExpr(Logical logical) {
        analyse(logical.left);
        analyse(logical.right);
        return null;
    }

    /**
     * Analyses a cast expression
     * @param cast the cast expression
     */
    @Override
    public Void visitCastExpr(Cast cast) {
        analyse(cast.expr);
        return null;
    }

    /* ------------------------------------- Statements ------------------------------------- */
    /**
     * Analyses an expression statement
     * @param exprStmt the expression statement
     */
    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        analyse(exprStmt.expr);
        return null;
    }
    
    /**
     * Analyses a while statement
     * @param whileStmt the while statement
     */
    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        analyse(whileStmt.conditional);
        analyse(whileStmt.body);
        return null;
    }
    
    /**
     * Analyses an if else statement
     * @param ifStmt the if else statement
     */
    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        analyse(ifStmt.cond);
        analyse(ifStmt.thenBlock);
        if (ifStmt.elseBlock != null) {
            analyse(ifStmt.elseBlock);
        }
        return null;
    }
    
    /**
     * Analyses a print statement
     * @param printStmt the print statement
     */
    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        analyse(printStmt.expr);
        return null;
    }

    /**
     * Analyses a block statement by constructing a new environment for the block,
     * subsequently analysing each statement within this scope.
     * Raises an error if one of the non-final statements is a return statement.
     * @param blockStmt the block statement
     */
    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable(currentEnvironment);

        int num_statements = blockStmt.stmts.size();
        for (int i = 0; i < num_statements; i++) {
            // If any returnstatement is non-final statement of block, return error
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

    /** 
     * Analyses a Function Statement.
     * An error is raised if:
     *  - the function statement occurs in a non-global environment
     *  - the function has already been declared
     *  - the function parameters are named similarly
     * If no error is raised, the global flag global {@link #is_function_env} is set and the body of the function is analysed. 
     * @param functionStmt the function declaration statement
     */
    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        if (!currentEnvironment.isGlobal) {
            VVPLController.error(functionStmt.name.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Function %s can only be defined in a global scope.", functionStmt.name.lexeme));
            return null;
        }

        try {
            functionsTable.define(functionStmt.name.lexeme, functionStmt);
        } 
        catch (SymbolTableException e) {
            VVPLController.error(functionStmt.name.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Function %s already exist in scope.", functionStmt.name.lexeme));
        }

        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable();

        List<Param> params = functionStmt.params;
        for (int i = 0; i < params.size(); i++) {
            Token paramToken = params.get(i).id;
            try { 
                currentEnvironment.define(paramToken.lexeme, paramToken);
            } 
            catch (SymbolTableException e) {
                VVPLController.error(paramToken.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Parameter %s has already been defined.", paramToken.lexeme));
                return null;
            }
        }

        is_function_env = true;
        analyse(functionStmt.body);
        is_function_env = false;

        currentEnvironment = oldTable;
        return null;
    }

    /**
     * Analyses a return statement by using {@link #is_function_env} set by {@link #visitFunctionStmt(FunctionStmt)}.
     * Error is raised if a return statement occurs outside of a function body.
     * @param returnStmt the return statement
     */
    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        if (!is_function_env) {
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.SCOPE_ERROR, "Return statement cannot occur outside of function.");
        } 
        else if (returnStmt.returnValue != null) {
            analyse(returnStmt.returnValue);
        }
        return null;
    }

    /** 
     * Analyses a Call Statement.
     * An error is raised if:
     *  - the function name is not defined
     *  - the number of given arguments does not match the number of necessary parameters.
     * If no error is raised, the arguments are analysed. 
     * @param call the call expression
     */
    @Override
    public Void visitCallExpr(Call call) {
        FunctionStmt functionStmt;
        try {
            functionStmt = functionsTable.get(call.id.lexeme);
        } 
        catch (SymbolTableException e) {
            VVPLController.error(call.id.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Function %s does not exist.", call.id.lexeme));
            return null;
        }
        
        List<Param> params = functionStmt.params;   
        List<Expr> args = call.arguments;

        if (params.size() != args.size()) {
            VVPLController.error(call.id.line, ErrorTypeStrings.SCOPE_ERROR, String.format("Function %s takes exactly %d parameters. %d were given", call.id.lexeme, params.size(), args.size()));
            return null;
        }

        for (int i = 0; i < args.size(); i++) {
            analyse(args.get(i));   
        }

        return null;
    }
}