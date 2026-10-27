package mathscript.model;

/**
 * Entrada única de la tabla de símbolos.
 * Registra una palabra reservada o identificador solamente la primera vez que
 * aparece, a diferencia de la lista de tokens, que conserva cada ocurrencia.
 */
public class Simbolo {

    private final String lexema;
    private final String tipo;
    private final int lineaPrimeraAparicion;

    public Simbolo(String lexema, String tipo, int lineaPrimeraAparicion) {
        this.lexema = lexema;
        this.tipo = tipo;
        this.lineaPrimeraAparicion = lineaPrimeraAparicion;
    }

    public String getLexema() {
        return lexema;
    }

    public String getTipo() {
        return tipo;
    }

    public int getLineaPrimeraAparicion() {
        return lineaPrimeraAparicion;
    }
}
