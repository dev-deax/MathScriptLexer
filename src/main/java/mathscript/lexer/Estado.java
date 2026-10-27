package mathscript.lexer;

/**
 * Estados conceptuales del autómata finito determinista (AFD).
 *
 * Cada reconocedor del {@link Lexer} parte de uno de estos estados y cambia
 * de estado cuando encuentra una transición relevante: por ejemplo, un número
 * pasa de ENTERO a PUNTO_DECIMAL y finalmente a DECIMAL. ERROR representa una
 * transición inválida de la que el lexer se recupera antes de seguir leyendo.
 */
public enum Estado {
    INICIO,
    IDENTIFICADOR,
    ENTERO,
    PUNTO_DECIMAL,
    DECIMAL,
    CADENA,
    CADENA_CERRADA,
    OPERADOR,
    DELIMITADOR,
    COMENTARIO,
    ERROR
}
