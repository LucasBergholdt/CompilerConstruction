package dk.sdu.teaching.compiler.fs24.spl.codegen;

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
/// Problem med sampillet mellem VarStmt "var x =..." og BinaryExpr.
/// Dette betyder, at De virker hver for sig "var x = 2;" og "b=b+2"/"b+2", men lige nu virker "var b = 2+2" ikke korrekt. 
/// 
/// Output er SSA og 3AC, men indeholder desværre en del redundans. 
/// Dette skyldes, at binaryExpression både skal kunne virke ifm. 2+2+2 og bare 2+2. 
/// Lige nu resulterer den per default en temp, som er godt ifm. nested expression (2+2+2), men 
/// giver redundant kode ifm. ikke-nested (2+2).
/// 
/// Jeg ved ikke, om man kan detektere hvorvidt binary er en 2+2 eller 2+2+2 før en temp variabel skabes, men det virker ikke sådan.
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
            operatorMap.put("!=", "neq");
            operatorMap.put("!=", "ne");
            operatorMap.put("<", "ult");
            operatorMap.put("<=", "ule");
            operatorMap.put(">", "ugt");
            operatorMap.put(">=", "uge");
            operatorMap.put("and", "AND");
            operatorMap.put("or", "OR");
        }

        // For storing alive variables during compiling.
        private LinkedHashMap<String, Symbol> symbolHashMap = new LinkedHashMap<>();

        public class Symbol {
            boolean isTemporary;
            String llvmName;
            String originalVar;

            // boolean isAlive;
            // int scopeLevel;

            public Symbol(String tempName, boolean isTemporary, String originalVar) {
                this.isTemporary = isTemporary;
                this.llvmName = tempName;
                this.originalVar = originalVar;
            }
        }

        // Keeps track of the current amount of temporary variables.
        private static int current_temp = 0;

        /*
         * New var to be stored on the symbol table.
         * Should replace the old version, but have not implemented.
         */
        public String newVar(String var, String tempName) {
            String varName = var;
            Symbol newVar = new Symbol(tempName, false, var);
            symbolHashMap.put(varName, newVar);
            return varName;
        }

        /* New temporary variable, does not point to an original variable. */
        public String newTempVar() {
            String temp_name = "tempVar" + String.valueOf(current_temp);
            Symbol new_temp = new Symbol(temp_name, true, null);
            symbolHashMap.put(temp_name, new_temp);
            current_temp++;
            return temp_name;
        }

        /* Used in the generator. */
        public Void generateLine(Stmt stmt) {
            return stmt.accept(this);
        }

        @Override
        /*
         * For when existing variables get updated.
         * E.g "b=b+1"
         */
        public String visitAssignExpr(Assign expr) {

            String rightHandSide = expr.value.accept(this);

            // Create a new var, linked to a new temp (TO PRESERVE SSA)
            String temp = newTempVar();
            newVar(expr.name.lexeme, temp);

            String leftHandSide = "%" + temp + " = ";

            return leftHandSide + rightHandSide;
        }

        @Override
        /* "add i32 1, %d" */
        public String visitBinaryExpr(Binary expr) {

            String left = expr.left.accept(this);
            String right = expr.right.accept(this);
            String operator = operatorMap.get(expr.operator.lexeme);

            String result = newTempVar();
            String prefix = operator + " i32 ";

            sb.append("%" + result + " = " + prefix + left + ", " + right + "\n");

            return "%" + result;

        }

        @Override
        /*
         * Af en eller anden grund håndterer hun tal som Doubles,
         * men vil have os til at emitte integers. Det virker som en dårlig løsning at
         * omgøre noget, der er implementeret
         * i hendes scanner. Derfor outputter vi "#.0"...
         */
        public String visitLiteralExpr(Literal expr) {
            Object lexeme = expr.value;
            return lexeme.toString();
        }

        @Override
        /*
         * "or", "and"
         * Stadigvæk i32? Kommer an på brugerens input...
         * "i1" angiver boolean værdi.
         */
        public String visitLogicalExpr(Logical expr) {

            String left = expr.left.accept(this);
            String right = expr.right.accept(this);
            String operator = operatorMap.get(expr.operator.lexeme);

            String result = newTempVar();
            String prefix = operator + " i1 ";

            sb.append("%" + result + " = " + prefix + left + ", " + right + "\n");

            return "%" + result;
        }

        @Override
        /* IKKE FÆRDIG, Der står ikke som om, den skal implmementeres?. */
        public String visitUnaryExpr(Unary expr) {
            String operator = operatorMap.get(expr.operator.lexeme);
            // String expr.right.accept(this);
            return operator + expr.right.accept(this);
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

            sb.append(stmt.expression.accept(this));
            sb.append("\n");

            return null;
        }

        @Override
        /*
         * Jeg er i tvivl om, hvorvidt labels skal hardcodes.
         * Under normale omstændigheder ville man jo lade labels være dynamiske, e.g
         * label1,label2,... osv.
         * 
         * Opgavebeskrivelsen lyder som om, at man "bare" skal hardcode.
         */
        public Void visitIfStmt(If stmt) {

            sb.append("Test:\n");
            String condVar = newTempVar();
            sb.append("%" + condVar + " = icmp " + stmt.condition.accept(this));
            sb.append("\n");
            sb.append("br i1 %" + condVar + ", label %IfEq, label %ifNEq\n");
            sb.append("ifEq:\n");
            stmt.thenBranch.accept(this);
            sb.append("ifNEq:\n");
            stmt.elseBranch.accept(this);
            sb.append("End:\n"); // I tvivl om denne skal returneres. 

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

            sb.append("%" + lexeme + " = ");

            // Saves var to symbol table, for future get.
            Symbol var = new Symbol(lexeme, false, lexeme);
            symbolHashMap.put(lexeme, var);

            sb.append(stmt.initializer.accept(this));
            sb.append(";\n");

            return null;
        }

        @Override
        /*
         * Samme problematik som ovenstående - Man KUNNE lave dynamiske labels - men det
         * ligner ikke, at det efterspørges i opgaven.
         */
        public Void visitWhileStmt(While stmt) {

            sb.append("WhileTest:\n");
            String condVar = newTempVar();
            sb.append("%" + condVar + " = icmp " + stmt.condition.accept(this));
            sb.append("\n");
            sb.append("br i1 %" + condVar + ", label %WhileEql, label %End\n");
            sb.append("WhileEql:\n");
            stmt.body.accept(this);
            sb.append("br label WhileTest\n");
            sb.append("End:\n");

            return null;

        }

    }

}
