package dk.sdu.imada.teaching.compiler.fs25.vvpl.semanticAnalysis;

/**
 * Exception thrown when an error occurs in the symbol table.
*/
public class SymbolTableException extends Exception {
    /**
     * Constructs a new SymbolTableException with the specified detail message.
     *
     * @param message a description of the cause of the exception
     */
    public SymbolTableException(String message) {
        super(message);
    }
}
