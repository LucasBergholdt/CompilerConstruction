package dk.sdu.imada.teaching.compiler.fs25.vvpl;

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
