package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.List;

/**
 * Allows us to implement different types of functions (built-in, user defined) if necessary.
 * Normally, one would also have the "arity()" method to check number of arguments versus the expected.
 * Arity() not necessary in our case, as the check for expected amount of parameters have been handled earlier in the semantic check.
 * 
 */
interface VVPLCallable {
    public Object call(Interpreter interpreter, List<Object> arguments);
}
