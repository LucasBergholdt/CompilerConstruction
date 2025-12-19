package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.BOOL_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.NUMBER_TYPE;
import static dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType.STRING_TYPE;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ErrorTypeStrings;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.VVPLController;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Expr.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.visitors.*;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;

/**
 * Interpreter that implements the execute method.
 * 
 * @author: Lasse Arpe Kristensen
 */
public class Interpreter implements StmtVisitor<Void>, ExprVisitor<Object> {

    /**
     * Stores the output of the interpreter.
     */
    List<String> output = new ArrayList<String>();

    /**
     * The environments stores the internal state of the interpreter.
     */
    final Environment globals = new Environment(null);

    /**
     * Globals can only be changed in the outmost environnment.
     */
    private Environment env = globals;

    /**
     * 
     * Executes a program, represented as a list of Statements.
     * We assume that the TypeAnalyzer and ScopeAnalyser has ran beforehand.
     * 
     * Will however report a runtimeError if there is an incorrect casting of String
     * to Number.
     * 
     * @param stmts the list of statements representing the program
     * @return the output of the programs execution
     */
    public List<String> interpret(List<Stmt> stmts) {

        try {
            for (Stmt s : stmts) {
                execute(s);
            }
        } catch (RuntimeError e) {
            // See error implementation below.
        }
        return output;

    }

    /**
     * Helper, used to Stringify objects in order to make them visible for printing.
     * @param object the object to stringify
     * @return the string representation of the object
     */
    private String stringify(Object object) {
        if (object == null)
            return "null";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }

    /**
     * Executing statements.
     * @param s the statement to execute
     */
    private void execute(Stmt s) {
        s.accept(this);
    }

    /**
     * Evaluating expressions.
     * @param e the expression to evaluate
     * @return the result of evaluating the expression
     */
    private Object evaluate(Expr e) {
        return e.accept(this);
    }

    /**
     * Evaluates assignment expression.
     * Assigns the expression to the identifier in the environment.
     * @param assign the assignment expression
     * @return the value assigned
     */
    @Override
    public Object visitAssignExpr(Assign assign) {
        Object value = evaluate(assign.expr);
        env.assign(assign.ID.lexeme, value);
        return value;
    }

    /**
     * Returns the expr that an assignment is bound to.
     * @param identifier the identifier expression
     * @return the value bound to the identifier
     */
    @Override
    public Object visitIdentifierExpr(Identifier identifier) {
        return env.get(identifier.id.lexeme);
    }

    /**
     * Evalutes Literals.
     * Also evalutes reserved keywords "true" and "false".
     * @param literal the literal expression
     * @return the value of the literal
     */
    @Override
    public Object visitLiteralExpr(Literal literal) {
        if (literal.token.type == TokenType.TRUE) {
            return true;
        } else if (literal.token.type == TokenType.FALSE) {
            return false;
        } else {
            return literal.token.literal;
        }
    }

    /**
     * Evaluates logical expressions.
     * Uses a "short circuit" to detect OR verus AND.
     * @param logical the logical expression
     * @return the result of the logical operation
     */
    @Override
    public Object visitLogicalExpr(Logical logical) {

        Object left = evaluate(logical.left);

        // Short-circuit: If OR and left is true, return immediatly.
        if (logical.operator.type == TokenType.OR) {

            if (isTruthy(left))
                return left;

        } else { // Must be AND.
            // If AND, and left is not true, dont bother evluating right.
            if (!isTruthy(left))
                return left;
        }

        return evaluate(logical.right);
    }

    /**
     * Evaluates binary expressions.
     * @param binary the binary expression
     * @return the result of the binary expression
     */
    @Override
    public Object visitBinaryExpr(Binary binary) {
        Object left = evaluate(binary.left);
        Object right = evaluate(binary.right);

        switch (binary.operator.type) {
            case SUB:
                return (double) left - (double) right;
            case PLUS:
                return (double) left + (double) right;
            case DIV:
                // Java handles division by 0.
                return (double) left / (double) right;
            case MULT:
                return (double) left * (double) right;
            case NOT_EQUALS:
                return !isEqual(left, right);
            case EQUALS:
                return isEqual(left, right);
            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQUAL:
                return (double) left >= (double) right;
            case LESS:
                return (double) left < (double) right;
            case LESS_EQUAL:
                return (double) left <= (double) right;
            default:
                return null; // Unreachable.

        }
    }

