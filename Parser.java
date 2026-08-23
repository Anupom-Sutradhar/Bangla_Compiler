import java.util.ArrayList;
import java.util.List;

public final class Parser {
    private static final class ParseError extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final List<Token> tokens;
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Ast.Program parse() {
        List<Ast.Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            Ast.Stmt stmt = declarationOrStatementRecovering();
            if (stmt != null) statements.add(stmt);
        }
        return new Ast.Program(statements);
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    private Ast.Stmt declarationOrStatementRecovering() {
        try {
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }
    }

    private Ast.Stmt statement() {
        if (match(TokenType.KW_INT)) return variableDeclaration(ValueType.INT);
        if (match(TokenType.KW_DOUBLE)) return variableDeclaration(ValueType.DOUBLE);
        if (match(TokenType.KW_PRINT)) return printStatement(previous());
        if (match(TokenType.KW_IF)) return ifStatement(previous());
        if (match(TokenType.KW_WHILE)) return whileStatement(previous());
        if (check(TokenType.LEFT_BRACE)) return block();

        if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) {
            return assignmentStatement();
        }

        throw error(peek(), "Expected a statement. Start with সংখ্যা, দশমিক, দেখাও, যদি, যতক্ষণ, or an assignment.");
    }

    private Ast.Stmt variableDeclaration(ValueType type) {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name after data type.");
        consume(TokenType.ASSIGN, "Expected '=' after variable name.");
        Ast.Expr initializer = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
        return new Ast.VarDecl(type, name, initializer);
    }

    private Ast.Stmt assignmentStatement() {
        Token name = consume(TokenType.IDENTIFIER, "Expected variable name.");
        consume(TokenType.ASSIGN, "Expected '=' in assignment.");
        Ast.Expr value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after assignment.");
        return new Ast.Assign(name, value);
    }

    private Ast.Stmt printStatement(Token keyword) {
        consume(TokenType.LEFT_PAREN, "Expected '(' after দেখাও.");
        Ast.Expr expr = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
        consume(TokenType.SEMICOLON, "Expected ';' after দেখাও(...).");
        return new Ast.Print(keyword, expr);
    }

    private Ast.Stmt ifStatement(Token keyword) {
        consume(TokenType.LEFT_PAREN, "Expected '(' after যদি.");
        Ast.Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after IF condition.");
        Ast.Block thenBranch = block();
        Ast.Block elseBranch = null;
        if (match(TokenType.KW_ELSE)) {
            elseBranch = block();
        }
        return new Ast.If(keyword, condition, thenBranch, elseBranch);
    }

    private Ast.Stmt whileStatement(Token keyword) {
        consume(TokenType.LEFT_PAREN, "Expected '(' after যতক্ষণ.");
        Ast.Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expected ')' after WHILE condition.");
        Ast.Block body = block();
        return new Ast.While(keyword, condition, body);
    }

    private Ast.Block block() {
        consume(TokenType.LEFT_BRACE, "Expected '{' to start block.");
        List<Ast.Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            Ast.Stmt stmt = declarationOrStatementRecovering();
            if (stmt != null) statements.add(stmt);
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after block.");
        return new Ast.Block(statements);
    }

    // expression -> equality
    private Ast.Expr expression() {
        return equality();
    }

    // equality -> comparison ((== | !=) comparison)*
    private Ast.Expr equality() {
        Ast.Expr expr = comparison();
        while (match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
            Token op = previous();
            Ast.Expr right = comparison();
            expr = new Ast.Binary(expr, op, right);
        }
        return expr;
    }

    // comparison -> term ((> | >= | < | <=) term)*
    private Ast.Expr comparison() {
        Ast.Expr expr = term();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                     TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token op = previous();
            Ast.Expr right = term();
            expr = new Ast.Binary(expr, op, right);
        }
        return expr;
    }

    // term -> factor ((+ | -) factor)*
    private Ast.Expr term() {
        Ast.Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            Ast.Expr right = factor();
            expr = new Ast.Binary(expr, op, right);
        }
        return expr;
    }

    // factor -> unary ((* | / | %) unary)*
    private Ast.Expr factor() {
        Ast.Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token op = previous();
            Ast.Expr right = unary();
            expr = new Ast.Binary(expr, op, right);
        }
        return expr;
    }

    // unary -> (+ | -) unary | primary
    private Ast.Expr unary() {
        if (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            return new Ast.Unary(op, unary());
        }
        return primary();
    }

    private Ast.Expr primary() {
        if (match(TokenType.NUMBER)) {
            Token token = previous();
            ValueType type = token.lexeme.contains(".") ? ValueType.DOUBLE : ValueType.INT;
            return new Ast.Literal(token, token.lexeme, type);
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Ast.Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Token left = previous();
            Ast.Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.");
            return new Ast.Grouping(left, expr);
        }

        throw error(peek(), "Expected a number, variable, or parenthesized expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().type == type;
    }

    private boolean checkNext(TokenType type) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(Math.max(0, current - 1));
    }

    private ParseError error(Token token, String message) {
        diagnostics.add(new Diagnostic(Diagnostic.Kind.PARSER, token.line, token.column, message));
        return new ParseError();
    }

    // Basic syntax recovery: skip to semicolon or end of the current source line.
    private void synchronize() {
        if (isAtEnd()) return;

        // If the parser noticed a missing token exactly when the next source line
        // begins (for example a missing semicolon), do not discard that next line.
        if (current > 0 && peek().line > previous().line) return;

        int errorLine = peek().line;

        // Otherwise make progress so malformed input cannot cause an infinite loop.
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;
            if (peek().line > errorLine) return;
            if (peek().type == TokenType.RIGHT_BRACE) return;
            advance();
        }
    }
}
