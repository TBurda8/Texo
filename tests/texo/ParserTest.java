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

    private List<Stmt> parseProgram(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        assertFalse(lexer.hasErrors(), String.join("\n", lexer.getErrors()));

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        assertFalse(parser.hasErrors(), String.join("\n", parser.getErrors()));

        return statements;
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

    @Test
    void parsesFunctionCall() {
        Expr expression = parse("multiply(2, 3 + 4)");

        assertInstanceOf(Expr.Call.class, expression);

        Expr.Call call = (Expr.Call) expression;

        assertInstanceOf(Expr.Variable.class, call.callee());

        Expr.Variable callee = (Expr.Variable) call.callee();

        assertEquals("multiply", callee.name().text);
        assertEquals(2, call.arguments().size());

        assertEquals(2.0, ((Expr.Literal) call.arguments().get(0)).value());

        assertInstanceOf(Expr.Binary.class, call.arguments().get(1));

        Expr.Binary addition = (Expr.Binary) call.arguments().get(1);

        assertEquals(ADD, addition.operator().type);
    }

    @Test
    void parseAssign() {
        Expr expression = parse("result = 2 + 3");

        assertInstanceOf(Expr.Assign.class, expression);

        Expr.Assign assignment = (Expr.Assign) expression;

        assertEquals("result", assignment.name().text);

        assertInstanceOf(Expr.Binary.class, assignment.value());

        Expr.Binary addition = (Expr.Binary) assignment.value();

        assertEquals(ADD, addition.operator().type);
    }

    @Test
    void assignmentGoesRight() {
        Expr expression = parse("a = b = 10");

        Expr.Assign first = (Expr.Assign) expression;

        assertEquals("a", first.name().text);
        assertInstanceOf(Expr.Assign.class, first.value());

        Expr.Assign second = (Expr.Assign) first.value();

        assertEquals("b", second.name().text);
        assertEquals(10.0, ((Expr.Literal) second.value()).value());
    }

    @Test
    void badAssignTarget() {
        Parser parser = createParser("(2 + 3) = 10");

        Expr expression = parser.parseExpression();

        assertNull(expression);
        assertTrue(parser.hasErrors());

        assertTrue(parser.getErrors().get(0).contains("Invalid assignment target"));
    }

    @Test
    void parsePrint() {
        List<Stmt> statements = parseProgram("print 2 + 3;");

        assertEquals(1, statements.size());
        assertInstanceOf(Stmt.Print.class, statements.get(0));

        Stmt.Print print = (Stmt.Print) statements.get(0);

        assertInstanceOf(Expr.Binary.class, print.expression());
    }

    @Test
    void parseMultipleStatements() {
        List<Stmt> statements = parseProgram("""
                result = 10;
                multiply(result);
                """);

        assertEquals(2, statements.size());

        Stmt.Expression first = (Stmt.Expression) statements.get(0);
        Stmt.Expression second = (Stmt.Expression) statements.get(1);

        assertInstanceOf(Expr.Assign.class, first.expression());
        assertInstanceOf(Expr.Call.class, second.expression());
    }
}