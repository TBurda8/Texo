package texo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private Expr parseExpression(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(lexer.hasErrors(), String.join("\n", lexer.getErrors()));

        Parser parser = new Parser(tokens);
        Expr expression = parser.parseExpression();

        assertFalse(parser.hasErrors(), String.join("\n", parser.getErrors()));

        return expression;
    }

    private Object evaluate(String source) {
        return new Interpreter().evaluate(parseExpression(source));
    }

    @Test
    void evaluatePrecedence() {
        assertEquals(14.0, evaluate("2 + 3 * 4"));
        assertEquals(20.0, evaluate("(2 + 3) * 4"));
    }

    @Test
    void evaluateComparison() {
        assertEquals(true, evaluate("10 >= 5"));
        assertEquals(false, evaluate("10 < 5"));
        assertEquals(true, evaluate("3 == 3"));
    }

    @Test
    void evaluatesUnary() {
        assertEquals(-5.0, evaluate("-5"));
        assertEquals(true, evaluate("!false"));
    }

    @Test
    void reportsBadOperand() {
        RuntimeError error = assertThrows(
                RuntimeError.class, () -> evaluate("\"hello\" - 2")
        );

        assertTrue(error.getMessage().contains("requires numbers"));
    }

    @Test
    void reportsDivByZero() {
        RuntimeError error = assertThrows(
                RuntimeError.class, () -> evaluate("10 / 0")
        );

        assertEquals("Cannot divide by zero.", error.getMessage());
    }

    @Test
    void storesAndUpdatesVar() {
        Interpreter interpreter = new Interpreter();

        Object first = interpreter.evaluate(parseExpression("num = 10"));
        Object second = interpreter.evaluate(parseExpression("num = num + 5"));
        assertEquals(10.0, first);
        assertEquals(15.0, second);
    }

    @Test
    void chainDeclaration() {
        Interpreter interpreter = new Interpreter();

        assertEquals(10.0, interpreter.evaluate(parseExpression("a = b = 10")));
        assertEquals(10.0, interpreter.evaluate(parseExpression("a")));
        assertEquals(10.0, interpreter.evaluate(parseExpression("b")));
    }

    @Test
    void undefinedVar() {
        Interpreter interpreter = new Interpreter();

        RuntimeError error = assertThrows(RuntimeError.class, () -> interpreter.evaluate(
                parseExpression("missing")
        ));

        assertEquals("Undefined variable 'missing'.", error.getMessage());
    }

}