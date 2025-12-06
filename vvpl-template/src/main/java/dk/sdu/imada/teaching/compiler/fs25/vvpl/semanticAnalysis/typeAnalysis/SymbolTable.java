package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Type> symbols = new HashMap<>();       // Maps a name/identifier (=symbol) to their type.
    public SymbolTable outer = null;

    public SymbolTable() {
    }

    public SymbolTable(SymbolTable outer) {
        this.outer = outer;
    }

    public void define(String symbol, Type type)  {
        symbols.put(symbol, type);
    }

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
}