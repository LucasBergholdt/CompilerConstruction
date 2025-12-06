package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

public class SymbolTable {
    private Map<String, Token> symbols = new HashMap<>();       // Maps a name/identifier (=symbol) their Token. #OBS: Vi anvender aldrig denne Token. Evt blot lav LinkedList<String> i stedet for et Table.
    public SymbolTable outer = null;
    public Boolean isGlobal = false;

    /* Constructors */

    // Default (only used for functions)
    public SymbolTable() {
    }

    // Used in global scope only.
    public SymbolTable(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    // Used for nested blocks.
    public SymbolTable(SymbolTable outer) {
        this.outer = outer;
    }

    /* Methods */
    public void define(String symbol, Token token) throws SymbolTableException {
        if (contains(symbol)) {
            throw new SymbolTableException();
            // Return error if Symbol is in current or any outer scope. Redefinition of symbol is not allowed unless in function.
        }
        symbols.put(symbol, token);
    }

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

    public Token get(String symbol) throws SymbolTableException {
    if (symbols.containsKey(symbol)) {
        return symbols.get(symbol);
    }

    if (outer != null) {
        return outer.get(symbol);
    }
    throw new SymbolTableException();
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














/* DEPRECATED: Only here for reference. */
/*
class Attributes {
    public final TokenType type;
    public final Object literal;

    // Constructs attributes given a Token.
    public Attributes(Token token) {
        this.type = token.type;
        this.literal = token.literal;
    }

    // C-E: Unødvendigt for nu. Ordinary constructor for the sake of it. Can be deleted.
    public Attributes(TokenType type, Object literal) {
    this.type = type;
    this.literal = literal;
    }
}
*/