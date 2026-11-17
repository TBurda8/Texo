package texo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private List<Stmt> parseProgram(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(lexer.hasErrors(), String.join("\n", lexer.getErrors()));

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        assertFalse(parser.hasErrors(), String.join("\n", parser.getErrors()));

        return statements;
    }

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

    @Test
    void executesAssignAndPrint() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        Interpreter interpreter = new Interpreter();

        interpreter.interpret(parseProgram("""
                score = 10;
                score = score + 5;
                print score;
                """));
        String result = bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");

        assertEquals("15\n", result);
    }

    @Test
    void printsTexoValues() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        Interpreter interpreter = new Interpreter();

        interpreter.interpret(parseProgram("""
                print "hello";
                print true;
                print 2.5;
                print 2;
                """));

        String result = bytes.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");

        assertEquals("""
                hello
                true
                2.5
                2
                """, result);
    }

}