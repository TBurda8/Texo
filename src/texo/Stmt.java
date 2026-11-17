package texo;

import java.util.List;

sealed interface Stmt permits
        Stmt.Expression,
        Stmt.Function,
        Stmt.If,
        Stmt.Input,
        Stmt.Print,
        Stmt.Return,
        Stmt.While {

    record Expression(
            Expr expression
    ) implements Stmt {

    }

    record Function(
            Token name,
            List<Token> parameters,
            List<Stmt> body
    ) implements Stmt {
        public Function {
            parameters = List.copyOf(parameters);
            body = List.copyOf(body);
        }
    }

    record If(
            Expr condition,
            List<Stmt> thenBranch,
            List<Stmt> elseBranch
    ) implements Stmt {
        public If {
            thenBranch = List.copyOf(thenBranch);
            elseBranch = List.copyOf(elseBranch);
        }
    }

    record Input(
            Token name
    ) implements Stmt {
    }

    record Print(
            Expr expression
    ) implements Stmt {
    }

    record Return(
            Token keyword,
            Expr value
    ) implements Stmt {
    }

    record While(
            Expr condition,
            List<Stmt> body
    ) implements Stmt {
        public While {
            body = List.copyOf(body);
        }
    }
}

