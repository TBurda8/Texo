package texo;

import java.util.HashMap;
import java.util.Map;

class Enviroment {
    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name) {
        if (values.containsKey(name.text)) {
            return values.get(name.text);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.text + "'.");
    }

    void assign(Token name, Object value) {
        values.put(name.text, value);
    }
}
