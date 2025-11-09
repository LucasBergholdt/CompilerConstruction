/* C-E note til selv: ved ikke om fil skal bruges. */

package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private Map<String, Object> symbols = new HashMap<>();
    public Environment outer = null;


    public Environment(Environment outer) {
        this.outer = outer;
    }

    // define symbol
    public void define(String symbol, Object value) {
        symbols.put(symbol, value);
    }

    public void assign(String symbol, Object value) {
        if (symbols.containsKey(symbol)) {
            symbols.put(symbol, value);
            return;
        }

        if (outer != null) {
            outer.assign(symbol, value);
            return;
        }
    }

    public Object get(String symbol) {
        if (symbols.containsKey(symbol)) {
            return symbols.get(symbol);
        }

        if (outer != null) {
            return outer.get(symbol);
        }

        // Unreachable
        return null;
    }

    // contains. Looks in current and outer scopes.
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