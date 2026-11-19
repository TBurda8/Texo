package texo;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment enclosing;

    Environment() {
        this(null);
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    Object get(Token name) {
        if (values.containsKey(name.text)) {
            return values.get(name.text);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.text + "'.");
    }

    void assign(Token name, Object value) {
        values.put(name.text, value);
    }
}
