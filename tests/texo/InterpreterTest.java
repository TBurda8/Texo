package texo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private Object evaluate(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(lexer.hasErrors(), String.join("\n", lexer.getErrors()));

        Parser parser = new Parser(tokens);
        Expr expression = parser.parseExpression();

        assertFalse(parser.hasErrors(), String.join("\n", parser.getErrors()));

        return new Interpreter().evaluate(expression);
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
}