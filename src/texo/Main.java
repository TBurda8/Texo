package texo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

public class Main {
    private static final int EXIT_USAGE_ERROR = 64;
    private static final int EXIT_FILE_ERROR = 66;
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_SYNTAX_ERROR = 65;
    private static final int EXIT_RUNTIME_ERROR = 70;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: texo <script.texo>");
            System.exit(EXIT_USAGE_ERROR);
            return;
        }

        Path scriptPath = Path.of(args[0]);

        if (!scriptPath.getFileName().toString().endsWith(".texo")) {
            System.err.println("Texo source files must use .texo ending.");
            System.exit(EXIT_USAGE_ERROR);
            return;
        }

        if (!Files.isRegularFile(scriptPath)) {
            System.err.println("File not found: " + scriptPath);
            System.exit(EXIT_FILE_ERROR);
            return;
        }

        try {
            String source = Files.readString(scriptPath, StandardCharsets.UTF_8);
            int exitCode = run(source, new InputStreamReader(System.in, StandardCharsets.UTF_8),
                    System.out, System.err);
            if (exitCode != EXIT_SUCCESS) {
                System.exit(exitCode);
            }
        } catch (IOException e) {
            System.err.println("Could not read file : " + e.getMessage());
            System.exit(EXIT_FILE_ERROR);
        }
    }

    static int run(String source, Reader input, PrintStream output, PrintStream errorOutput) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        if (lexer.hasErrors()) {
            lexer.getErrors().forEach(errorOutput::println);
            return EXIT_SYNTAX_ERROR;
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (parser.hasErrors()) {
            parser.getErrors().forEach(errorOutput::println);
            return EXIT_SYNTAX_ERROR;
        }

        Interpreter interpreter = new Interpreter(input, output);

        try {
            interpreter.interpret(statements);
            return EXIT_SUCCESS;
        } catch (RuntimeError error) {
            reportRuntimeError(error, errorOutput);
            return EXIT_RUNTIME_ERROR;
        }
    }

    private static void reportRuntimeError(RuntimeError error, PrintStream out) {
        if (error.token == null) {
            out.println("Runtime error: " + error.getMessage());
            return;
        }

        out.println("[line " + error.token.line + ", column " + error.token.col +
                "] Runtime error at '" + error.token.text + "': " + error.getMessage());
    }
}
