package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;



/* C-E: Maps symbols (variables) to their values. */
public class SymbolTable {
    private Map<String, Token> symbols = new HashMap<>();
    public SymbolTable outer = null;

    public SymbolTable() {
    }

    public SymbolTable(SymbolTable outer) {
        this.outer = outer;
    }

    // define symbol
    public void define(String symbol, Token token) throws SymbolTableException {
        if (contains(symbol)) {
            throw new SymbolTableException();
            // Return error if Symbol is in current or any outer scope. Redefinition of symbol is not allowed.
        }
        symbols.put(symbol, token);
    }

    // måske ikke rigtigt
    public void assign(String symbol, Token token) throws SymbolTableException {
        if (symbols.containsKey(symbol)) {
            symbols.put(symbol, token);
            return;
        }

        if (outer != null) {
            outer.assign(symbol, token);
            return;
            }
        throw new SymbolTableException();
    }

    // get value. (Lavet af CE)
    public Token get(String symbol) throws SymbolTableException {
    if (symbols.containsKey(symbol)) {
        return symbols.get(symbol);
    }

    if (outer != null) {
        return outer.get(symbol);
    }

    throw new SymbolTableException();
}



    // contains.
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

class SymbolTableException extends Exception {
    // TODO create constructor with string for cause
}