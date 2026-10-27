package mathscript.model;

/**
 * Base común para elementos detectados dentro del código fuente.
 *
 * Además del texto reconocido, conserva dos sistemas de ubicación: línea y
 * columna para mostrarlos a una persona, y posición absoluta (base cero) para
 * aplicar estilos directamente sobre el documento Swing.
 */
public abstract class ElementoLexico {

    private final String lexema;
    private final int linea;
    private final int columna;
    private final int posicionInicial;

    public ElementoLexico(String lexema, int linea, int columna, int posicionInicial) {
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.posicionInicial = posicionInicial;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public int getPosicionInicial() {
        return posicionInicial;
    }

    /** @return cantidad de caracteres ocupados por el lexema en la fuente */
    public int getLongitud() {
        return lexema.length();
    }
}
