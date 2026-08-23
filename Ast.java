import java.util.List;

public final class Ast {
    private Ast() {}

    public static final class Program {
        public final List<Stmt> statements;
        public Program(List<Stmt> statements) { this.statements = statements; }
    }

    public abstract static class Stmt {}

    public static final class VarDecl extends Stmt {
        public final ValueType declaredType;
        public final Token name;
        public final Expr initializer;
        public VarDecl(ValueType declaredType, Token name, Expr initializer) {
            this.declaredType = declaredType;
            this.name = name;
            this.initializer = initializer;
        }
    }

    public static final class Assign extends Stmt {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    public static final class Print extends Stmt {
        public final Token keyword;
        public final Expr expression;
        public Print(Token keyword, Expr expression) {
            this.keyword = keyword;
            this.expression = expression;
        }
    }

    public static final class If extends Stmt {
        public final Token keyword;
        public final Expr condition;
        public final Block thenBranch;
        public final Block elseBranch;
        public If(Token keyword, Expr condition, Block thenBranch, Block elseBranch) {
            this.keyword = keyword;
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    public static final class While extends Stmt {
        public final Token keyword;
        public final Expr condition;
        public final Block body;
        public While(Token keyword, Expr condition, Block body) {
            this.keyword = keyword;
            this.condition = condition;
            this.body = body;
        }
    }

    public static final class Block extends Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) { this.statements = statements; }
    }

    public abstract static class Expr {
        public final Token anchor;
        protected Expr(Token anchor) { this.anchor = anchor; }
    }

    public static final class Literal extends Expr {
        public final String javaText;
        public final ValueType type;
        public Literal(Token token, String javaText, ValueType type) {
            super(token);
            this.javaText = javaText;
            this.type = type;
        }
    }

    public static final class Variable extends Expr {
        public final Token name;
        public Variable(Token name) {
            super(name);
            this.name = name;
        }
    }

    public static final class Grouping extends Expr {
        public final Expr expression;
        public Grouping(Token leftParen, Expr expression) {
            super(leftParen);
            this.expression = expression;
        }
    }

    public static final class Unary extends Expr {
        public final Token operator;
        public final Expr right;
        public Unary(Token operator, Expr right) {
            super(operator);
            this.operator = operator;
            this.right = right;
        }
    }

    public static final class Binary extends Expr {
        public final Expr left;
        public final Token operator;
        public final Expr right;
        public Binary(Expr left, Token operator, Expr right) {
            super(operator);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }
}
