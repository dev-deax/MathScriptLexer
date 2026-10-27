package mathscript.util;

import java.awt.Color;
import mathscript.model.TipoToken;

/**
 * Traduce un TipoToken al color visual del editor oscuro.
 * El enum conserva únicamente el significado léxico y no conoce la interfaz.
 */
public final class SyntaxStyle {

    private SyntaxStyle() {
    }

    public static Color getColor(TipoToken tipo) {
        // Esta es la unión entre la lógica léxica y la presentación. El Lexer
        // solo produce categorías; esta clase decide cómo se ve cada una.
        switch (tipo) {
            case TOKEN_VAR:
            case TOKEN_PRINT:
            case TOKEN_IF:
            case TOKEN_ELSE:
                // Todas las palabras reservadas comparten el mismo color.
                return Style.COLOR_RESERVADA;

            case TOKEN_INT:
            case TOKEN_FLOAT:
                // Enteros y decimales pertenecen a la familia visual de números.
                return Style.COLOR_NUMERO;

            case TOKEN_STRING:
                return Style.COLOR_CADENA;

            case TOKEN_OP:
                return Style.COLOR_OPERADOR;

            case TOKEN_DELIM:
                return Style.COLOR_DELIMITADOR;

            case TOKEN_COMMENT:
                return Style.COLOR_COMENTARIO;

            case TOKEN_WHITESPACE:
                // El espacio conserva el color normal del documento.
                return Style.COLOR_TEXTO;

            case TOKEN_ID:
            default:
                return Style.COLOR_IDENTIFICADOR;
        }
    }
}
