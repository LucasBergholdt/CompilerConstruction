package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.BOOL_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NOT;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NUMBER_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.STRING_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;

public class TypeAnalyzer implements ExprVisitor<Type>, StmtVisitor<Void> {

    private SymbolTable currentEnvironment = new SymbolTable();
    private FuncSymbolTable functionsTable = new FuncSymbolTable();
    private Type currentFuncReturnType = Type.UNKNOWN;

    private List<Stmt> program;

    public TypeAnalyzer(List<Stmt> program) {
        this.program = new ArrayList<>(program);
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

    private Type analyse(Expr expr) {
        return expr.accept(this);
    }

    /* ------------------------------------- Expressions / Statements relateret til Symbol Table. ALL COMPLETED -------------------------  */
    /* Type Error Policy: If new type is not compatible with old type, return the old type. If not possible (e.g. in binary expressions), return Type.UNKNOWN. */

    public Type convertVariableType(TokenType variableType) {
        return switch (variableType) {
            case NUMBER_TYPE -> Type.NUMBER;
            case STRING_TYPE -> Type.STRING;
            case BOOL_TYPE -> Type.BOOL;
            default -> Type.UNKNOWN; // Unreachable. #TODO return null?
            };     
        }

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        Type exprType = analyse(varDecl.expr); 

        Type castToType = convertVariableType(varDecl.typeToken.type);

        if (exprType != castToType && exprType != Type.UNKNOWN) {
            VVPLController.error(varDecl.id.line, ErrorTypeStrings.TYPE_ERROR, String.format("Type %s does not match type of expression (%s)", exprType, castToType));
        }
        else {
            currentEnvironment.define(varDecl.id.lexeme, exprType);
        }
        return null;
        }

    @Override
    public Type visitIdentifierExpr(Identifier identifier) {
        return currentEnvironment.get(identifier.id.lexeme);
    }

    @Override
    public Type visitAssignExpr(Assign assign) {
        Type currType = currentEnvironment.get(assign.ID.lexeme);
        Type exprType = analyse(assign.expr);

        if (exprType == Type.UNKNOWN) {
            // Do not report error if given type is invalid
            return Type.UNKNOWN;
        }
        if (currType == exprType) {
            return currType;   
        }
        else {
            VVPLController.error(assign.ID.line, ErrorTypeStrings.TYPE_ERROR, "current type of identifier [insert name here] does not match the type of the given expression.");
            return currType; // return identifier's current type.   #TODO return null?
        }
    }

  /* ---------------------------- Expressions, nedefra og op af grammaren. ------------------------ */
    @Override 
    public Type visitLiteralExpr(Literal literal) {
        if (literal.token.literal == null) {
            return null;    //#TODO maybe return Types.UNKNOWN here. We dont need to evaluate literals that hold no value. does this even occur? C-E.
        }

        switch (literal.token.literal) {
            case Double d: 
                return Type.NUMBER;
            case Boolean b:
                return Type.BOOL;
            case String s:
                return Type.STRING;
            default:
                // For literals som ikke har en type?
                return Type.UNKNOWN;
        }
    }

