package texo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static texo.TokenType.*;

class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int tokenLine = 1;
    private int tokenColumn = 1;

    Lexer(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            tokenLine = line;
            tokenColumn = column;

            scanToken();
        }
        tokens.add(new Token(EOF, "", null, 1, 1));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        char character = source.charAt(current);
        current++;

        if (character == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        return character;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }

        advance();
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);

        tokens.add(new Token(
                type, text, literal, tokenLine, tokenColumn
        ));

    }

    private void scanToken() {
        char character = advance();

        switch (character) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case ',' -> addToken(COMMA);
            case ';' -> addToken(SEMICOLON);

            case '+' -> addToken(ADD);
            case '-' -> addToken(SUBTRACT);
            case '*' -> addToken(MULTIPLY);
            case '/' -> addToken(DIVIDE);

            case '=' -> addToken(match('=') ? EQUAL_EQUAL : ASSIGN);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);

            case ' ', '\r', '\t', '\n' -> {
                // Nothing
            }

            case '"' -> scanString();
            case '#' -> scanComment();

            default -> {
                if (isDigit(character)) {
                    scanNumber();
                } else if (isIdentifierStart(character)) {
                    scanIdentifier();
                } else {
                    reportError("Unexpected character: " + character);
                }
            }
        }
    }

    private void scanNumber() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        String numberText = source.substring(start, current);
        double value = Double.parseDouble(numberText);

        addToken(NUMBER, value);
    }

    private boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("start", START),
            Map.entry("in", IN),
            Map.entry("out", OUT),
            Map.entry("end", END),
            Map.entry("print", PRINT),
            Map.entry("if", IF),
            Map.entry("then", THEN),
            Map.entry("else", ELSE),
            Map.entry("while", WHILE),
            Map.entry("do", DO),
            Map.entry("put", PUT),
            Map.entry("true", TRUE),
            Map.entry("false", FALSE)
    );

    private void scanIdentifier() {
        while (isIdentifierPart(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, IDENTIFIER);

        addToken(type);
    }

    private boolean isIdentifierStart(char character) {
        return character == '_'
                || character >= 'a' && character <= 'z'
                || character >= 'A' && character <= 'Z';
    }

    private boolean isIdentifierPart(char character) {
        return isIdentifierStart(character) || isDigit(character);
    }

    private void scanString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                reportError("Unfinished string.");
                return;
            }

            advance();
        }

        if (isAtEnd()) {
            reportError("Unfinished string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void scanComment() {
        if (match('/')) {
            scanMultilineComment();
            return;
        }

        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    private void scanMultilineComment() {
        while (!isAtEnd()) {
            if(peek() == '/' && peekNext() == '#') {
                advance();
                advance();
                return;
            }
            advance();
        }

        reportError("Unfinished multiline comment.");
    }

    private final List<String> errors = new ArrayList<>();

    private void reportError(String message) {
        errors.add(
                "[line " + tokenLine +
                ", column " + tokenColumn +
                "] Lexer error: " + message
        );
    }

    boolean hasErrors() {
        return !errors.isEmpty();
    }

    List<String> getErrors() {
        return List.copyOf(errors);
    }
}
