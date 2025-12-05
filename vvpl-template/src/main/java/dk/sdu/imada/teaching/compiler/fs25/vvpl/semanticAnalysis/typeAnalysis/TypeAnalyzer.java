package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.BOOL_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.FALSE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NOT;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NUMBER;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NUMBER_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.STRING;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.STRING_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.TRUE;

import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;


public class TypeAnalyzer implements ExprVisitor<Type>, StmtVisitor<Void> {

    private SymbolTable currentEnviroment = new SymbolTable();
    private List<String> typeErrors = new LinkedList<>();
    private List<Stmt> program;

    public TypeAnalyzer(List<Stmt> program) {
        this.program = program;
    }

    public List<String> analyse() {
        for (Stmt stmt : program) {
            analyse(stmt);
        }
        return this.typeErrors;
    }

    private void analyse(Stmt stmt) {
        stmt.accept(this);
    }

    private void analyse(Expr expr) {
        expr.accept(this);
    }

    /* ------------------------------------- Expressions / Statements relateret til Symbol Table. ALL COMPLETED -------------------------  */

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        if (varDecl.expr == null) {
            typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + varDecl.id.line
                    + ": VarDecl must be initialized with a value.");
            return null;
        }
        else {
            currentEnviroment.define(varDecl.id.lexeme, varDecl.expr.accept(this));
            return null;
        }
    }

    @Override
    public Type visitIdentifierExpr(Identifier identifier) {
        return currentEnviroment.get(identifier.id.lexeme);
    }

    @Override
    public Type visitAssignExpr(Assign assign) {
        Type currType = currentEnviroment.get(assign.ID.lexeme);
        Type exprType = assign.expr.accept(this);

        /* 
            C-E: Niels har dette i hans kode. Men vi antager at: 1. varDecl er foregået foruden denne funktion (ScopeAnalyzer's opgave) 2. type er blevet erklæret (visitvarDecl's opgave) */
                /*
                    if (currType == Type.UNKNOWN) {
                        currentEnviroment.assign(assign.ID.lexeme, exprType);
                        return exprType;
                }
        */

        if (currType == exprType) {
            return currType;   
        }
        else {
            typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + assign.ID.line
                    + ": current type of identifier [insert name here] does not match the type of the given expression");
            return currType; // return identifier's current type.
        }
    }

  /* ---------------------------- Expressions, nedefra og op af grammaren. ------------------------ */
    @Override // COMPLETED
    public Type visitLiteralExpr(Literal literal) {
        return switch (literal.token.literal) {
            case Double d -> Type.NUMBER;
            case Boolean b -> Type.BOOL;
            case String s -> Type.STRING;
            // Unreachable
            default -> null;    // C-E: Måske return Type.UNKNOWN her?
        };
    }

    @Override // COMPLETED
    public Type visitUnaryExpr(Unary unary) {
        Type exprType = unary.expr.accept(this);

        if (unary.operator.type == NOT && exprType != Type.BOOL) {
            typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + unary.operator.line
                    + ": operator NOT can only precede a boolean expression.");
                    return Type.UNKNOWN;
        }
        else {
            return exprType;
        }
    }

    @Override // COMPLETED
    public Type visitBinaryExpr(Binary binary) {
        Type left = binary.left.accept(this);
        Type right = binary.right.accept(this);

        switch (binary.operator.type) {
            case PLUS:
            case MINUS:
            case DIV:
            case MULT:    
                if (left == Type.NUMBER && right == Type.NUMBER) {
                    return Type.NUMBER;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] only accepts numbers.");
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
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] only accepts numbers.");
                    return Type.UNKNOWN;
                }
            case EQUALS:
            case NOT_EQUALS:
                if (left == right) {
                    return Type.BOOL;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + binary.operator.line
                    + ": operator [insert name here] can only compare two expressions of the same type.");
                    return Type.UNKNOWN;
                }

            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override   // COMPLETED
    public Type visitLogicalExpr(Logical logical) {
        Type left = logical.left.accept(this);
        Type right = logical.right.accept(this);

        switch (logical.operator.type) {
            case OR, AND: 
                if (left == Type.BOOL && right == Type.BOOL) {
                    return Type.BOOL;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + logical.operator.line
                    + ": operator [insert name here] only accepts booleans.");
                    return Type.UNKNOWN;
                }
            default:
                // Unreachable
                throw new UnsupportedOperationException();
        }
    }

    @Override // MISSING
    @SuppressWarnings("all")
    public Type visitCastExpr(Cast cast) {
        Type cast_from = cast.expr.accept(this);    // Type of expression before cast
        Type cast_to = cast.type;                  // Type of the type we want to cast to.
        switch (cast_from.type) {
            case NUMBER:
                if (cast_to.type == STRING_TYPE) {
                    // Return a STRING_TYPE.
                    return cast_to; //C-E Problem: Denne Type vil KUN have en type, e.g. "NUMBER_TYPE", men ikke 
                }
                if (cast_to.type == BOOL_TYPE) {
                    // Return a STRING_TYPE.
                    return cast_to;
                }
                else {
                    typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.type.line
                    + ": can only cast NUMBER to string or bool.");
                }
            case STRING_TYPE:
                if (cast_to.type == NUMBER_TYPE) {
                    // prøver at caste fra String til Number. Kræver at alle elementer i String er tal.

                    if !(string_to_number_possible(cast_from.literal)) {
                        // go through string and see if it consists of digits only. If not, error
                        typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.type.line
                    + ": not possible to cast string to number.");
                    }
                    else {
                        typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + cast.type.line
                        + ": can only cast string to number");
                    }
                }   
        }
    }

    @Override //SKIP: Function related.
    public Type visitCallExpr(Call call) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitCallExpr'");
    }

    /* ------------------------- Statements ---------------------------- */
    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitExprStmt'");
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWhileStmt'");
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStmt'");
    }

    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        Type exprtoken = printStmt.expr.accept(this); // e.g. STRING_TYPE
            if (exprtoken.type != STRING) {
                typeErrors.add(ErrorTypeStrings.TYPE_ERROR + ", line " + printStmt.token.line
                    + ": PrintStmt only accepts strings.");
            }
        return null;
    }

    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBlockStmt'");
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
