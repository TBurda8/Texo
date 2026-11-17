package texo;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static texo.TokenType.*;

class StmtTest {
    @Test
    void constructsPrintStatement() {
        Stmt statement = new Stmt.Print(
                new Expr.Literal("Hello")
        );

        assertInstanceOf(Stmt.Print.class, statement);

        Stmt.Print print = (Stmt.Print) statement;

        assertEquals(
                "Hello",
                ((Expr.Literal) print.expression()).value()
        );
    }

    @Test
    void functionCopiesParametersAndBody() {
        List<Token> parameters = new ArrayList<>();
        List<Stmt> body = new ArrayList<>();

        parameters.add(
                new Token(IDENTIFIER, "value", null, 1, 10)
        );

        body.add(
                new Stmt.Return(
                        new Token(OUT, "out", null, 2, 5),
                        new Expr.Variable(parameters.get(0))
                )
        );

        Stmt.Function function = new Stmt.Function(
                new Token(IDENTIFIER, "identity", null, 1, 7),
                parameters,
                body
        );

        parameters.clear();
        body.clear();

        assertEquals(1, function.parameters().size());
        assertEquals(1, function.body().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> function.body().clear()
        );
    }
}