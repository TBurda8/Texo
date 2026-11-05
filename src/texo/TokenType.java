package texo;

public enum TokenType {
    // Punctuation
    LEFT_PAREN, RIGHT_PAREN, COMMA, SEMICOLON,

    // One or two char operators
    ASSIGN, EQUAL_EQUAL, GREATER, LESS, GREATER_EQUAL,
    LESS_EQUAL, BANG, BANG_EQUAL,

    // Keywords
    IF, THEN, ELSE, WHILE, DO, START, IN, OUT, END, PRINT,
    PUT, TRUE, FALSE,

    // Literals
    IDENTIFIER, NUMBER, STRING,

    // Arithmetic Operators
    ADD, SUBTRACT, MULTIPLY, DIVIDE,

    // End of File
    EOF
}
