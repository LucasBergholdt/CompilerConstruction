package dk.sdu.teaching.compiler.fs24.spl.codegen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import dk.sdu.teaching.compiler.fs24.spl.ast.ExprVisitor;
import dk.sdu.teaching.compiler.fs24.spl.ast.Stmt;
import dk.sdu.teaching.compiler.fs24.spl.ast.StmtVisitor;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Assign;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Binary;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Literal;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Logical;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Unary;
import dk.sdu.teaching.compiler.fs24.spl.ast.expr.Variable;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Block;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Expression;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.If;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Print;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.Var;
import dk.sdu.teaching.compiler.fs24.spl.ast.stmt.While;

// Tager vores parsede statements.  


/////// KENDTE BUGS /////////// -Lasse
/// 
/// Kopi af Lasses implementation med fixes til bugs der skabte problemer.
/// Tilføj extension "Better Comments" for tydeligt at se steder jeg har ændret.
/// 

public class LLVMEmitter {
    private StringBuilder sb = new StringBuilder();

    public void generateCode(List<Stmt> statements) {

        IRVisitor generator = new IRVisitor();
        sb.append("Main:\n");
        for (var stmt : statements) {
            generator.generateLine((Stmt) stmt);
        }

        System.out.println(indent(sb));
        // System.out.println(sb.toString()); // Uden indent.
    }

