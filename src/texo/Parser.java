package texo;

import java.util.ArrayList;
import java.util.List;

import static texo.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private final List<String> errors = new ArrayList<>();

    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parseExpression() {
        try {
            Expr result = expression();

            if (!isAtEnd()) {
                throw error(
                        peek(),
                        "Expected end of expression."
                );
            }

            return result;
        } catch (ParseError ignored) {
            return null;
        }
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expression = equality();

        if (match(ASSIGN)) {
            Token equals = previous();
            Expr value = assignment();

            if (expression instanceof Expr.Variable variable) {
                return new Expr.Assign(
                        variable.name(),
                        value
                );
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expr equality() {
        Expr expression = comparison();

        while (match(EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();

            expression = new Expr.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expr comparison() {
        Expr expression = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();

            expression = new Expr.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expr term() {
        Expr expression = factor();

        while (match(ADD, SUBTRACT)) {
            Token operator = previous();
            Expr right = factor();

            expression = new Expr.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expr factor() {
        Expr expression = unary();

        while (match(MULTIPLY, DIVIDE)) {
            Token operator = previous();
            Expr right = unary();

            expression = new Expr.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expr unary() {
        if (match(BANG, SUBTRACT)) {
            Token operator = previous();
            Expr right = unary();

            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call() {
        Expr expression = primary();

        while (match(LEFT_PAREN)) {
            expression = finishCall(expression);
        }

        return expression;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguements = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                arguements.add(expression());
            } while (match(COMMA));
        }

        Token closingParen = consume(RIGHT_PAREN, "Expected ')' after function args.");

        return new Expr.Call(callee, closingParen, arguements);
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expression = expression();

            consume(RIGHT_PAREN, "Expected ')' after expression.");

            return new Expr.Grouping(expression);
        }

        throw error(peek(), "Expected expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        String location;

        if (token.type == EOF) {
            location = "at end";
        } else {
            location = "at '" + token.text + "'";
        }

        errors.add(
                "[line " + token.line + ", column " + token.col
                + "] Parser error " + location + ": " + message
        );

        return new ParseError();
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    List<String> getErrors() {
        return List.copyOf(errors);
    }

    private static final class ParseError extends RuntimeException {
    }
}
