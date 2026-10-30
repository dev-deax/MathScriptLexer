package mathscript.model;

import java.util.ArrayList;
import mathscript.lexer.PasoAFD;

/**
 * Resultado compuesto de una ejecución del lexer.
 *
 * Se devuelven juntas las tres vistas de la misma pasada: todos los tokens
 * reconocidos, los errores recuperables y la tabla de símbolos únicos.
 */
public class ResultadoAnalisis {

    private final ArrayList<Token> tokens;
    private final ArrayList<ErrorLexico> errores;
    private final ArrayList<Simbolo> simbolos;
    private final ArrayList<PasoAFD> recorridoAFD;

    public ResultadoAnalisis(ArrayList<Token> tokens,
            ArrayList<ErrorLexico> errores,
            ArrayList<Simbolo> simbolos,
            ArrayList<PasoAFD> recorridoAFD) {
        // Las colecciones proceden de la misma pasada del Lexer. Mantenerlas en
        // un objeto evita ejecutar tres análisis que podrían quedar desfasados.
        this.tokens = tokens;
        this.errores = errores;
        this.simbolos = simbolos;
        this.recorridoAFD = recorridoAFD;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public ArrayList<ErrorLexico> getErrores() {
        return errores;
    }

    public ArrayList<Simbolo> getSimbolos() {
        return simbolos;
    }

    /** @return transiciones recorridas, conservadas en orden de ejecución */
    public ArrayList<PasoAFD> getRecorridoAFD() {
        return recorridoAFD;
    }
}
