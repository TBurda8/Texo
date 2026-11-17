package texo;

import java.util.Objects;

class Interpreter {

    Object evaluate(Expr expression) {
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

}
