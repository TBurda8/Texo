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

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            try {
                statements.add(declaration());
            } catch (ParseError ignored) {
                synchronize();
            }
        }

        return List.copyOf(statements);
    }

    private Stmt statement() {
        if (match(IF)) {
            return ifStatement();
        }

        if (match(PRINT)) {
            return printStatement();
        }

        if (match(PUT)) {
            return inputStatement();
        }

        if (match(OUT)) {
            return returnStatement();
        }

        return expressionStatement();
    }

    private Stmt ifStatement() {
        Expr condition = expression();

        consume(THEN, "Expected 'then' after if condition.");

        List<Stmt> thenBranch = statementsUntil(ELSE, END);
        List<Stmt> elseBranch = List.of();

        if (match(ELSE)) {
            elseBranch = statementsUntil(END);
        }

        consume(END, "Expected 'end' after if statement.");

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() {
        Expr value = expression();

        consume(SEMICOLON, "Expected ';' after printed expression.");

        return new Stmt.Print(value);
    }

    private Stmt inputStatement() {
        Token name = consume(IDENTIFIER, "Expected variable name after 'put'.");

        consume(SEMICOLON, "Expected ';' after input statement.");

        return new Stmt.Input(name);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = expression();

        consume(SEMICOLON, "Expected ';' after returned expression.");

        return new Stmt.Return(keyword, value);
    }

    private Stmt expressionStatement() {
        Expr expression = expression();

        consume(SEMICOLON, "Expected ';' after expression.");

        return new Stmt.Expression(expression);
    }

    private Stmt declaration() {
        if (match(START)) {
            return functionDeclaration();
        }
        return statement();
    }

    private Stmt functionDeclaration() {
        Token name = consume(IDENTIFIER, "Expected function name after 'start'.");
        List<Token> parameters = new ArrayList<>();

        if (match(IN)) {
            consume(LEFT_PAREN, "Expected '(' after 'in'.");

            if (!check(RIGHT_PAREN)) {
                do {
                    parameters.add(consume(IDENTIFIER, "Expected parameter name."));
                } while (match(COMMA));
            }

            consume(RIGHT_PAREN, "Expected ')' after parameters.");
        }

        List<Stmt> body = statementsUntil(END);

        consume(END, "Expected 'end' after function body.");

        return new Stmt.Function(name, parameters, body);
    }

    private List<Stmt> statementsUntil(TokenType... terminators) {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd() && !checkAny(terminators)) {
            statements.add(statement());
        }

        return statements;
    }

    private boolean checkAny(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                return true;
            }
        }

        return false;
    }

    private void synchronize() {
        while (!isAtEnd()) {
            if (current > 0 && previous().type == SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case PRINT, PUT, OUT, IF, WHILE, START -> {
                    return;
                }
                default -> advance();
            }
        }
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
        List<Expr> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token closingParen = consume(RIGHT_PAREN, "Expected ')' after function args.");

        return new Expr.Call(callee, closingParen, arguments);
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
