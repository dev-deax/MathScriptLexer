package mathscript.lexer;

import mathscript.model.ResultadoAnalisis;

/**
 * Contrato de cualquier componente capaz de convertir código fuente en un
 * resultado léxico. La interfaz desacopla la ventana de la implementación
 * manual concreta de {@link Lexer}.
 */
public interface Analizador {

    /**
     * Analiza una fuente completa sin modificarla.
     *
     * @param texto código fuente que se desea reconocer
     * @return colecciones de tokens, errores y símbolos detectados
     */
    ResultadoAnalisis analizar(String texto);
}
