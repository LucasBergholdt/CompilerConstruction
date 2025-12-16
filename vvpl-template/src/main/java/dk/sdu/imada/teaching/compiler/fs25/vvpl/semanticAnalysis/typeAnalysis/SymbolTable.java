package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;

import java.util.HashMap;
import java.util.Map;

/**
* Symbol Table used in type analysis for mapping symbols to their type.
* @author Carl-Emil Dons Christensen
*/
public class SymbolTable {
    private Map<String, Type> symbols = new HashMap<>();
    public SymbolTable outer = null;

    /**
     * Creates a new symbol table with no enclosing scope.
     * Used for global scope and blocks directly used in a FunctionStmt
     */
    public SymbolTable() {
    }

    /**
     * Creates a new symbol table with an enclosing scope.
     * Used for all nested blocks.
     */
    public SymbolTable(SymbolTable outer) {
        this.outer = outer;
    }

    /**
     * Defines a new mapping in the symbol table
     * @param symbol name of symbol to be stored
     * @param type type of symbol to be associated with symbol
     */
    public void define(String symbol, Type type)  {
        symbols.put(symbol, type);
    }

    /**
     * Assigns a type to an existing symbol
     * @param symbol name of symbol to assign
     * @param type type to associate with the symbol
     */
    public void assign(String symbol, Type type){
        if (symbols.containsKey(symbol)) {
            symbols.put(symbol, type);
            return;
        }

        if (outer != null) {
            outer.assign(symbol, type);
            return;
        }
        // Unreachable.
        return;
    }

    /**
     * Returns the type associated with the given symbol
     * @param symbol symbol name to look up
     * @return the associated type
     */
    public Type get(String symbol) {
    if (symbols.containsKey(symbol)) {
        return symbols.get(symbol);
    }

    if (outer != null) {
        return outer.get(symbol);
    }
    // Unreachable.
    return null;
    }
}



