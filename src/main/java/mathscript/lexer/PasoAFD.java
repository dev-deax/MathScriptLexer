package mathscript.lexer;

/**
 * Transición inmutable recorrida por el AFD durante el análisis de la fuente.
 *
 * La posición permite relacionar el paso con el carácter que provocó la
 * transición. Cuando la condición es el fin de la entrada, la posición puede
 * coincidir con la longitud total del texto.
 */
public final class PasoAFD {

    /** Estado antes de examinar el carácter o condición indicada. */
    private final Estado origen;
    /** Estado alcanzado; el panel lo resalta junto con el origen. */
    private final Estado destino;
    /** Explicación legible de la entrada que provocó el paso. */
    private final String condicion;
    /** Índice absoluto, desde cero, para relacionar el paso con el editor. */
    private final int posicionInicial;

    public PasoAFD(Estado origen, Estado destino,
            String condicion, int posicionInicial) {
        this.origen = origen;
        this.destino = destino;
        this.condicion = condicion;
        this.posicionInicial = posicionInicial;
    }

    public Estado getOrigen() {
        return origen;
    }

    public Estado getDestino() {
        return destino;
    }

    public String getCondicion() {
        return condicion;
    }

    public int getPosicionInicial() {
        return posicionInicial;
    }
}
