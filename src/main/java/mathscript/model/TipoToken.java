package mathscript.model;

/** Categorías léxicas que el lenguaje MathScript puede producir. */
public enum TipoToken {
    TOKEN_VAR,
    TOKEN_PRINT,
    TOKEN_IF,
    TOKEN_ELSE,
    TOKEN_ID,
    TOKEN_INT,
    TOKEN_FLOAT,
    TOKEN_STRING,
    TOKEN_OP,
    TOKEN_DELIM,
    TOKEN_COMMENT,
    TOKEN_WHITESPACE
}