    /**
     * Helper for binary expression
     * @param left the left operand
     * @param right the right operand
     * @return true if operands are equal, otherwise false
     */
    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;
        return left.equals(right);
    }

    /**
     * Evaluates unary expressions.
     * @param unary the unary expression
     * @return the result of the unary operation
     */
    @Override
    public Object visitUnaryExpr(Unary unary) {
        Object right = evaluate(unary.expr);

        switch (unary.operator.type) {
            case MINUS:
                return -(double) right;
            case NOT:
                return !isTruthy(right);
            default:
                return null; // Unreachable
        }

    }

    /**
     * Helper for deciding the boolean value of an object.
     * The number 13 is evaluated to true.
     * @param object the object to check
     * @return true if the object is truthy, false otherwise
     */
    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        if (object instanceof Number) { // 13 should evaluate to true.
            if (object.equals(13.0)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Executes block statements.
     * @param blockStmt the block statement
     * @return null
     */
    @Override
    public Void visitBlockStmt(BlockStmt blockStmt) {
        executeBlock(blockStmt.stmts, new Environment(env));
        return null;
    }

    /**
     * Helper for visitBlockStmt. Executes the environment of the function.
     * @param body the list of statements in the block
     * @param environment the environment to execute in
     */
    void executeBlock(List<Stmt> body, Environment environment) {
        Environment previous = this.env; // save current environment, so we can get back to it after.
        try {
            this.env = environment;

            for (Stmt statement : body) {
                execute(statement);
            }
        } finally {
            this.env = previous; // restore former environment.
        }
    }

    /**
     * Executes a variable declaration.
     * Evaluate a containing expression, if one is present.
     * Then save it in the environment.
     * @param varDecl the variable declaration statement
     * @return null
     */
    @Override
    public Void visitVarDecl(VarDecl varDecl) {
        Object value = null;
        if (varDecl.expr != null) { // If varDecl has an expr, evalaute that first.
            value = evaluate(varDecl.expr);
        }

        env.define(varDecl.id.lexeme, value);
        return null;
    }

    /**
     * Evaluates a function call.
     * Evaluates each argument beforehand,
     * and then adds these to an VVPLCallable function instance.
     * @param call the call expression
     * @return the result of the function call
     */
    @Override
    public Object visitCallExpr(Call call) {
        Object callee = evaluate(call.callee);

        List<Object> evaluatedArgs = new LinkedList<>();
        for (Expr arg : call.arguments) {
            evaluatedArgs.add(evaluate(arg)); //
        }

        // We assume type and number of args are correct.
        // Calls the function with the evaluated args.
        VVPLCallable function = (VVPLCallable) callee;
        return function.call(this, evaluatedArgs);
    }

    /**
     * Evaluates a casting expression. 
     * 
     * In the cast of String -> Number, we do error checking. 
     * @param cast the cast expression
     * @return the result of the cast
     */
    @Override
    public Object visitCastExpr(Cast cast) {

        // Conversion to Boolean.
        if (cast.typeToken.type == BOOL_TYPE) {
            Object value = evaluate(cast.expr);

            return isTruthy(value);
        }

        // Conversion to Bumber.
        if (cast.typeToken.type == NUMBER_TYPE) {

            // CHECK IF WE ARE ALLOWED TO CONVERT
            Object value = evaluate(cast.expr);
            String str = value.toString();
            Double number = null;

            try {
                number = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                throw error(cast.typeToken,
                        ": String \"" + str + "\" is not in the right format to convert to Number.");
            }

            return number;
        }

        // Conversion to String
        if (cast.typeToken.type == STRING_TYPE) {
            Object value = evaluate(cast.expr);
            return value.toString();
        }

        return evaluate(cast.expr);
    }
    /**
     * Evaluates an expression statement. 
     * @param exprStmt the expression statement to evaluate
     * @return null
     */
    @Override
    public Void visitExprStmt(ExprStmt exprStmt) {
        evaluate(exprStmt.expr);
        return null;
    }

    /**
     * Executes "while" statement. Utilizes "isTruthy" helper.
     * @param whileStmt the while statement to evaluate
     * @return null
     */
    @Override
    public Void visitWhileStmt(WhileStmt whileStmt) {
        while (isTruthy(evaluate(whileStmt.conditional))) {
            execute(whileStmt.body);
        }
        return null;
    }

    /**
     * Executes "If" statement and the potential "Else" block. Utilizes "isTruthy" helper.
     * @param ifStmt the if statement to evaluate
     * @return null
     * 
     */
    @Override
    public Void visitIfStmt(IfStmt ifStmt) {
        if (isTruthy(evaluate(ifStmt.cond))) {
            execute(ifStmt.thenBlock);
        } else if (ifStmt.elseBlock != null) {
            execute(ifStmt.elseBlock);
        }
        return null;
    }

    /**
     * Executes "print" (write_to_console) statement. 
     * Adds the evaluation to the global output, so it can be shown to the user.
     * @param printStmt the print statement to evaluate
     * @return null
     */
    @Override
    public Void visitPrintStmt(PrintStmt printStmt) {
        Object value = evaluate(printStmt.expr);
        output.add(stringify(value));

        return null;
    }

    /**
     * Helps us unwind past the visit methods back to the code that began the
     * executing body.
     * Done via the implemented ReturnExcep exception, that extends a Runtime exception. 
     * @param returnStmt the return statement to evaluate
     * @return null
     */
    @Override
    public Void visitReturnStmt(ReturnStmt returnStmt) {
        Object value = null; // return null if no return value.
        if (returnStmt.returnValue != null)
            value = evaluate(returnStmt.returnValue);

        // Getting from the top of the call stack back to call().
        // Unwind back to where the function call began.

        // We store the return value in order to pass it to the environment.
        throw new ReturnExcep(value);
    }

    /**
     * Take compile time representation (node) and convert to runtime
     * interpretation. (stores the function in the environment)
     * @param functionStmt the function stmt to evaluate
     * @return null
     */
    @Override
    public Void visitFunctionStmt(FunctionStmt functionStmt) {
        VVPLFunction function = new VVPLFunction(functionStmt);
        env.define(functionStmt.name.lexeme, function);
        return null;
    }

    /**
     * Same implementation as our {@link ParseError}. 
     * The only difference is that we do not synchronize, but report the error immediately. 
     */
    private static class RuntimeError extends RuntimeException {}

    /**
     * Reports a runtime error
     * @param token the token where the error occured
     * @param message the error message
     * @return a new RuntimeError
     */
    private RuntimeError error(Token token, String message) {
        // Add the error to the list of errors in VVPLController.
        VVPLController.error(token.line, ErrorTypeStrings.RUNTIME_ERROR, message);

        return new RuntimeError();
    }

}
