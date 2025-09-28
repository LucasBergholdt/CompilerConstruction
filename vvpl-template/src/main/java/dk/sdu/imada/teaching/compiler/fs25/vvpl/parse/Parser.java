package dk.sdu.imada.teaching.compiler.fs25.vvpl.parse;

import java.util.ArrayList;
import java.util.List;

import dk.sdu.imada.teaching.compiler.fs25.vvpl.ast.Stmt;
import dk.sdu.imada.teaching.compiler.fs25.vvpl.scan.Token;

/**
 * @author Sandra Greiner
 * @version CompilerConstruction FT 2025
 */

public class Parser {

    private List<Token> tokens;
   
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
       
        return statements;
    }

}