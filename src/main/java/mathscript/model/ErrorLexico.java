package mathscript.model;

/** Fragmento de la fuente que no satisface ninguna regla léxica válida. */
public class ErrorLexico extends ElementoLexico {

    private final String mensaje;

    public ErrorLexico(String lexema, String mensaje, int linea, int columna, int posicionInicial) {
        super(lexema, linea, columna, posicionInicial);
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {
        return "Línea " + getLinea() + ", columna " + getColumna()
                + ": " + mensaje + " [" + getLexema() + "]";
    }
}
