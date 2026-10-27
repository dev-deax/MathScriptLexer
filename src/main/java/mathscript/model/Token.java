package mathscript.model;

/**
 * Unidad reconocida correctamente por el lexer.
 * Un token reúne el lexema original, su categoría y su ubicación en la fuente.
 */
public class Token extends ElementoLexico {

    private final TipoToken tipo;

    public Token(String lexema, TipoToken tipo, int linea, int columna, int posicionInicial) {
        super(lexema, linea, columna, posicionInicial);
        this.tipo = tipo;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    /**
     * Indica si el token es útil para conservar posiciones o resaltar sintaxis,
     * pero no forma parte de la tabla formal que se presenta al usuario.
     */
    public boolean esIgnorable() {
        return tipo == TipoToken.TOKEN_COMMENT || tipo == TipoToken.TOKEN_WHITESPACE;
    }

    @Override
    public String toString() {
        return getLexema() + " -> " + tipo + " (" + getLinea() + ":" + getColumna() + ")";
    }
}
