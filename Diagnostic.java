public final class Diagnostic {
    public enum Kind { LEXER, PARSER, SEMANTIC, IO, INTERNAL }

    public final Kind kind;
    public final int line;
    public final int column;
    public final String message;

    public Diagnostic(Kind kind, int line, int column, String message) {
        this.kind = kind;
        this.line = line;
        this.column = column;
        this.message = message;
    }

    @Override
    public String toString() {
        String where = line > 0 ? " [line " + line + ", col " + column + "]" : "";
        return kind + " ERROR" + where + ": " + message;
    }
}
