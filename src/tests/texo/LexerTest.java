package texo;

import org.junit.jupiter.api.Test;
import texo.Lexer;
import texo.Token;
import texo.TokenType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static texo.TokenType.*;

class LexerTest {
    private Lexer createLexer(String source) {
        return new Lexer(source);
    }

    private List<Token> scan(String source) {
        return createLexer(source).scanTokens();
    }

    @Test
    void scansOperatorsAndPunctuation() {
        List<Token> tokens = scan(
                "( ) , ; + - * / = == ! != > >= < <="
        );

        List<TokenType> types = tokens.stream().map(token -> token.type).toList();

        assertEquals(List.of(
                LEFT_PAREN,
                RIGHT_PAREN,
                COMMA,
                SEMICOLON,
                ADD,
                SUBTRACT,
                MULTIPLY,
                DIVIDE,
                ASSIGN,
                EQUAL_EQUAL,
                BANG,
                BANG_EQUAL,
                GREATER,
                GREATER_EQUAL,
                LESS,
                LESS_EQUAL,
                EOF
        ), types);
    }

    @Test
    void scansNumbers() {
        List<Token> tokens = scan("10 3.14 0.5");

        assertEquals(NUMBER, tokens.get(0).type);
        assertEquals(10.0, tokens.get(0).literal);

        assertEquals(NUMBER, tokens.get(1).type);
        assertEquals(3.14, tokens.get(1).literal);

        assertEquals(NUMBER, tokens.get(2).type);
        assertEquals(0.5, tokens.get(2).literal);
    }

    @Test
    void sepKeyWordsFromIdentifiers() {
        List<Token> tokens = scan(
                "start in out end print if then else while do put " +
                        "true false starter printer"
        );

        List<TokenType> types = tokens.stream().map(token -> token.type).toList();

        assertEquals(List.of(
                START,
                IN,
                OUT,
                END,
                PRINT,
                IF,
                THEN,
                ELSE,
                WHILE,
                DO,
                PUT,
                TRUE,
                FALSE,
                IDENTIFIER,
                IDENTIFIER,
                EOF
        ), types);
    }

    @Test
    void ignoresComments() {
        String source = """
                print 1;
                # Ignore this line
                #/
                Ignore all of this.
                /#
                print 2;
                """;

        List<Token> tokens = scan(source);

        long printTokens = tokens.stream().filter(token -> token.type == PRINT).count();

        assertEquals(2, printTokens);
    }

    @Test
    void tracksTokens() {
        List<Token> tokens = scan("""
                print 1;
                    print 2;""");

        Token firstPrint = tokens.get(0);
        assertEquals(1, firstPrint.line);
        assertEquals(1, firstPrint.col);

        Token secondPrint = tokens.get(3);
        assertEquals(2, secondPrint.line);
        assertEquals(5, secondPrint.col);

        Token eof = tokens.get(tokens.size() - 1);
        assertEquals(2, eof.line);
        assertEquals(13, eof.col);
    }

    @Test
    void reportsUnChars() {
        Lexer lexer = createLexer("@");

        lexer.scanTokens();
        assertTrue(lexer.hasErrors());
        assertTrue(lexer.getErrors().get(0).contains("Unexpected character"));
    }

    @Test
    void reportsUnString() {
        Lexer lexer = createLexer("\"unfinished");

        lexer.scanTokens();

        assertTrue(lexer.hasErrors());
        assertTrue(
                lexer.getErrors().get(0).contains("Unterminated string")
        );
    }

    @Test
    void reportsUnMultiComment() {
        Lexer lexer = createLexer("#/ never closed");

        lexer.scanTokens();

        assertTrue(lexer.hasErrors());
        assertTrue(
                lexer.getErrors().get(0).contains("Unterminated multiline comment")
        );
    }

    @Test
    void oneEOF() {
        List<Token> tokens = scan("print 10;");

        long eofCount = tokens.stream().filter(token -> token.type == EOF).count();

        assertEquals(1, eofCount);
        assertEquals(EOF, tokens.get(tokens.size() - 1).type);
    }
}
