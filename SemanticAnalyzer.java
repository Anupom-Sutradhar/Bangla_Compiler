import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SemanticAnalyzer {
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final Deque<Map<String, ValueType>> scopes = new ArrayDeque<>();

    public List<Diagnostic> analyze(Ast.Program program) {
        scopes.clear();
        diagnostics.clear();
        beginScope();
        for (Ast.Stmt stmt : program.statements) analyzeStmt(stmt);
        endScope();
        return diagnostics;
    }

    private void analyzeStmt(Ast.Stmt stmt) {
        if (stmt instanceof Ast.VarDecl s) {
            ValueType initType = analyzeExpr(s.initializer);
            if (!canAssign(s.declaredType, initType)) {
                error(s.name, "Cannot assign " + readable(initType) + " value to " + readable(s.declaredType) + " variable '" + s.name.lexeme + "'.");
            }
            define(s.name, s.declaredType);
        } else if (stmt instanceof Ast.Assign s) {
            ValueType target = resolve(s.name);
            ValueType value = analyzeExpr(s.value);
            if (target != ValueType.ERROR && !canAssign(target, value)) {
                error(s.name, "Cannot assign " + readable(value) + " value to " + readable(target) + " variable '" + s.name.lexeme + "'.");
            }
        } else if (stmt instanceof Ast.Print s) {
            analyzeExpr(s.expression);
        } else if (stmt instanceof Ast.If s) {
            ValueType cond = analyzeExpr(s.condition);
            if (cond != ValueType.BOOL && cond != ValueType.ERROR) {
                error(s.keyword, "IF condition must be boolean. Use a comparison such as x < 10.");
            }
            analyzeBlock(s.thenBranch);
            if (s.elseBranch != null) analyzeBlock(s.elseBranch);
        } else if (stmt instanceof Ast.While s) {
            ValueType cond = analyzeExpr(s.condition);
            if (cond != ValueType.BOOL && cond != ValueType.ERROR) {
                error(s.keyword, "WHILE condition must be boolean. Use a comparison such as x <= 5.");
            }
            analyzeBlock(s.body);
        } else if (stmt instanceof Ast.Block s) {
            analyzeBlock(s);
        }
    }

    private void analyzeBlock(Ast.Block block) {
        beginScope();
        for (Ast.Stmt stmt : block.statements) analyzeStmt(stmt);
        endScope();
    }

    private ValueType analyzeExpr(Ast.Expr expr) {
        if (expr instanceof Ast.Literal e) {
            validateNumericLiteral(e);
            return e.type;
        }
        if (expr instanceof Ast.Variable e) {
            return resolve(e.name);
        }
        if (expr instanceof Ast.Grouping e) {
            return analyzeExpr(e.expression);
        }
        if (expr instanceof Ast.Unary e) {
            ValueType right = analyzeExpr(e.right);
            if (right == ValueType.ERROR) return ValueType.ERROR;
            if (!right.isNumeric()) {
                error(e.operator, "Unary '" + e.operator.lexeme + "' requires a numeric operand.");
                return ValueType.ERROR;
            }
            return right;
        }
        if (expr instanceof Ast.Binary e) {
            ValueType left = analyzeExpr(e.left);
            ValueType right = analyzeExpr(e.right);
            if (left == ValueType.ERROR || right == ValueType.ERROR) return ValueType.ERROR;

            switch (e.operator.type) {
                case PLUS, MINUS, STAR, SLASH, PERCENT -> {
                    if (!left.isNumeric() || !right.isNumeric()) {
                        error(e.operator, "Arithmetic operator '" + e.operator.lexeme + "' requires numeric operands.");
                        return ValueType.ERROR;
                    }
                    if ((e.operator.type == TokenType.SLASH || e.operator.type == TokenType.PERCENT)
                            && isLiteralZero(e.right)) {
                        error(e.operator, "Division or remainder by literal zero is not allowed.");
                        return ValueType.ERROR;
                    }
                    return (left == ValueType.DOUBLE || right == ValueType.DOUBLE)
                            ? ValueType.DOUBLE : ValueType.INT;
                }
                case GREATER, GREATER_EQUAL, LESS, LESS_EQUAL -> {
                    if (!left.isNumeric() || !right.isNumeric()) {
                        error(e.operator, "Comparison operator '" + e.operator.lexeme + "' requires numeric operands.");
                        return ValueType.ERROR;
                    }
                    return ValueType.BOOL;
                }
                case EQUAL_EQUAL, BANG_EQUAL -> {
                    if (!left.isNumeric() || !right.isNumeric()) {
                        error(e.operator, "Equality comparison currently supports numeric operands only.");
                        return ValueType.ERROR;
                    }
                    return ValueType.BOOL;
                }
                default -> {
                    error(e.operator, "Unsupported operator '" + e.operator.lexeme + "'.");
                    return ValueType.ERROR;
                }
            }
        }

        diagnostics.add(new Diagnostic(Diagnostic.Kind.INTERNAL, 0, 0, "Unknown expression node."));
        return ValueType.ERROR;
    }

    private void validateNumericLiteral(Ast.Literal literal) {
        try {
            if (literal.type == ValueType.INT) {
                BigInteger value = new BigInteger(literal.javaText);
                if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
                    error(literal.anchor, "Integer literal is too large for সংখ্যা/int (maximum 2147483647).");
                }
            } else if (literal.type == ValueType.DOUBLE) {
                BigDecimal exact = new BigDecimal(literal.javaText);
                double value = Double.parseDouble(literal.javaText);
                if (!Double.isFinite(value)
                        || (exact.compareTo(BigDecimal.ZERO) != 0 && value == 0.0)) {
                    error(literal.anchor, "Decimal literal is outside the supported দশমিক/double range.");
                }
            }
        } catch (NumberFormatException ex) {
            error(literal.anchor, "Invalid numeric literal '" + literal.javaText + "'.");
        }
    }

    private static boolean isLiteralZero(Ast.Expr expr) {
        if (expr instanceof Ast.Literal lit) {
            try {
                return Double.parseDouble(lit.javaText) == 0.0;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        if (expr instanceof Ast.Grouping g) return isLiteralZero(g.expression);
        return false;
    }

    private boolean canAssign(ValueType target, ValueType value) {
        if (value == ValueType.ERROR) return true; // avoid duplicate cascaded diagnostics
        if (target == value) return true;
        return target == ValueType.DOUBLE && value == ValueType.INT;
    }

    private void define(Token name, ValueType type) {
        Map<String, ValueType> current = scopes.peek();
        if (current.containsKey(name.lexeme)) {
            error(name, "Variable '" + name.lexeme + "' is already declared in this scope.");
            return;
        }
        current.put(name.lexeme, type);
    }

    private ValueType resolve(Token name) {
        for (Map<String, ValueType> scope : scopes) {
            ValueType type = scope.get(name.lexeme);
            if (type != null) return type;
        }
        error(name, "Variable '" + name.lexeme + "' is not declared.");
        return ValueType.ERROR;
    }

    private void beginScope() {
        scopes.push(new LinkedHashMap<>());
    }

    private void endScope() {
        if (!scopes.isEmpty()) scopes.pop();
    }

    private void error(Token token, String message) {
        diagnostics.add(new Diagnostic(Diagnostic.Kind.SEMANTIC, token.line, token.column, message));
    }

    private static String readable(ValueType type) {
        return switch (type) {
            case INT -> "সংখ্যা/int";
            case DOUBLE -> "দশমিক/double";
            case BOOL -> "boolean";
            case ERROR -> "invalid";
        };
    }
}
