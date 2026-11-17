package texo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static texo.TokenType.*;

class ExprTest {
    @Test
    void constructsBinaryExpression() {
        Token plus = new Token(ADD, "+", null, 1, 3);

        Expr expression = new Expr.Binary(
                new Expr.Literal(2.0),
                plus,
                new Expr.Literal(3.0)
        );

        assertInstanceOf(Expr.Binary.class, expression);

        Expr.Binary binary = (Expr.Binary) expression;

        assertEquals(2.0, ((Expr.Literal) binary.left()).value());
        assertEquals(ADD, binary.operator().type);
        assertEquals(3.0, ((Expr.Literal) binary.right()).value());
    }

    @Test
    void copiesFunctionArguments() {
        Token name = new Token(
                IDENTIFIER,
                "multiply",
                null,
                1,
                1
        );

        Token closingParen = new Token(
                RIGHT_PAREN,
                ")",
                null,
                1,
                14
        );

        Expr call = new Expr.Call(
                new Expr.Variable(name),
                closingParen,
                List.of(
                        new Expr.Literal(2.0),
                        new Expr.Literal(3.0)
                )
        );

        Expr.Call functionCall = (Expr.Call) call;

        assertEquals(2, functionCall.arguments().size());
        assertEquals("multiply", ((Expr.Variable) functionCall.callee()).name().text);
    }
}