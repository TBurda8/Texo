package texo;

import java.util.List;

interface TexoCallable {
    int arity();

    Object call(
            Interpreter interpreter,
            List<Object> arguments
    );
}
