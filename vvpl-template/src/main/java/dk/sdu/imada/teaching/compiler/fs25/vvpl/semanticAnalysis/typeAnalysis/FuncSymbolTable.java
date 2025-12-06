package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis.typeAnalysis;

import java.util.HashMap;
import java.util.Map;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt.FunctionStmt;

public class FuncSymbolTable {
    private Map<String, FunctionStmt> symbols = new HashMap<>();       // Maps a name/identifier (=symbol) to their AST-node FunctionStmt

    /* Constructors */
    public FuncSymbolTable() {
    }

    /* Methods */
    public void define(String symbol, FunctionStmt functionStmt) {
        symbols.put(symbol, functionStmt);
    }

    public void assign(String symbol, FunctionStmt functionStmt) {
            symbols.put(symbol, functionStmt);
            return;
    }

    public FunctionStmt get(String symbol) {
        return symbols.get(symbol);
    }


}