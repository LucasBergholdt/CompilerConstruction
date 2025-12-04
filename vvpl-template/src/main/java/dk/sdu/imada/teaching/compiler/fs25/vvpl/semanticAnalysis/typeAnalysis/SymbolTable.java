package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.TokenType;


/* C-E: Maps symbols (variables) to their values. */
public class SymbolTable {
    private Map<String, Type> symbols = new HashMap<>();       // Maps a name/identifier (=symbol) to attributes TokenType and Object literal (=values)
    public SymbolTable outer = null;

    public SymbolTable() {
    }

    public SymbolTable(SymbolTable outer) {
        this.outer = outer;
    }

    public void define(String symbol, Type type) throws SymbolTableException {
        if (contains(symbol)) {
            throw new SymbolTableException();
            // Return error if Symbol is in current or any outer scope. Redefinition of symbol is not allowed.
        }
        symbols.put(symbol, type);
    }

    // måske ikke rigtigt
    public void assign(String symbol, Type type){
        if (symbols.containsKey(symbol)) {
            symbols.put(symbol, type);
            return;
        }

        if (outer != null) {
            outer.assign(symbol, type);
            return;
        }
        // Unreachable. ScopeAnalyzer has checked already.
        return;
    }

    // get value. (Lavet af CE)
    public Type get(String symbol) {
    if (symbols.containsKey(symbol)) {
        return symbols.get(symbol);
    }

    if (outer != null) {
        return outer.get(symbol);
    }
    // Unreachable. ScopeAnalyzer has checked already.
    return null;

}

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