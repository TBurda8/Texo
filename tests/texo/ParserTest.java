package texo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static texo.TokenType.*;

class ParserTest {
    private Parser createParser(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(
                lexer.hasErrors(),
                String.join("\n", lexer.getErrors())
        );

        return new Parser(tokens);
    }

    private Expr parse(String source) {
        Parser parser = createParser(source);
        Expr expression = parser.parseExpression();

        assertFalse(
                parser.hasErrors(),
                String.join("\n", parser.getErrors())
        );

        assertNotNull(expression);
        return expression;
    }

    @Test
    void precedenceTest() {
        Expr expression = parse("2 + 3 * 4");

        assertInstanceOf(Expr.Binary.class, expression);

        Expr.Binary addition = (Expr.Binary) expression;

        assertEquals(ADD, addition.operator().type);
        assertEquals(2.0, ((Expr.Literal) addition.left()).value());

        assertInstanceOf(Expr.Binary.class, addition.right());

        Expr.Binary multi = (Expr.Binary) addition.right();

        assertEquals(MULTIPLY, multi.operator().type);

        assertEquals(3.0, ((Expr.Literal) multi.left()).value());
    }

    @Test
    void parenthPrecedence() {
        Expr expression = parse("(2 + 3) * 4");

        Expr.Binary multi = (Expr.Binary) expression;

        assertEquals(MULTIPLY, multi.operator().type);

        assertInstanceOf(Expr.Grouping.class, multi.left());

        Expr.Grouping grouping = (Expr.Grouping) multi.left();

        Expr.Binary addition = (Expr.Binary) grouping.expression();

        assertEquals(ADD, addition.operator().type);
    }

    @Test
    void parseUnary() {
        Expr expression = parse("-5");

        assertInstanceOf(Expr.Unary.class, expression);

        Expr.Unary unary = (Expr.Unary) expression;

        assertEquals(SUBTRACT, unary.operator().type);
        assertEquals(5.0, ((Expr.Literal) unary.right()).value());
    }

    @Test
    void reportNoCloseParen() {
        Parser parser = createParser("(2 + 3");

        Expr expression = parser.parseExpression();

        assertNull(expression);
        assertTrue(parser.hasErrors());

        assertTrue(parser.getErrors().get(0).contains("Expected ')'"));
    }
}