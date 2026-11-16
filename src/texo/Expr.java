package texo;

import java.util.List;

sealed interface Expr permits
        Expr.Assign,
        Expr.Binary,
        Expr.Call,
        Expr.Grouping,
        Expr.Literal,
        Expr.Unary,
        Expr.Variable {

    record Assign(
            Token name,
            Expr value
    ) implements  Expr {
    }

    record Binary(
            Expr left,
            Token operator,
            Expr right
    ) implements Expr {
    }

    record Call(
            Expr callee,
            Token closingParenthesis,
            List<Expr> arguements
    ) implements Expr {
        public Call {
            arguements = List.copyOf(arguements);
        }
    }

    record Grouping(
            Expr expression
    ) implements Expr {
    }

    record Literal(
            Object value
    ) implements Expr {
    }

    record Unary(
            Token operator,
            Expr right
    ) implements Expr {
    }

    record Variable(
            Token name
    ) implements Expr {
    }
}
