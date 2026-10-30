package mathscript.lexer;

/**
 * Transición inmutable recorrida por el AFD durante el análisis de la fuente.
 *
 * La posición permite relacionar el paso con el carácter que provocó la
 * transición. Cuando la condición es el fin de la entrada, la posición puede
 * coincidir con la longitud total del texto.
 */
public final class PasoAFD {

    private final Estado origen;
    private final Estado destino;
    private final String condicion;
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
