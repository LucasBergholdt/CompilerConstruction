package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.*;

import java.util.ArrayList;
import java.util.List;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;

/**
 * TypeAnalyzer class that implements the {@link #analyse()} method. 
 * 
 * @author Carl-Emil Dons Christensen
 */
public class TypeAnalyzer implements ExprVisitor<Type>, StmtVisitor<Void> {

    /**
     * Symbol table for variables
     */
    private SymbolTable currentEnvironment = new SymbolTable();

    /**
     * Symbol table for functions
     */
    private FuncSymbolTable functionsTable = new FuncSymbolTable();

    /**
     * Set by {@link #visitFunctionStmt(FunctionStmt)} upon declared type, used by {@link #visitReturnStmt(ReturnStmt)}.
     */
    private Type currentFuncReturnType = Type.UNKNOWN;

    /**
     * List of statements to be analysed.
     */
    private List<Stmt> program;

    /**
     * Initializes the type analyser with a set of statements to analyse
     * @param program the list of statements to be analysed
     */
    public TypeAnalyzer(List<Stmt> program) {
        this.program = new ArrayList<>(program);
    }

    /**
     * Performs type analysis for the entire program.
     * 
     * This method analyzes all top-level function declarations first,
     * and then analyzes the remaining statements in program order.
     *
     * Any type-related errors are reported via {@link VVPLController#error}.
     * @author Carl-Emil Dons Christensen
     */
    public void analyse() {
        for (Stmt stmt : program) {
            if (stmt instanceof FunctionStmt) {
                FunctionStmt functionStmt = (FunctionStmt) stmt;
                functionsTable.define(functionStmt.name.lexeme, functionStmt);
            }
        }
        for (Stmt stmt : program) {
            analyse(stmt);
        }
        return;
    }

