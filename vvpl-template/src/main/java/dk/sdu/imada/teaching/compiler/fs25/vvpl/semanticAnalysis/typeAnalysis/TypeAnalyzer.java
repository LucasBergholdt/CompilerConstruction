package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.BOOL_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.GREATER_EQUAL;
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

        // Pass 1: add all function declarations to functionsTable to support out of order calling
        for (Stmt stmt : program) {
            if (stmt instanceof FunctionStmt) {
                FunctionStmt functionStmt = (FunctionStmt) stmt;
                functionsTable.define(functionStmt.name.lexeme, functionStmt);
            }
        }

        // Pass 2: Analyse rest of program
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
            default -> Type.UNKNOWN; // Unreachable. 
            };
        }

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        Type exprType = analyse(varDecl.expr); 
        if (exprType == Type.UNKNOWN) {
            return null;
        }

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
            return Type.UNKNOWN; // return identifier's current type.   #TODO return null?
        }
    }

  /* ---------------------------- Expressions, nedefra og op af grammaren. ------------------------ */
    @Override 
    public Type visitLiteralExpr(Literal literal) {
        switch (literal.token.type) {
            case NUMBER: 
                return Type.NUMBER;
            case TRUE:
            case FALSE:
                return Type.BOOL;
            case STRING:
                return Type.STRING;
            default:
                return Type.UNKNOWN;
        }
    }

    @Override 
    public Type visitUnaryExpr(Unary unary) {
        Type exprType = analyse(unary.expr);
        if (exprType == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }

        if (unary.operator.type == NOT && exprType != Type.BOOL) {
            VVPLController.error(unary.operator.line, ErrorTypeStrings.TYPE_ERROR, "operator NOT can only precede a boolean expression.");
            return Type.UNKNOWN;
        }
        else {
            return exprType;
        }
    }

    @Override 
    public Type visitBinaryExpr(Binary binary) {
        Type left = analyse(binary.left);
        Type right = analyse(binary.right);
        /*
        if (left == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }
        Type right = analyse(binary.right);
        if (right == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }
        */
        
        
        if (left == Type.UNKNOWN || right == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }
        
        switch (binary.operator.type) {
            case PLUS:
            case DIV:
            case MULT:
            case SUB:    
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
                // Unreachable #debugger siger noget andet. #TODO return null?
                return null;
        }
    }

    @Override   
    public Type visitLogicalExpr(Logical logical) {
        Type left = analyse(logical.left);
        Type right = analyse(logical.right);

        if (left == Type.UNKNOWN || right == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }

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
            case Type.NUMBER:
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
            case Type.STRING:
                if (cast_to == NUMBER_TYPE) {
                     // TypeAnalyzer assumes this is always possible. Runtime error is returned in interpreter in the case that string contains non-digits.
                    return Type.NUMBER;
                }
                else {
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast string to number.");
                    return cast_from;
                }
            case Type.BOOL:
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

    public boolean alwaysReturns(Stmt stmt) {
        // CASE 1: Hvis vi ser en return statement ved vi at denne branch/path returnerer.
        // Base case
        if (stmt instanceof ReturnStmt) {
            return true;
        }

        // CASE 2: Block statement.
        // HVILKEN SOM HELST af dens statements skal returnere. Vi skal bare returnere på et eller andet tidspunkt i en block.
        // En if-else statement har flere blocks. Hver af disse blocks skal have bare ÈN statement der returnerer.
        if (stmt instanceof BlockStmt) {
            BlockStmt block = (BlockStmt) stmt;
            // Return if ANY of the blocks statements return
            for (Stmt s : block.stmts) {
                if (alwaysReturns(s)) {
                    return true;
                }
            }
            return false;
        }

        // CASE 3: If Statement
        // For at en if statmenet altid returner kræver det at:
        // 1. Else block eksisterer (ellers kan vi skippe if branch hvis condition ikke er satisfied og så retunerer vi måske ikke)
        // 2. Både then-branch OG else-branch skal returnere.
        if (stmt instanceof IfStmt) {
            IfStmt ifStmt = (IfStmt) stmt;

            // If tjekker 1.
            if (ifStmt.elseBlock != null) { 
                // Denne && tjekker 2.
                return alwaysReturns(ifStmt.thenBlock) && alwaysReturns(ifStmt.elseBlock);
            } else {
                // Ingen else block, så vi der er sti (når if condition ikke er satisfied)
                // hvor vi skpper hele if statement og ikke returner fra ifStmt
                return false;
            }
        }

        // CASE 4: alle andre statements
        // Alle andre statements kan vi ikke garantere returnerer skulle jeg mene.
        // Kun whileStmt er iffy men det er lidt samme logik som ifStmt uden else-branch.
        // Vi kan ikke garantere at while condition er satisfied -> derfor kan while skippes helt og ikke returnere
        // Derfor blot false for alle andre typer statements
        return false;
    }

    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {

        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable();

        // define (potentially shadowing) parameters in new environment.
        List<Param> params = functionStmt.params;
        for (int i = 0; i < params.size(); i++) {
            Param currParam = params.get(i);
            Type paramType = convertVariableType(currParam.typeToken.type);
            
            currentEnvironment.define(currParam.id.lexeme, paramType);
        }

        // if function type is declared, tell returnStmts which type this is.
        if (functionStmt.typeToken != null) {
            currentFuncReturnType = convertVariableType(functionStmt.typeToken.type);
        }
        else {
            currentFuncReturnType = Type.UNKNOWN;
        }

        // analyse. 
        analyse(functionStmt.body);

        //: Check that functions with return types actually return
        if (functionStmt.typeToken != null && !alwaysReturns(functionStmt.body)) {
            VVPLController.error(functionStmt.name.line, ErrorTypeStrings.TYPE_ERROR, "Function must return a value in all code paths");
        }

        // ---- reset ----
        currentEnvironment = oldTable;
        currentFuncReturnType = Type.UNKNOWN;
        return null;
    }

    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {

        // Handling void returns (return;)
        if (returnStmt.returnValue == null) {
            // if current function has defined type then it should not be able to just return with no value
            if (currentFuncReturnType != Type.UNKNOWN) {
                VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Function with return type must return a value.");
            } else {
                // Void function can return with no value so this is fine.
                return null;
            }
        }
        // From now on and forward we know that the returnValue != null (e.g. return <expr>)

        Type exprType = analyse(returnStmt.returnValue);
        if (exprType == Type.UNKNOWN) {
            return null;
        }

/*         // Case: Vi læser et FunctionStmt (currFuncReturnType != unknown). Return Stmt matcher ikke krævede type.
        if (currentFuncReturnType != Type.UNKNOWN && exprType != currentFuncReturnType) {
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Type of returned value does not match declared return type of function");
            return null;
        }
        else if (currentFuncReturnType != Type.UNKNOWN && exprType == currentFuncReturnType) {
            // Case: Vi læser et FunctionStmt (currFuncType != unknown), og typerne matcher.
            return null;
        }
        else {  //currentFuncReturnType == Type.UNKNOWN. Ergo er return type NULL, og der må ikke forekomme et returnStmt med en type. Error!
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Cannot return a value from a void function");
            return null;
        } */

        //? Ny version. Mere simpel og burde være samme logik som overstående udkommenteret if/elif/else.
        //currentFuncReturnType == Type.UNKNOWN. Ergo er return type NULL, og der må ikke forekomme et returnStmt med en type. Error!
        if (currentFuncReturnType == Type.UNKNOWN) { 
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Cannot return a value from a void function");
            return null;
        } else if (exprType != currentFuncReturnType) { // Case: Vi læser et FunctionStmt (currFuncReturnType != unknown). Return Stmt matcher ikke krævede type.
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Type of returned value does not match declared return type of function");
            return null;
        } else {
            // Case: Vi læser et FunctionStmt (currFuncType != unknown), og typerne matcher.
            return null;
        }

    }

    @Override
    public Type visitCallExpr(Call call) {
        
        /*
        // If Call Expr is not a function call.
        if (call.paren == null) {
            return analyse(call.callee);
        }
        */

        // Else if Call Expr is a function call
        FunctionStmt functionStmt = functionsTable.get(call.id.lexeme);

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

        // Function has return type declared. No need to evaluate function again.
        if (functionStmt.typeToken != null) {
            return convertVariableType(functionStmt.typeToken.type);
        } 
        else {
            // CASE: Funktion har ikke angivet en type. Derfor må return type være null. #TODO Antagelse om opgaven.
            return Type.UNKNOWN;
        }

        // Else, we need to return a type of a dynamically chosen return type. This is the interpreter's job.

        /* ALT I KOMMENTAREN: Returnerer return typen på en arbitrær ReturnStmt i koden

        currentFuncReturnType = Type.UNKNOWN; // function has no return type declared


        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable();
  
        // define (potentially shadowing) parameters in new environment.
        for (int i = 0; i < params.size(); i++) {
            Param currParam = params.get(i);
            Type paramType = convertVariableType(currParam.typeToken.type);
            
            currentEnvironment.define(currParam.id.lexeme, paramType);
        }
        
        analyse(functionStmt.body);
        Type resultType = currentFuncReturnType;

        // reset
        currentFuncReturnType = Type.UNKNOWN;
        currentEnvironment = oldTable;

        return resultType;
    */
    }
}
