import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Lexer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("সংখ্যা", TokenType.KW_INT);
        KEYWORDS.put("দশমিক", TokenType.KW_DOUBLE);
        KEYWORDS.put("যদি", TokenType.KW_IF);
        KEYWORDS.put("নাহলে", TokenType.KW_ELSE);
        KEYWORDS.put("যতক্ষণ", TokenType.KW_WHILE);
        KEYWORDS.put("দেখাও", TokenType.KW_PRINT);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int startLine = 1;
    private int startColumn = 1;

    public Lexer(String source) {
        this.source = source == null ? "" : source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            startLine = line;
            startColumn = column;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> add(TokenType.LEFT_PAREN);
            case ')' -> add(TokenType.RIGHT_PAREN);
            case '{' -> add(TokenType.LEFT_BRACE);
            case '}' -> add(TokenType.RIGHT_BRACE);
            case ';' -> add(TokenType.SEMICOLON);
            case '+' -> add(TokenType.PLUS);
            case '-' -> add(TokenType.MINUS);
            case '*' -> add(TokenType.STAR);
            case '%' -> add(TokenType.PERCENT);
            case '=' -> add(match('=') ? TokenType.EQUAL_EQUAL : TokenType.ASSIGN);
            case '!' -> {
                if (match('=')) add(TokenType.BANG_EQUAL);
                else error("Unexpected '!'. Use '!=' for not-equal.");
            }
            case '>' -> add(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '<' -> add(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    add(TokenType.SLASH);
                }
            }
            case '#' -> {
                while (peek() != '\n' && !isAtEnd()) advance();
            }
            case ' ', '\r', '\t' -> { /* ignore */ }
            case '\n' -> { /* line/column already updated by advance() */ }
            default -> {
                if (isDigit(c)) number();
                else if (isIdentifierStart(c)) identifier();
                else error("Unexpected character: '" + c + "'.");
            }
        }
    }

    private void identifier() {
        while (isIdentifierPart(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        add(type, text);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        String original = source.substring(start, current);
        add(TokenType.NUMBER, normalizeDigits(original));
    }

    private static String normalizeDigits(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '.') {
                sb.append(ch);
            } else {
                int d = Character.digit(ch, 10);
                sb.append(d >= 0 ? (char)('0' + d) : ch);
            }
        }
        return sb.toString();
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        char c = source.charAt(current++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        advance();
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length() ? '\0' : source.charAt(current + 1);
    }

    private static boolean isDigit(char c) {
        return Character.digit(c, 10) >= 0;
    }

    private static boolean isIdentifierStart(char c) {
        return c == '_' || Character.isLetter(c);
    }

    private static boolean isIdentifierPart(char c) {
        int type = Character.getType(c);
        return isIdentifierStart(c)
                || Character.isDigit(c)
                || type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK;
    }

    private void add(TokenType type) {
        add(type, source.substring(start, current));
    }

    private void add(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, startLine, startColumn));
    }

    private void error(String message) {
        diagnostics.add(new Diagnostic(Diagnostic.Kind.LEXER, startLine, startColumn, message));
    }
}
