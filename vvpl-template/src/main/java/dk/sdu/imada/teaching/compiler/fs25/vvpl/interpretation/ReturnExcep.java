package dk.sdu.imada.teaching.compiler.fs25.vvpl.interpretation;

class ReturnExcep extends RuntimeException {
    final Object value;

    ReturnExcep(Object value) {
        super(null, null, false, false); // unimportant exception implementation details.
        this.value = value;
    }
}
