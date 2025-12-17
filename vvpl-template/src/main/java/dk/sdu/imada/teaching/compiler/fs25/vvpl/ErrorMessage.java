package dk.sdu.imada.teaching.compiler.fs25.vvpl;

/** 
 * This class represents an error message with line number, error type and message.
 * It implements the Comparable interface for efficient sorting and overrides the 
 * toString method for easy reporting in the format: <error-type>, line <lineNo> <message>
 * @author: Lucas Bergholdt Hansen 
 */
public class ErrorMessage implements Comparable<ErrorMessage> {
    int line;
    String errorType;
    String message;

    public ErrorMessage(int line, String errorType, String message) {
        this.line = line;
        this.errorType = errorType;
        this.message = message;
    }

    @Override
    public int compareTo(ErrorMessage other) {
        return this.line - other.line;
    }

    @Override
    public String toString() {
        return errorType + ", line " + line + " " + message;
    }
}
