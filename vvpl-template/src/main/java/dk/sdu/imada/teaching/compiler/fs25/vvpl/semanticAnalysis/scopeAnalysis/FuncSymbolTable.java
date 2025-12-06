package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.scopeAnalysis;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.FunctionStmt;

public class FuncSymbolTable {
    private Map<String, FunctionStmt> symbols = new HashMap<>();       // Maps a name/identifier (=symbol) their AST-node FunctionStmt

    /* Constructors */
    public FuncSymbolTable() {
    }

    /* Methods */
    public void define(String symbol, FunctionStmt functionStmt) throws SymbolTableException {
        if (contains(symbol)) {
            throw new SymbolTableException();
            // Return error if Symbol is in current or any outer scope. Redefinition of symbol is not allowed unless in function.
        }
        symbols.put(symbol, functionStmt);
    }

    public void assign(String symbol, FunctionStmt functionStmt) throws SymbolTableException {
        if (symbols.containsKey(symbol)) {
            symbols.put(symbol, functionStmt);
            return;
        }
        throw new SymbolTableException();
    }

    public FunctionStmt get(String symbol) throws SymbolTableException {
    if (symbols.containsKey(symbol)) {
        return symbols.get(symbol);
    }

    throw new SymbolTableException();
}

    public boolean contains(String symbol) {
        if (symbols.containsKey(symbol)) {
            return true;
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
    public final FunctionStmtType type;
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