package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

import java.util.List;

interface VVPLCallable {
    public Object call(Interpreter interpreter, List<Object> arguments);
}
