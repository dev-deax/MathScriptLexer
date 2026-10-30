package mathscript.lexer;

/**
 * Estados formales del AFD léxico de MathScript.
 *
 * <p>Las letras A-AA coinciden con la documentación del autómata. Los estados
 * de prefijo permiten que el recorrido distinga una palabra reservada exacta
 * de un identificador que solamente comienza igual, por ejemplo {@code if}
 * frente a {@code ifx}.</p>
 */
public enum Estado {
    INICIO("A", "INICIO", false),
    ESPACIO("B", "ESPACIO", true),
    CADENA("C", "CADENA", false),
    DELIMITADOR("D", "DELIMITADOR", true),
    ERROR("E", "ERROR", false),
    OPERADOR("F", "OPERADOR", true),
    BARRA("G", "/ · OP", true),
    ENTERO("H", "ENTERO", true),
    IDENTIFICADOR("I", "ID", true),
    PREFIJO_E("J", "e · ID", true),
    PREFIJO_I("K", "i · ID", true),
    PREFIJO_P("L", "p · ID", true),
    PREFIJO_V("M", "v · ID", true),
    CADENA_CERRADA("N", "STRING", true),
    COMENTARIO("O", "COMENTARIO", true),
    PUNTO_DECIMAL("P", "PUNTO DECIMAL", false),
    PREFIJO_EL("Q", "el · ID", true),
    PALABRA_IF("R", "if · IF", true),
    PREFIJO_PR("S", "pr · ID", true),
    PREFIJO_VA("T", "va · ID", true),
    DECIMAL("U", "DECIMAL", true),
    PREFIJO_ELS("V", "els · ID", true),
    PREFIJO_PRI("W", "pri · ID", true),
    PALABRA_VAR("X", "var · VAR", true),
    PALABRA_ELSE("Y", "else · ELSE", true),
    PREFIJO_PRIN("Z", "prin · ID", true),
    PALABRA_PRINT("AA", "print · PRINT", true);

    /** Identificador corto utilizado para comparar el grafo con el PDF. */
    private final String codigo;
    /** Texto visible; no interviene en la clasificación de los tokens. */
    private final String etiqueta;
    /** Un lexema puede finalizar aquí; no significa que toda la fuente sea válida. */
    private final boolean aceptacion;

    Estado(String codigo, String etiqueta, boolean aceptacion) {
        this.codigo = codigo;
        this.etiqueta = etiqueta;
        this.aceptacion = aceptacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public boolean esAceptacion() {
        return aceptacion;
    }
}
