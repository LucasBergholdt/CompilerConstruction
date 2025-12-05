package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;

public class ScopeAnalyzer implements ExprVisitor<Void>, StmtVisitor<Void> {

    private SymbolTable currentEnvironment = new SymbolTable();
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

    @Override
    public Void visitCallExpr(Call call) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    @Override
    public Void visitCastExpr(Cast cast) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCastExpr'");
    }

    /* ------------------------------------- Statements ------------------------------------- */
    public Void visitExprStmt(ExprStmt exprStmt) {
        analyse(exprStmt.expr);
        return null;
    }
    
    public Void visitWhileStmt(WhileStmt whileStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
    
    public Void visitIfStmt(IfStmt ifStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
    
    public Void visitPrintStmt(PrintStmt printStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitFunctionStmt'");
    }
}