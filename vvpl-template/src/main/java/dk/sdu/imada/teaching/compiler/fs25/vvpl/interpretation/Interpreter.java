package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

public class Interpreter implements StmtVisitor<Void>, ExprVisitor<Object> {

    // private Environment env = new Environment(null);

    final Environment globals = new Environment(null);
    private Environment env = globals; // Globals can only be changed in the outmost scope.

    public void interpret(List<Stmt> stmts) {
        // execute
        try {
            // System.out.println("Lets try and execute.");
            for (Stmt s : stmts) {
                execute(s);
            }
        } catch (Exception e) {
            // System.out.println("Something went wrong.");
            // TODO: handle exception

            // Vi skal ikke håndtere runtime exceptions, right? /Lasse
        }

    }

    // Stringify for at se output.
    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private void execute(Stmt s) {
        s.accept(this);
    }

    private Object evaluate(Expr e) {
        return e.accept(this);
    }

    @Override
    public Object visitAssignExpr(Assign assign) {
        Object value = evaluate(assign.expr);
        env.assign(assign.ID.lexeme, value);
        // System.out.println("AssignExpr: "+ value + " to " +assign.ID.lexeme);
        return value;
    }

    // C-E: Skal returnere den Expr som assignment er bundet til.
    @Override
    public Object visitIdentifierExpr(Identifier identifier) {
        return env.get(identifier.id.lexeme);

    }

    @Override
    // Also evalutes reserved keywords. 
    public Object visitLiteralExpr(Literal literal) {
        if (literal.token.type == TokenType.TRUE) {
            return true;
        } else if (literal.token.type == TokenType.FALSE) {
            return false;
        } else {
            return literal.token.literal; // Retrieve literal value from token.
        }
    }

    @Override
    public Object visitLogicalExpr(Logical logical) {
        
        Object left = evaluate(logical.left);

        if (logical.operator.type == TokenType.OR) {
            // Short-circuit: If OR and left is true, return immediatly.
            if (isTruthy(left))
                return left;

        } else { // Must be AND.
            // If AND, and left is not true, dont bother evluating right.case
            if (!isTruthy(left))
                return left;
        }

        return evaluate(logical.right);
    }

    @Override
    public Object visitBinaryExpr(Binary binary) {
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        System.out.println("Visiting a binary expression");
        
        System.out.println("Left: "+left);
        System.out.println("right: "+right);

        // System.out.println("operator type: "+binary.operator.type);

        switch (binary.operator.type) {
            case SUB:
                return (double) left - (double) right;
            case PLUS:
                return (double) left + (double) right;
            case DIV:
                return (double) left / (double) right;
            case MULT:
                return (double) left * (double) right;
            case NOT_EQUALS:
                return !isEqual(left, right);
            case EQUALS:
                return isEqual(left, right);
            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQUAL: // Should not be implemented with "isEqual"?
                return (double) left >= (double) right;
            case LESS:
                return (double) left < (double) right;
            case LESS_EQUAL:
                return (double) left <= (double) right;

        }
        return null;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;
        return left.equals(right);
    }

    @Override
    public Object visitUnaryExpr(Unary unary) {
        Object right = evaluate(unary.expr);

        switch (unary.operator.type) {
            case MINUS:
                return -(double) right;
            case NOT:
                return !isTruthy(right);
        }
        // Unreachable.
        return null;
    }

    // C-E: Hjælpefunktion til visitUnary. Gør at alle datatyper kan anses som
    // Booleans.
    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;

        // TODO: LA: Overvej om dette er stedet til at evaluere 13 til true?
        // TODO: Vigtigt at teste, om denne funktionalitet virker!
        if (object instanceof Number) {
            System.err.println("Number check:"+ object);
            // Du havde her skrevet object.equals(13) før. Jeg tror ikke dette virker.
            // Numbers opbevares som doubles i systemet så du ville sammenligne 13.0 med 13 hvilket ikke er equals så du ville få false.
            // Sammenligner med 13.0 i stedet. Tror jeg vil virke. Men ved ikke om det er robust. Sikkert fint? /Lucas
            if (object.equals(13.0)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        executeBlock(blockStmt.stmts, new Environment(env));
        return null;
    }

    /* LA: Helper for visitBlockStmt. Create new environment for the blocks scope */
    void executeBlock(List<Stmt> body, Environment environment) {
        Environment previous = this.env; // save current, so we can get back to it after.
        try {
            this.env = environment;

            for (Stmt statement : body) {
                execute(statement);
            }
        } finally {
            this.env = previous; // retore former environment.
        }
    }

    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        Object value = null;
        /* Hvis VarDecl har fået en expr med, så evaluér denne. */
        if (varDecl.expr != null) {
            value = evaluate(varDecl.expr);
        }

        System.out.println("varDecl: "+value+" is assigned to "+varDecl.id.lexeme);
        env.define(varDecl.id.lexeme, value);
        return null;
    }

    @Override
    public Object visitCallExpr(Call call) {
        Object callee = evaluate(call.callee);

        List<Object> evaluatedArgs = new LinkedList<>();
        for (Expr arg : call.arguments) {
            evaluatedArgs.add(evaluate(arg));
        }

        // ... RuntimeError not necessary

        VVPLCallable function = (VVPLCallable) callee;
        return function.call(this, evaluatedArgs);
    }

    @Override
    public Object visitCastExpr(Cast cast) {
        evaluate(cast.expr);
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        evaluate(exprStmt.expr);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        while (isTruthy(evaluate(whileStmt.conditional))) {
            execute(whileStmt.body);
        }
        return null;
    }

    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        if (isTruthy(evaluate(ifStmt.cond))) {
            execute(ifStmt.thenBlock);
        } else if (ifStmt.elseBlock != null) {
            execute(ifStmt.elseBlock);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        Object value = evaluate(printStmt.expr);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    // Helps us unwind past the visit methods back to the code that began the
    // executing body.
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        Object value = null; // return null if no return value.
        if (returnStmt.returnValue != null)
            value = evaluate(returnStmt.returnValue);

        // Getting from the top of the call stack back to call().
        // Unwind back to where the function call began.
        throw new ReturnExcep(value);
    }

    @Override
    // Take compile time representation (node) and convert to runtime
    // interpretation.
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        VVPLFunction function = new VVPLFunction(functionStmt);
        env.define(functionStmt.name.lexeme, function);
        return null;

    }

    /*
     * CE: Dette havde hun i sin interpreter
     * 
     * @Override
     * public Object visitVar(Variable variable) {
     * return null;
     * // return env.get(variable.name);
     * }
     */
}