    /**
     * Performs type analysis for a single statement node using the visitor interface.
     *
     * @param stmt the statement to analyze
     */
    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Performs type analysis for a single expression node using the visitor interface.
     *
     * @param stmt the expression to analyze
     * @return type of the expression
     */
    private Type analyse(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Converts (i.e. casts) a {@link TokenType} to a {@link Type}.
     * @param variableType the TokenType to be casted. The following types are allowed as parameters:
     *  - {@link TokenType#NUMBER_TYPE}
     *  - {@link TokenType#STRING_TYPE}
     *  - {@link TokenType#BOOL_TYPE}
     * @return resulting type
     * @author: Carl-Emil Dons Christensen */
    public Type convertVariableType(TokenType variableType) {
        return switch (variableType) {
            case NUMBER_TYPE -> Type.NUMBER;
            case STRING_TYPE -> Type.STRING;
            case BOOL_TYPE -> Type.BOOL;
            default -> Type.UNKNOWN; // Unreachable. 
            };
        }

    /* ------------------------------------- Identifier Declarations, Assignments and Referencing ------------------------------------- */
    /**
     * Analyses a variable declaration.
     * Error is raised if the declared type does not match the type of the given expression
     * @param varDecl the variable declaration statement
     * @author Carl-Emil Dons Christensen
     */
    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        Type exprType = analyse(varDecl.expr); 
        if (exprType == Type.UNKNOWN) {
            return null;
        }
        Type declaredType = convertVariableType(varDecl.typeToken.type);

        if (exprType != declaredType) {
            VVPLController.error(varDecl.id.line, ErrorTypeStrings.TYPE_ERROR, String.format("Type %s does not match type of expression (%s)", exprType, declaredType));
        }
        else {
            currentEnvironment.define(varDecl.id.lexeme, exprType);
        }
        return null;
        }

    /**
     * Analyses an assignment expression.
     * An error is raised if the type of the new expression does not match the type of the identifier
     * @param assign the assignment expression
     * @return the type of the expression; {@link Type#UNKNOWN} if an error has occurred
     * @author Carl-Emil Dons Christensen
     */
    @Override
    public Type visitAssignExpr(Assign assign) {
        Type currType = currentEnvironment.get(assign.ID.lexeme);
        Type exprType = analyse(assign.expr);

        if (exprType == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }
        if (currType == exprType) {
            return currType;   
        }
        else {
            VVPLController.error(assign.ID.line, ErrorTypeStrings.TYPE_ERROR, String.format("current type of %s does not match the type of the given expression.", assign.ID.lexeme));
            return Type.UNKNOWN;
        }
    }

    /**
     * Analyses an identifier reference expression.
     * @param identifier the identifier expression
     * @return the type of the expression; {@link Type#UNKNOWN} if an error has occurred
     * @author Carl-Emil Dons Christensen
     */
    @Override
    public Type visitIdentifierExpr(Identifier identifier) {
        return currentEnvironment.get(identifier.id.lexeme);
    }


    /* ------------------------------------- Expressions ------------------------------------- */
    /**
     * Analyses a literal expression by casting the {@link TokenType} to a {@link Type}
     * @param literal the literal expression
     * @return the type of the literal;
     * @author Carl-Emil Dons Christensen
     */
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
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Analyses a unary expression
     * Raises an error if the expression is preceded by NOT whilst not being of type {@link Type#BOOL}
     * @param unary the unary expression
     * @return the type of the expression; {@link Type#UNKNOWN} if an error has occured
     * @author Carl-Emil Dons Christensen
     */
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

    /**
     * Analyses a binary expression.
     * Raises an error if:
     *  - arithmetic operations and numerical comparators are used between other than {@link Type#NUMBER}
     *  - logical comparators are used between two elements not of same type
     * @param binary the binary expression
     * @return the type of the expression; {@link Type#UNKNOWN} if an error has occured
     * @author Carl-Emil Dons Christensen
     */
    @Override 
    public Type visitBinaryExpr(Binary binary) {
        Type left = analyse(binary.left);
        Type right = analyse(binary.right);

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
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, String.format("operator %s only accepts numbers.", binary.operator.lexeme));
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
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, String.format("operator %s only accepts numbers.", binary.operator.lexeme));
                    return Type.UNKNOWN;
                }
            case EQUALS:
            case NOT_EQUALS:
                if (left == right) {
                    return Type.BOOL;
                }
                else {
                    VVPLController.error(binary.operator.line, ErrorTypeStrings.TYPE_ERROR, String.format("operator %s can only compare two expressions of the same type.", binary.operator.lexeme));
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Analyses a logical expression
     * Raises an error if logical operator is used between other than {@link Type#BOOL}s
     * @param logical the logical expression
     * @return the type of the expression; {@link Type#UNKNOWN} if an error has occured
     * @author Carl-Emil Dons Christensen
     */
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
                    VVPLController.error(logical.operator.line, ErrorTypeStrings.TYPE_ERROR, String.format("operator %s only accepts Bools.", logical.operator.lexeme));
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Analyses a cast expression
     * Raises an error if the cast is not possible
     * @param cast the cast expression
     * @return the type of the resulting expression after being casted; {@link Type#UNKNOWN} if an error has occured
     * @author Carl-Emil Dons Christensen
     */
    @Override 
    public Type visitCastExpr(Cast cast) {
        Type cast_from = analyse(cast.expr);
        TokenType cast_to = cast.typeToken.type;

        
        if (cast_from == Type.UNKNOWN) {
            return Type.UNKNOWN;
        }

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
                    VVPLController.error(cast.typeToken.line, ErrorTypeStrings.TYPE_ERROR, "can only cast STRING to NUMBER.");
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
                // Unreachable 
                throw new UnsupportedOperationException();
        }
    }

    /* ------------------------- Statements ---------------------------- */
    /**
     * Analyses an expression statement
     * @param exprStmt the expression statement
     * @author Carl-Emil Dons Christensen
     */
    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        analyse(exprStmt.expr);
        return null;
    }

    /**
     * Analyses a while statement
     * Raises an error if the conditional is not of type {@link Type#BOOL}
     * @param whileStmt the while statement
     * @author Carl-Emil Dons Christensen
     */
    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        Type condType = analyse(whileStmt.conditional);
        
        if (condType != Type.BOOL && condType != Type.UNKNOWN) {
            VVPLController.error(whileStmt.whileToken.line, ErrorTypeStrings.TYPE_ERROR, "conditional has to be of type Bool");
        }
        analyse(whileStmt.body);
        return null;
    }

    /**
     * Analyses an if else statement
     * Raises an error if the conditional is not of type {@link Type#BOOL}
     * @param ifStmt the if else statement
     * @author Carl-Emil Dons Christensen
     */
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

    /**
     * Analyses a print statement
     * @param printStmt the print statement
     * @author Carl-Emil Dons Christensen
     */
    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        analyse(printStmt.expr);
        return null;
    }

    /**
     * Analyses a block statement by constructing a new environment for the block,
     * subsequently analysing each statement within this scope.
     * @param blockStmt the block statement
     * @author Carl-Emil Dons Christensen
     */
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


    /* ------------------------------------- Functions Related (visitBlockStmt are also related to this section.) ------------------------------------- */

    /** @author: Lucas Bergholdt Hansen */
    public boolean alwaysReturns(Stmt stmt) {
        // CASE 1: Hvis vi ser en return statement ved vi at denne branch/path returnerer.
        // Base case
        if (stmt instanceof ReturnStmt) {
            return true;
        }
        // CASE 2: If Statement
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

        
        // CASE 3: Block statement.
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

        // CASE 4: alle andre statements
        // Alle andre statements kan vi ikke garantere returnerer skulle jeg mene.
        // Kun whileStmt er iffy men det er lidt samme logik som ifStmt uden else-branch.
        // Vi kan ikke garantere at while condition er satisfied -> derfor kan while skippes helt og ikke returnere
        // Derfor blot false for alle andre typer statements
        return false;
    }

    /** 
     * Analyses a Function Statement by setting the variable {@link #currentFuncReturnType} for {@link #visitReturnStmt(ReturnStmt)} to use, subsequently analysing the body of the function.
     * An error is raised if the function body is not guaranteed to return the declared type.
     * @param functionStmt the function declaration statement
     * @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen
     */
    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {

        SymbolTable oldTable = currentEnvironment;
        currentEnvironment = new SymbolTable();

        List<Param> params = functionStmt.params;
        for (int i = 0; i < params.size(); i++) {
            Param currParam = params.get(i);
            Type paramType = convertVariableType(currParam.typeToken.type);
            
            currentEnvironment.define(currParam.id.lexeme, paramType);
        }

        if (functionStmt.typeToken != null) {
            currentFuncReturnType = convertVariableType(functionStmt.typeToken.type);
        }
        else {
            currentFuncReturnType = Type.UNKNOWN;
        }
 
        analyse(functionStmt.body);

        if (functionStmt.typeToken != null && !alwaysReturns(functionStmt.body)) {
            VVPLController.error(functionStmt.name.line, ErrorTypeStrings.TYPE_ERROR, String.format("Function %s must return a value in all code paths", functionStmt.name.lexeme));
        }

        currentEnvironment = oldTable;
        currentFuncReturnType = Type.UNKNOWN;
        return null;
    }

    /** @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen */
    /**
     * Analyses a return statement by using {@link #currentFuncReturnType} set by {@link #visitFunctionStmt(FunctionStmt)}.
     * Error is raised if:
     *  - A non-void type is returned in a void function
     *  - type of return statement does not match declared type
     * @param returnStmt the return statement
     * @author: Carl-Emil Dons Christensen, Lucas Bergholdt Hansen
     */
    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        // Handling void returns (return;)
        if (returnStmt.returnValue == null) {
            if (currentFuncReturnType != Type.UNKNOWN) {
                VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Function is not a void function.");
            } 
            else {
                return null;
            }
        }
        // Handling non-void returns
        Type exprType = analyse(returnStmt.returnValue);
        if (exprType == Type.UNKNOWN) {
            return null;
        }
        else if (exprType != currentFuncReturnType) {
            VVPLController.error(returnStmt.returnKeyword.line, ErrorTypeStrings.TYPE_ERROR, "Type of returned value does not match declared return type of function");
            return null;
        } 
        else {
            return null;
        }
    }

      /** 
     * Analyses a Call Statement.
     * An error is raised if type of arguments does not match type of declared parameters
     * @param call the call expression
     * @return the type of the call expression; {@link Type#UNKNOWN} if an error has occured
     * @author Carl-Emil Dons Christensen
     */
    @Override
    public Type visitCallExpr(Call call) {
        FunctionStmt functionStmt = functionsTable.get(call.id.lexeme);

        List<Param> params = functionStmt.params;
        List<Expr> args = call.arguments;

        for (int i = 0; i < args.size(); i++) {
            Type argumentType = analyse(args.get(i));
            Type paramType = convertVariableType(params.get(i).typeToken.type);
            
            if (argumentType != paramType) {
                VVPLController.error(call.id.line, ErrorTypeStrings.TYPE_ERROR, String.format("Type of given argument %s does not match the required type %s", argumentType, paramType));
                // Do not return. Report all argument type errors.
            }
        }

        if (functionStmt.typeToken != null) {
            return convertVariableType(functionStmt.typeToken.type);
        }
        else {
            return Type.UNKNOWN;
        }
    }
}
