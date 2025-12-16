package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.SymbolTableException;
import java.util.HashMap;
import java.util.Map;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/**
* Symbol Table used in scope analysis for mapping symbols to their token.
* @author Carl-Emil Dons Christensen
*/
public class SymbolTable {
    private Map<String, Token> symbols = new HashMap<>();
    public SymbolTable outer = null;
    public Boolean isGlobal = false;

    /**
     * Creates a new symbol table with no enclosing scope and marks it as the global scope. 
     * Only used once.
     */
    public SymbolTable(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    /**
     * Creates a new symbol table with an enclosing scope.
     * Used for all nested blocks except blocks being used in a FunctionStmt.
     */
    public SymbolTable(SymbolTable outer) {
        this.outer = outer;
    }

    /**
     * Creates a new symbol table with no enclosing scope. 
     * Only used for blocks directly used in a FunctionStmt.
     */
    public SymbolTable() {
    }


    /**
     * Defines a new mapping in the symbol table
     * @param symbol name of symbol to be stored
     * @param token token to be associated with symbol
     */
    public void define(String symbol, Token token) throws SymbolTableException {
        if (contains(symbol)) {
            throw new SymbolTableException("Variable is already defined in scope.");
        }
        symbols.put(symbol, token);
    }

    /**
     * Assigns a token to an existing symbol
     * @param symbol name of symbol to assign
     * @param token token to associate with the symbol
     */
    public void assign(String symbol, Token token) throws SymbolTableException {
        if (symbols.containsKey(symbol)) {
            symbols.put(symbol, token);
            return;
        }

        if (outer != null) {
            outer.assign(symbol, token);
            return;
            }
        throw new SymbolTableException("Variable is not defined in scope.");
    }

    /**
     * Returns the token associated with the given symbol
     * @param symbol symbol name to look up
     * @return the associated token
     */
    public Token get(String symbol) throws SymbolTableException {
    if (symbols.containsKey(symbol)) {
        return symbols.get(symbol);
    }

    if (outer != null) {
        return outer.get(symbol);
    }
    throw new SymbolTableException("Variable is not defined in scope.");
    }

    /**
     * Checks if symbol is in the current scope
     * @param symbol symbol name to look up
     * @return true if symbol is in scope, otherwise false
     */
    public boolean contains(String symbol) {
        if (symbols.containsKey(symbol)) {
            return true;
        }

        if (outer != null) {
            return outer.contains(symbol);
        }

        return false;
    }
}