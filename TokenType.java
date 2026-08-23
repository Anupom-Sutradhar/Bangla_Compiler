public enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    SEMICOLON,
    PLUS, MINUS, STAR, SLASH, PERCENT,

    // One or two character tokens
    ASSIGN,
    EQUAL_EQUAL, BANG_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, NUMBER,

    // Bangla keywords
    KW_INT,       // সংখ্যা
    KW_DOUBLE,    // দশমিক
    KW_IF,        // যদি
    KW_ELSE,      // নাহলে
    KW_WHILE,     // যতক্ষণ
    KW_PRINT,     // দেখাও

    EOF
}
