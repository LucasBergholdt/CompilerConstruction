package dk.sdu.imada.teaching.compiler.fs25.vvpl;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 * 
 * This class is not necessary for Assignment 1 but for Assignment 3
 */
public class ErrorTypeStrings {
    
    // lexical errors (e.g., wrong symbols)
    public static final String SCAN_ERROR   = "SCAN_ERROR";
    
    // syntactic errors (e.g., missing or surplus tokens)
    public static final String PARSE_ERROR  = "PARSE_ERROR";

    // undeclared variables variable redefinitions in same scope
    public static final String SCOPE_ERROR  = "SCOPE_ERROR"; 

    // incompatible types in assignments
    public static final String TYPE_ERROR   = "TYPE_ERROR"; 

    // e.g., wrong number of parameters (or too many) in call
    public static final String FUNC_ERROR   = "FUNCTION_ERROR";  

    // behavior that you forbid at runtime
    public static final String RUNTIME_ERROR = "RUNTIME_ERROR";
}
