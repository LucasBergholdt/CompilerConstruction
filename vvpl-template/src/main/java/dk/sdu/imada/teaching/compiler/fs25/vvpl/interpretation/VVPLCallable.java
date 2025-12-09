package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.List;

interface VVPLCallable {

    // Jeg antager, at vi ikke skal implementere et "arity()"-check, da vi ikke skal checke for runtime fejl?
    
    // arity () ville normalt bruges til at checke om antallet er parametre er tilstrækkelige i runtime. 
    
    public Object call(Interpreter interpreter, List<Object> arguments);
}
