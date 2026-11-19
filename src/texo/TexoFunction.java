package texo;

import java.util.List;

final class TexoFunction implements TexoCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    TexoFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.parameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguements) {
        Environment local = new Environment(closure);

        for (int index = 0; index < declaration.parameters().size(); index++) {
            local.assign(declaration.parameters().get(index), arguements.get(index));
        }

        interpreter.executeBlock(declaration.body(), local);

        return null;
    }

    @Override
    public String toString() {
        return "<function " + declaration.name().text + ">";
    }
}
