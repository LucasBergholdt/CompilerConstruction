package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

/**
 * Helps us get back to the top of the call stack with the return value.
 * @author Lasse Arpe Kristensen
 */
class ReturnExcep extends RuntimeException {
    final Object value;

    ReturnExcep(Object value) {
        super(null, null, false, false);
        this.value = value; // The value to return to the top. 
    }
}
