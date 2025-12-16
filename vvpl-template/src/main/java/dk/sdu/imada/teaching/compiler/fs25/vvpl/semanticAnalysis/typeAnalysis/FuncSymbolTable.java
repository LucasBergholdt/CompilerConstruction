package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;

import java.util.HashMap;
import java.util.Map;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.FunctionStmt;

/**
* Symbol Table used in type analysis for mapping function symbols to their AST-node
* @author Carl-Emil Dons Christensen
*/
public class FuncSymbolTable {
    private Map<String, FunctionStmt> symbols = new HashMap<>();

    /**
     * Creates a new symbol table with no enclosing scope 
     * Only used once globally.
     */
    public FuncSymbolTable() {
    }

    /**
     * Defines a new mapping in the symbol table
     * @param symbol name of symbol to be stored
     * @param functionStmt FunctionStmt to be associated with symbol
     */
    public void define(String symbol, FunctionStmt functionStmt) {
        symbols.put(symbol, functionStmt);
    }

    /**
     * Returns the FunctionStmt associated with the given symbol
     * @param symbol symbol name to look up
     * @return the associated FunctionStmt
     */
    public FunctionStmt get(String symbol) {
        return symbols.get(symbol);
    }
}