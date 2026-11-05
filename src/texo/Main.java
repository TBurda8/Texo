package texo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    private static final int EXIT_USAGE_ERROR = 64;
    private static final int EXIT_FILE_ERROR = 66;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: texo <scrpit.texo>");
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
            run(source);
        } catch (IOException e) {
            System.err.println("Could not read file : " + e.getMessage());
            System.exit(EXIT_FILE_ERROR);
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        if (lexer.hasErrors()) {
            lexer.getErrors().forEach(System.err::println);
            return;
        }
        tokens.forEach(System.out::println);
    }
}
