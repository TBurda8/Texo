package texo;

public class Token {
    final TokenType type;
    final String text;
    final Object literal;
    final int line;
    final int col;

    Token(TokenType type, String text, Object literal, int line, int col) {
        this.type = type;
        this.text = text;
        this.literal = literal;
        this.line = line;
        this.col = col;
    }

    @Override
    public String toString() {
        return type + " '" + text + "'"
                + (literal == null ? "" : " " + literal)
                + " at " + line + ":" + col;
    }
}