    @Override 
    public Type visitUnaryExpr(Unary unary) {
        Type exprType = analyse(unary.expr);

        if (unary.operator.type == NOT && exprType != Type.BOOL) {
            VVPLController.error(unary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator NOT can only precede a boolean expression.");
            return exprType;    // #TODO return null?
        }
        else {
            return exprType;
        }
    }

    @Override 
    public Type visitBinaryExpr(Binary binary) {
        Type left = analyse(binary.left);
        Type right = analyse(binary.right);

        switch (binary.operator.type) {
            case PLUS:
            case MINUS:
            case DIV:
            case MULT:    
                if (left == Type.NUMBER && right == Type.NUMBER) {
                    return Type.NUMBER;
                }
                else {
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] only accepts numbers.");
                    return Type.UNKNOWN;
                }
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
                if (left == Type.NUMBER && right == Type.NUMBER) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] only accepts numbers.");
                    return Type.UNKNOWN;
                }
            case EQUALS:
            case NOT_EQUALS:
                if (left == right) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] can only compare two expressions of the same type.");
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable #debugger siger noget andet
                return Type.UNKNOWN;
        }
    }

    @Override   
    public Type visitLogicalExpr(Logical logical) {
        Type left = analyse(logical.left);
        Type right = analyse(logical.right);

        switch (logical.operator.type) {
            case OR, AND: 
                if (left == Type.BOOL && right == Type.BOOL) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(logical.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator [insert name here] only accepts booleans.");
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override 
    public Type visitCastExpr(Cast cast) {
        Type cast_from = analyse(cast.expr);    // Type of expression before cast
        TokenType cast_to = cast.typeToken.type;    // The type that we want to cast to. 
        switch (cast_from) {
            case NUMBER:
                if (cast_to == STRING_TYPE) {
                    return Type.STRING;
                }
                if (cast_to == BOOL_TYPE) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast NUMBER to string or bool.");
                    return cast_from;
                }
            case STRING:
                if (cast_to == NUMBER_TYPE) {
                     // TypeAnalyzer assumes this is always possible. Runtime error is returned in interpreter in the case that string contains non-digits.
                    return Type.NUMBER;
                }
                else {
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast string to number.");
                    return cast_from;
                }
            case BOOL:
                if (cast_to == STRING_TYPE) {
                    return Type.STRING;
                }
                else {
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast bool to string.");
                    return cast_from;
                }
            default:
                // Reachable if current type is UNKNOWN (an error has happened before this clause). 
                return Type.UNKNOWN;
        }
    }


    /* ------------------------- Statements ---------------------------- */
    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        analyse(exprStmt.expr);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        Type condType = analyse(whileStmt.conditional);
        
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            VVPLController.error(whileStmt.whileToken.line, ErrorTypeStrings.TYPE_ERROR, "conditional has to be of type Bool");
        }
        analyse(whileStmt.body);
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        Type condType = analyse(ifStmt.cond);

        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            VVPLController.error(ifStmt.ifToken.line, ErrorTypeStrings.TYPE_ERROR, "conditional has to be of type Bool");
        }
        analyse(ifStmt.thenBlock);
        if (ifStmt.elseBlock != null) {
            analyse(ifStmt.elseBlock);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        /* DEPRECATED (PrintStmt can print any type). Only here for reference.
        Type exprType = analyse(printStmt.expr);

        if (exprType != Type.STRING && exprType != Type.UNKNOWN) {
            VVPLController.error(printStmt.token.line, ErrorTypeStrings.TYPE_ERROR, "PrintStmt only accepts strings.");
        }
        */
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable(currentEnvironment);

        for (Stmt stmt : blockStmt.stmts) {
            analyse(stmt);
        }
        currentEnvironment = oldTable;
        return null;
    }



    /* --------------------- Function Related --------------------- */

    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        functionsTable.define(functionStmt.name.lexeme, functionStmt);
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        Type exprType = analyse(returnStmt.returnValue); // skal evt sammenlignes med givne type af funktion.
        if (exprType == Type.UNKNOWN) {
            return null;
        }

        // Hvis return type er krævet af funktionen men typen ikke matcher med return expression
        if (currentFuncReturnType != Type.UNKNOWN && exprType != currentFuncReturnType) {
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Type of returned value does not match declared return type of function");
        }
        return null;
    }

    @Override
    public Type visitCallExpr(Call call) {
        
        // If Call Expr is not a function call.
        if (call.paren == null) {
            return analyse(call.callee);
        }

        // Else if Call Expr is a function call
        FunctionStmt functionStmt = functionsTable.get(call.id.lexeme); //NULLPOINTEREXCEPTION. Tjek lige dette i ScopeAnalyzer. Burde ikke være muligt.


        // ------------------ Fetch parameters ------------------
        List<Param> params = functionStmt.params;
        List<Expr> args = call.arguments;

        // Check om typen af arguments matcher typen af erklærede function parameters
        for (int i = 0; i < args.size(); i++) {
            Type argumentType = analyse(args.get(i));
            
            Type paramType = convertVariableType(params.get(i).typeToken.type);
            
            if (argumentType != paramType) {
                VVPLController.error(call.id.line, ErrorTypeStrings.TYPE_ERROR, "Type of given argument [insert name here] does not match the required type [paramType]");
                // return null;? Skal vi stoppe med funktionen her?
            }
        }

        // ---- No errors. Create new environment ----------
        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable();
  
        // If type is required from function, change return type from UNKNOWN.
        if (functionStmt.typeToken != null) {
            currentFuncReturnType = convertVariableType(functionStmt.typeToken.type);
        }

        // define (potentially shadowing) parameters in new environment.
        for (int i = 0; i < params.size(); i++) {
            Param currParam = params.get(i);
            Type paramType = convertVariableType(currParam.typeToken.type);
            
            currentEnvironment.define(currParam.id.lexeme, paramType);
        }

        // analyse body
        analyse(functionStmt.body);
        Type returnType = currentFuncReturnType;

        // ------------- Reset state ------------
        currentEnvironment = oldTable;
        currentFuncReturnType = Type.UNKNOWN;   // Reset FuncReturnType

        return returnType;
    }
}
