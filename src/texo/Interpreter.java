package texo;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

class Interpreter {
    private Environment environment;
    private final PrintStream output;
    private final BufferedReader input;

    Interpreter() {
        this(new Environment(),
                new InputStreamReader(System.in, StandardCharsets.UTF_8),
                System.out);
    }

    Interpreter(PrintStream output) {
        this(new Environment(),
                new InputStreamReader(System.in, StandardCharsets.UTF_8),
                output);
    }

    Interpreter(Reader input, PrintStream output) {
        this(new Environment(), input, output);
    }

    Interpreter(Environment environment, Reader input, PrintStream output) {
        this.environment = environment;
        this.input = new BufferedReader(input);
        this.output = output;
    }

    Object evaluate(Expr expression) {
        if (expression instanceof Expr.Variable variable) {
            return environment.get(variable.name());
        }

        if (expression instanceof  Expr.Call call) {
            return evaluateCall(call);
        }

        if (expression instanceof Expr.Assign assignment) {
            Object value = evaluate(assignment.value());

            environment.assign(assignment.name(), value);

            return value;
        }

        if (expression instanceof Expr.Literal literal) {
            return literal.value();
        }

        if (expression instanceof Expr.Grouping grouping) {
            return evaluate(grouping.expression());
        }

        if (expression instanceof Expr.Unary unary) {
            return evaluateUnary(unary);
        }

        if (expression instanceof Expr.Binary binary) {
            return evaluateBinary(binary);
        }

        throw new IllegalStateException(
                "Unsupported expression: " + expression.getClass().getSimpleName()
        );
    }

    private Object evaluateUnary(Expr.Unary expression) {
        Object right = evaluate(expression.right());
        Token operator = expression.operator();

        return switch (operator.type) {
            case SUBTRACT -> -requireNumber(operator, right);
            case BANG -> !requireBoolean(operator, right);
            default -> throw new IllegalStateException(
                    "Unsupported unary operator: " + operator.text
            );
        };
    }

    private Object evaluateBinary(Expr.Binary expression) {
        Object left = evaluate(expression.left());
        Object right = evaluate(expression.right());
        Token operator = expression.operator();

        return switch (operator.type) {
            case ADD -> requireNumber(operator, left) + requireNumber(operator, right);
            case SUBTRACT -> requireNumber(operator, left) - requireNumber(operator, right);
            case MULTIPLY -> requireNumber(operator, left) * requireNumber(operator, right);
            case DIVIDE -> divide(operator, left, right);
            case GREATER -> requireNumber(operator, left) > requireNumber(operator, right);
            case GREATER_EQUAL -> requireNumber(operator, left) >= requireNumber(operator, right);
            case LESS -> requireNumber(operator, left) < requireNumber(operator, right);
            case LESS_EQUAL -> requireNumber(operator, left) <= requireNumber(operator, right);
            case EQUAL_EQUAL -> Objects.equals(left, right);
            case BANG_EQUAL -> !Objects.equals(left, right);
            default -> throw new IllegalStateException(
                    "Unsupported binary operator: " + operator.text
            );
        };
    }

    private Object evaluateCall(Expr.Call expression) {
        Object callee = evaluate(expression.callee());

        if (!(callee instanceof TexoCallable function)) {
            throw new RuntimeError(expression.closingParenthesis(), "Can only call functions.");
        }

        List<Object> arguments = new ArrayList<>();

        for (Expr argument : expression.arguments()) {
            arguments.add(evaluate(argument));
        }

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(
                    expression.closingParenthesis(),
                    "Expected " + function.arity() + " arguments but received " + arguments.size() + "."
            );
        }
        return function.call(this, arguments);
    }

    private double divide(Token operator, Object left, Object right) {
        double divisor = requireNumber(operator, right);

        if (divisor == 0.0) {
            throw new RuntimeError(
                    operator, "Cannot divide by zero."
            );
        }

        return requireNumber(operator, left) / divisor;
    }

    private double requireNumber(Token operator, Object value) {
        if (value instanceof Double number) {
            return number;
        }

        throw new RuntimeError(
                operator, "Operator '" + operator.text + "' requires numbers."
        );
    }

    private boolean requireBoolean (Token operator, Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }

        throw new RuntimeError(
                operator, "Operator '" + operator.text + "' requires a boolean."
        );
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (ReturnSignal returned) {
            throw new RuntimeError(returned.keyword, "'out' can only be used inside a function.");
        }
    }

    private void execute(Stmt statement) {
        if (statement instanceof Stmt.Expression expression) {
            evaluate(expression.expression());
            return;
        }

        if (statement instanceof Stmt.Function function) {
            TexoFunction callable = new TexoFunction(function, environment);
            environment.assign(function.name(), callable);
            return;
        }

        if (statement instanceof Stmt.Return returned) {
            Object value = evaluate(returned.value());

            throw new ReturnSignal(returned.keyword(), value);
        }

        if (statement instanceof Stmt.If conditional) {
            boolean condition = requireCondition(evaluate(conditional.condition()));

            if (condition) {
                executeBlock(conditional.thenBranch());
            } else {
                executeBlock(conditional.elseBranch());
            }

            return;
        }

        if (statement instanceof Stmt.While loop) {
            while (requireCondition(evaluate(loop.condition()))) {
                executeBlock(loop.body());
            }

            return;
        }

        if (statement instanceof Stmt.Input inputStatement) {
            readInput(inputStatement.name());
            return;
        }

        if (statement instanceof Stmt.Print print) {
            Object value = evaluate(print.expression());
            output.println(stringify(value));
            return;
        }

        throw new IllegalStateException("Unsupported statement: " + statement.getClass().getSimpleName());
    }

    private String stringify(Object value) {
        if (value instanceof Double number && number == Math.rint(number)) {
            return Long.toString(number.longValue());
        }

        return String.valueOf(value);
    }

    void executeBlock(List<Stmt> statements) {
        for (Stmt statement : statements) {
            execute(statement);
        }
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;

        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private boolean requireCondition(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }

        throw new RuntimeError("Conditions must evaluate to a boolean.");
    }

    private void readInput(Token name) {
        try {
            String line = input.readLine();

            if (line == null) {
                throw new RuntimeError(
                        name, "No input available for '" + name.text + "'."
                );
            }

            environment.assign(name, parseInputValue(line));
        } catch (IOException exception) {
            throw new RuntimeError(
                    name, "Could not read input for '" + name.text + "'."
            );
        }
    }

    private Object parseInputValue(String line) {
        String trimmed = line.trim();

        if (trimmed.equals("true")) {
            return true;
        }

        if (trimmed.equals("false")) {
            return false;
        }

        try {
            return Double.parseDouble(trimmed);
        } catch (NumberFormatException ignored) {
            return line;
        }
    }

}