    public String indent(StringBuilder sb) {
        // Creates the 1 indentation where necessary.
        String[] lines = sb.toString().split("\n");
        StringBuilder formatted = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("%") || line.startsWith("br")) {
                formatted.append("\t");
            }
            formatted.append(line).append("\n");
        }
        return formatted.toString();
    }

    public class IRVisitor implements StmtVisitor<Void>, ExprVisitor<String> {

        // For converting to correct operator.
        private static final HashMap<String, String> operatorMap;
        static {
            operatorMap = new HashMap<>();
            operatorMap.put("+", "add");
            operatorMap.put("-", "sub");
            operatorMap.put("*", "mul");
            operatorMap.put("/", "udiv");
            operatorMap.put("==", "eq");
            operatorMap.put("!=", "ne");
            operatorMap.put("<", "ult");
            operatorMap.put("<=", "ule");
            operatorMap.put(">", "ugt");
            operatorMap.put(">=", "uge");
            operatorMap.put("and", "and"); 
            operatorMap.put("or", "or"); 
        }

        // For storing alive variables during compiling.
        private LinkedHashMap<String, Symbol> symbolHashMap = new LinkedHashMap<>();

        public class Symbol {
            boolean isTemporary;
            String llvmName;
            String originalVar;

            public Symbol(String tempName, boolean isTemporary, String originalVar) {
                this.isTemporary = isTemporary;
                this.llvmName = tempName;
                this.originalVar = originalVar;
            }
        }

        // Keeps track of the current amount of temporary variables.
        private static int current_temp = 0;

        // Keeps track of the current label.
        private static int current_label = 0; 

        /*
         * Produces a new label.
         */
        private String newLabel() { 
            String labelName = "L" + current_label;
            current_label++;
            return labelName;
        }

        /*
         * New var to be stored on the symbol table.
         * Should replace the old version, but have not implemented.
         */
        public void newVar(String var, String tempName) {
            Symbol newVar = new Symbol(tempName, false, var);
            symbolHashMap.put(var, newVar);
        }

        /* New temporary variable, does not point to an original variable. */
        public String newTempVar() {
            String temp_name = "t" + current_temp; 
            Symbol new_temp = new Symbol(temp_name, true, null);
            symbolHashMap.put(temp_name, new_temp);
            current_temp++;
            return temp_name;
        }

        /* Used in the generator. */
        public Void generateLine(Stmt stmt) {
            return stmt.accept(this);
        }


        /*
         * For when existing variables get updated.
         * E.g "b=b+1"
         */
        @Override
         public String visitAssignExpr(Assign expr) {

            String rightHandSide = expr.value.accept(this);

            // Create a new var, linked to a new temp (TO PRESERVE SSA)
            String temp = newTempVar();
            newVar(expr.name.lexeme, temp);

            String leftHandSide = "%" + temp + " = ";

            sb.append(leftHandSide + rightHandSide + "\n"); 

            return "%" + temp; 
        }

        /* "add i32 1, %d" */
        @Override
        public String visitBinaryExpr(Binary expr) {

            String left = expr.left.accept(this);
            String right = expr.right.accept(this);
            String operator = operatorMap.get(expr.operator.lexeme);

            String result = newTempVar();

            // Handles both arithemtic and comparison (e.g. also handles appending icmp)
            String prefix;
            String[] comparisonOperators = {"eq", "ne", "ult", "ule", "ugt", "uge"};
            if (Arrays.asList(comparisonOperators).contains(operator)) {
                prefix = "icmp " + operator + " i32 ";
            } else {
                prefix = operator + " i32 ";
            }

            sb.append("%" + result + " = " + prefix + left + ", " + right + "\n");

            return "%" + result;

        }

        @Override
        /*
         * Small known bug: Numbers are doubles, so even integers will be represented as "1.0".
         * In general there are doubles in the examples, but we are to only use the i32 type.
         * So this is probably not important. 
         */

        public String visitLiteralExpr(Literal expr) {
            String lexeme = expr.value.toString();
            
            return lexeme;
        }

        @Override
        /*
         * "or", "and", same implementation as binary.
         */
        public String visitLogicalExpr(Logical expr) {

            String left = expr.left.accept(this);
            String right = expr.right.accept(this);
            String operator = operatorMap.get(expr.operator.lexeme);

            String result = newTempVar();

            sb.append("%" + result + " = " + operator + " i1 " + left + ", " + right + "\n");
            return "%" + result;
        }

        @Override
        /* Not implemented */
        public String visitUnaryExpr(Unary expr) {
            return null;
        }

        @Override
        /* Evaluates variables based on the symbol table. */
        public String visitVariableExpr(Variable expr) {

            Symbol getSymbol = symbolHashMap.get(expr.name.lexeme);
            return "%" + getSymbol.llvmName;

        }

        @Override
        public Void visitBlockStmt(Block stmt) {
            for (Stmt e : stmt.statements) {
                e.accept(this);
            }
             return null;
        }

        @Override
        /*
         * Creates temp to support SSA.
         */
        public Void visitExpressionStmt(Expression stmt) {

            stmt.expression.accept(this);
            return null;
        }

        @Override
        /*
         * Handles if statements.
         * if (cond), then {block}
         */
        public Void visitIfStmt(If stmt) {
            String testLabel = "Test" + newLabel();
            String thenlabel = "IfEq" + newLabel();
            String elseLabel = "IfNeq" + newLabel();
            String endLabel  = "IfEnd" + newLabel();

            // Add the test label and then the branching condition
            sb.append(testLabel + ":\n");
            String cond = stmt.condition.accept(this);
            sb.append("br i1 " + cond + ", label %" + thenlabel + ", label %" + elseLabel + "\n");

            // Then block:
            sb.append(thenlabel + ":\n");
            stmt.thenBranch.accept(this);

            // Else block:
            if (stmt.elseBranch != null) {
                sb.append(elseLabel + ":\n");
                stmt.elseBranch.accept(this);
            }

            sb.append(endLabel + ":\n");

            return null;
        }

        @Override
        /* Should not be implemented. */
        public Void visitPrintStmt(Print stmt) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'visitPrintStmt'");
        }

        @Override
        /*
         * Var is initiaized. 
         * "var x = ...;""
         */
        public Void visitVarStmt(Var stmt) {
            String lexeme = stmt.name.lexeme;

            // Saves var to symbol table, for future get.
            Symbol var = new Symbol(lexeme, false, lexeme);
            symbolHashMap.put(lexeme, var);

            if (stmt.initializer != null) {
                String initializer = stmt.initializer.accept(this);
                sb.append("%" + lexeme + " = " + initializer + "\n");
            } else {
                sb.append("%" + lexeme + "\n");
            }

            return null;
        }

        @Override
        /*
         * Handles while statement.
         * while (cond), then {block}
         */
        public Void visitWhileStmt(While stmt) {

            String testLabel  = "WhileTest" + newLabel();
            String whileLabel = "WhileEql" + newLabel();
            String endLabel   = "WhileEnd" + newLabel();

            sb.append(testLabel + ":\n");

            // while condition:
            String cond = stmt.condition.accept(this);
            sb.append("br i1 " + cond + ", label %" + whileLabel + ", label %" + endLabel + "\n");

            // WhileEql block:
            sb.append(whileLabel + ":\n");
            stmt.body.accept(this);
            sb.append("br label %" + testLabel + "\n");
            sb.append(endLabel + ":\n");

            return null;

        }

    }

}
