package texo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private RunResult run(String source) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        int exitCode = Main.run(source, new StringReader(""), new PrintStream(output), new PrintStream(errors));

        return new RunResult(
                exitCode,
                output.toString(StandardCharsets.UTF_8).replace("\r\n", "\n"),
                errors.toString(StandardCharsets.UTF_8).replace("\r\n", "\n")
        );
    }


    private record RunResult(
            int exitCode,
            String output,
            String errors
    ) {
    }

    @Test
    void runsProgram() {
        RunResult result = run("""
                start square in(number)
                    out number * number;
                end
                
                print square(7);
                """);

        assertEquals(0, result.exitCode());
        assertEquals("49\n", result.output());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void reportsSyntaxError() {
        RunResult result = run("""
                print 10
                """);

        assertEquals(65, result.exitCode());
        assertTrue(result.output().isEmpty());
        assertTrue(result.errors().contains("Parser error"));
    }

    @Test
    void reportsLexerError() {
        RunResult result = run("""
                print @;
                """);

        assertEquals(65, result.exitCode());
        assertTrue(result.errors().contains("Lexer error"));
    }

    @Test
    void reportsRuntimeError() {
        RunResult result = run("""
                print missing;
                """);

        assertEquals(70, result.exitCode());
        assertTrue(
                result.errors().contains(
                        "Undefined variable 'missing'."
                )
        );
    }
}