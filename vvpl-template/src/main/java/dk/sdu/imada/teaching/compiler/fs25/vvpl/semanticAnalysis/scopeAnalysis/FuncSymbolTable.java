package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.SymbolTableException;
import java.util.HashMap;
import java.util.Map;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.FunctionStmt;

/**
* Symbol Table used in scope analysis for mapping function symbols to their AST-node
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
    public void define(String symbol, FunctionStmt functionStmt) throws SymbolTableException {
        if (contains(symbol)) {
            throw new SymbolTableException("Function is already defined in scope.");
        }
        symbols.put(symbol, functionStmt);
    }

    /**
     * Returns the FunctionStmt associated with the given symbol
     * @param symbol symbol name to look up
     * @return the associated FunctionStmt
     */
    public FunctionStmt get(String symbol) throws SymbolTableException {
        if (symbols.containsKey(symbol)) {
            return symbols.get(symbol);
        }
        throw new SymbolTableException("Function is not defined in scope.");
    }

    /**
     * Checks if function is defined the current scope
     * @param symbol symbol name to look up
     * @return true if symbol is in scope, otherwise false
     */
    public boolean contains(String symbol) {
        if (symbols.containsKey(symbol)) {
            return true;
        }
        return false;
    }
}