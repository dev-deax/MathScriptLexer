package mathscript.lexer;

import java.util.ArrayList;
import mathscript.model.ErrorLexico;
import mathscript.model.ResultadoAnalisis;
import mathscript.model.Simbolo;
import mathscript.model.TipoToken;
import mathscript.model.Token;

/**
 * Analizador léxico manual de MathScript.
 *
 * No utiliza Pattern, Matcher, String.matches(), Flex, Lex ni motores regex.
 * El texto se recorre carácter por carácter siguiendo la lógica del AFD.
 */
public class Lexer implements Analizador {

    /** Código fuente completo que se está recorriendo. */
    private String texto;

    /** Índice absoluto del siguiente carácter pendiente de analizar. */
    private int posicion;

    /** Línea y columna asociadas a {@link #posicion}; ambas comienzan en 1. */
    private int linea;
    private int columna;

    /* Salidas que se construyen durante una ejecución de analizar(). */
    private ArrayList<Token> tokens;
    private ArrayList<ErrorLexico> errores;
    private ArrayList<Simbolo> simbolos;
    private ArrayList<PasoAFD> recorridoAFD;

    /**
     * Recorre el código fuente de izquierda a derecha y clasifica cada carácter.
     *
     * <p>Este método es el estado inicial o despachador del AFD. No consume el
     * carácter directamente: selecciona el reconocedor adecuado y ese método
     * avanza el cursor hasta dejarlo en el primer carácter del siguiente
     * lexema. Por eso, cada vuelta del ciclo reconoce exactamente un token,
     * un bloque ignorable o un error.</p>
     *
     * <p>El orden de las condiciones también expresa prioridad. Por ejemplo,
     * {@code //} debe reconocerse como comentario antes de tratar {@code /}
     * como un operador individual.</p>
     *
     * @param texto código fuente de MathScript; {@code null} se interpreta
     * como una entrada vacía
     * @return tokens, errores y símbolos encontrados en una sola pasada
     */
    @Override
    public ResultadoAnalisis analizar(String texto) {
        // Antes de leer la fuente se coloca el AFD en su estado conceptual
        // inicial: cursor al comienzo y colecciones de salida vacías.
        iniciar(texto);

        // El cursor avanza de izquierda a derecha. No se separa previamente el
        // texto en palabras porque símbolos como 3.14, "hola" o // comentario
        // necesitan reglas propias para determinar dónde termina su lexema.
        while (!fin()) {
            char actual = actual();

            if (esEspacio(actual)) {
                // Uno o varios caracteres de separación -> TOKEN_WHITESPACE.
                ignorarEspacios();
            } else if (esInicioIdentificador(actual)) {
                // Letra o '_' -> posible identificador o palabra reservada.
                analizarIdentificador();
            } else if (esDigito(actual)) {
                // Dígito -> estado ENTERO y posible transición a DECIMAL.
                analizarNumero();
            } else if (actual == '"') {
                // Comilla doble -> comienza una cadena.
                analizarCadena();
            } else if (actual == '/' && siguienteEs('/')) {
                // Dos barras tienen prioridad sobre el operador '/' individual.
                analizarComentario();
            } else if (esOperador(actual)) {
                // +, -, *, / o = -> TOKEN_OP.
                analizarOperador();
            } else if (esDelimitador(actual)) {
                // (, ), {, } o ; -> TOKEN_DELIM.
                analizarDelimitador();
            } else {
                // Ninguna transición del AFD acepta el carácter actual.
                registrarCaracterNoReconocido();
            }
        }

        // El lexer no decide si la secuencia de tokens forma una instrucción
        // válida. Por ejemplo, reconoce 'if' como TOKEN_IF, pero comprobar qué
        // debe aparecer después correspondería a un futuro analizador sintáctico.
        return new ResultadoAnalisis(tokens, errores, simbolos, recorridoAFD);
    }

    /** Reinicia el cursor y las colecciones para que cada análisis sea independiente. */
    private void iniciar(String fuente) {
        texto = fuente == null ? "" : fuente;
        posicion = 0;
        linea = 1;
        columna = 1;
        tokens = new ArrayList<>();
        errores = new ArrayList<>();
        simbolos = new ArrayList<>();
        recorridoAFD = new ArrayList<>();
    }

    /**
     * Aplica la regla {@code letra|_ (letra|digito|_)*}.
     *
     * <p>Primero consume el lexema completo (estrategia de coincidencia más
     * larga) y después comprueba si coincide exactamente con una palabra
     * reservada. De esta forma, {@code var} es reservada, mientras que
     * {@code variable} continúa siendo un identificador.</p>
     */
    private void analizarIdentificador() {
        // La condición de entrada de este método ya garantizó una letra o '_'.
        Estado estado = Estado.IDENTIFICADOR;

        // Se guarda el punto de partida antes de mover el cursor. Es la posición
        // que posteriormente usarán tanto la tabla como el resaltado visual.
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;

        StringBuilder lexema = new StringBuilder();
        Estado estadoAnterior = Estado.INICIO;

        // Mientras la transición IDENTIFICADOR -> IDENTIFICADOR sea válida,
        // se incorporan caracteres al mismo lexema. El primer carácter que no
        // cumple la regla queda pendiente para la siguiente vuelta principal.
        while (!fin() && estado == Estado.IDENTIFICADOR) {
            char c = actual();
            if (esContinuacionIdentificador(c)) {
                registrarTransicion(estadoAnterior, Estado.IDENTIFICADOR,
                        estadoAnterior == Estado.INICIO
                                ? "letra o _" : "letra, dígito o _",
                        posicion);
                lexema.append(c);
                avanzar();
                estadoAnterior = Estado.IDENTIFICADOR;
            } else {
                break;
            }
        }

        String palabra = lexema.toString();

        // Solo después de obtener la palabra completa se puede distinguir entre
        // una reservada exacta ('if') y un identificador que la contiene ('ifx').
        TipoToken tipo = resolverTipoIdentificador(palabra);
        agregarToken(palabra, tipo, lineaInicio, columnaInicio, inicio);

        // Además de producir el token de esta aparición, se actualiza la tabla
        // de símbolos, que almacena cada lexema una sola vez.
        if (tipo == TipoToken.TOKEN_ID) {
            agregarSimboloSiNoExiste(palabra, "IDENTIFICADOR", lineaInicio);
        } else {
            agregarSimboloSiNoExiste(palabra, "PALABRA_RESERVADA", lineaInicio);
        }
    }

    /** Traduce las palabras reservadas del lenguaje a su tipo de token. */
    private TipoToken resolverTipoIdentificador(String lexema) {
        // Esta selección representa la aceptación final del AFD para palabras.
        // Todas recorren primero la misma regla de identificador; el lexema
        // exacto determina después el tipo específico que se devuelve.
        switch (lexema) {
            case "var":
                return TipoToken.TOKEN_VAR;
            case "print":
                return TipoToken.TOKEN_PRINT;
            case "if":
                return TipoToken.TOKEN_IF;
            case "else":
                return TipoToken.TOKEN_ELSE;
            default:
                return TipoToken.TOKEN_ID;
        }
    }

    /**
     * Reconoce enteros y decimales mediante las transiciones
     * {@code ENTERO -> PUNTO_DECIMAL -> DECIMAL}.
     *
     * <p>Un punto obliga a que exista al menos un dígito posterior. Si aparece
     * otro punto, se consume el bloque numérico defectuoso completo para
     * reportar un solo error y continuar el análisis desde un punto estable.</p>
     */
    private void analizarNumero() {
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;
        Estado estado = Estado.ENTERO;
        boolean primerDigito = true;

        // ENTERO --dígito--> ENTERO. Se consumen todos los dígitos consecutivos.
        while (!fin() && esDigito(actual())) {
            registrarTransicion(primerDigito ? Estado.INICIO : Estado.ENTERO,
                    Estado.ENTERO, "dígito", posicion);
            avanzar();
            primerDigito = false;
        }

        // La parte decimal es opcional, pero si comienza debe estar completa.
        if (!fin() && actual() == '.') {
            // ENTERO --'.'--> PUNTO_DECIMAL.
            registrarTransicion(Estado.ENTERO, Estado.PUNTO_DECIMAL,
                    ".", posicion);
            estado = Estado.PUNTO_DECIMAL;
            avanzar();

            // PUNTO_DECIMAL necesita obligatoriamente un dígito para alcanzar
            // un estado de aceptación. '12.' termina en un estado no válido.
            if (fin() || !esDigito(actual())) {
                registrarTransicion(Estado.PUNTO_DECIMAL, Estado.ERROR,
                        fin() ? "fin de entrada" : "se esperaba dígito",
                        posicion);
                estado = Estado.ERROR;
                String lexema = texto.substring(inicio, posicion);
                agregarError(lexema, "Número decimal incompleto", lineaInicio, columnaInicio, inicio);
                return;
            }

            // PUNTO_DECIMAL --dígito--> DECIMAL.
            estado = Estado.DECIMAL;
            boolean primerDecimal = true;

            // DECIMAL --dígito--> DECIMAL.
            while (!fin() && esDigito(actual())) {
                registrarTransicion(primerDecimal
                        ? Estado.PUNTO_DECIMAL : Estado.DECIMAL,
                        Estado.DECIMAL, "dígito", posicion);
                avanzar();
                primerDecimal = false;
            }

            // Recuperación solicitada para casos como 12.3.4.
            if (!fin() && actual() == '.') {
                // Desde DECIMAL no existe otra transición válida con '.'. Se
                // consume el bloque defectuoso para generar un único error.
                registrarTransicion(Estado.DECIMAL, Estado.ERROR,
                        "segundo punto", posicion);
                estado = Estado.ERROR;
                consumirNumeroMalFormado();
                String lexema = texto.substring(inicio, posicion);
                agregarError(lexema, "Número con formato incorrecto", lineaInicio, columnaInicio, inicio);
                return;
            }
        }

        String lexema = texto.substring(inicio, posicion);

        // Ambos estados son de aceptación, pero generan categorías distintas.
        if (estado == Estado.DECIMAL) {
            agregarToken(lexema, TipoToken.TOKEN_FLOAT, lineaInicio, columnaInicio, inicio);
        } else {
            agregarToken(lexema, TipoToken.TOKEN_INT, lineaInicio, columnaInicio, inicio);
        }
    }

    /** Consume dígitos y puntos restantes como una sola unidad de error. */
    private void consumirNumeroMalFormado() {
        while (!fin()) {
            char c = actual();
            if (esDigito(c) || c == '.') {
                registrarTransicion(Estado.ERROR, Estado.ERROR,
                        "dígito o .", posicion);
                avanzar();
            } else {
                break;
            }
        }
    }

    /**
     * Consume desde la comilla de apertura hasta la comilla de cierre.
     * Las cadenas no pueden atravesar un salto de línea; llegar al salto o al
     * final del archivo sin cerrar genera un error léxico.
     */
    private void analizarCadena() {
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;
        Estado estado = Estado.CADENA;

        // INICIO --'"'--> CADENA. La comilla también forma parte del lexema.
        registrarTransicion(Estado.INICIO, Estado.CADENA, "\"", posicion);
        avanzar(); // comilla de apertura

        while (!fin() && estado == Estado.CADENA) {
            char c = actual();

            if (c == '"') {
                // CADENA --'"'--> CADENA_CERRADA: estado de aceptación.
                registrarTransicion(Estado.CADENA, Estado.CADENA_CERRADA,
                        "\"", posicion);
                avanzar();
                estado = Estado.CADENA_CERRADA;
            } else if (c == '\n' || c == '\r') {
                // En este lenguaje una cadena no puede continuar en otra línea.
                // El salto no se consume para que luego se trate como espacio.
                registrarTransicion(Estado.CADENA, Estado.ERROR,
                        "salto de línea", posicion);
                estado = Estado.ERROR;
            } else {
                // Cualquier otro carácter mantiene al AFD dentro de CADENA.
                registrarTransicion(Estado.CADENA, Estado.CADENA,
                        "carácter", posicion);
                avanzar();
            }
        }

        if (fin() && estado == Estado.CADENA) {
            registrarTransicion(Estado.CADENA, Estado.ERROR,
                    "fin de entrada", posicion);
            estado = Estado.ERROR;
        }

        String lexema = texto.substring(inicio, posicion);

        if (estado == Estado.CADENA_CERRADA) {
            agregarToken(lexema, TipoToken.TOKEN_STRING, lineaInicio, columnaInicio, inicio);
        } else {
            agregarError(lexema, "Cadena sin cerrar", lineaInicio, columnaInicio, inicio);
        }
    }

    /**
     * Consume un comentario {@code //} hasta el salto de línea, sin absorber
     * dicho salto. El comentario se guarda como token ignorable porque su
     * posición todavía es necesaria para colorearlo en el editor.
     */
    private void analizarComentario() {
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;
        Estado estado = Estado.COMENTARIO;

        // INICIO --'/'--> operador potencial --'/'--> COMENTARIO.
        registrarTransicion(Estado.INICIO, Estado.COMENTARIO,
                "//", posicion);
        avanzar(); // /
        avanzar(); // /

        // Todo carácter pertenece al comentario hasta encontrar fin de línea.
        while (!fin() && estado == Estado.COMENTARIO) {
            char c = actual();
            if (c == '\n' || c == '\r') {
                break;
            }
            registrarTransicion(Estado.COMENTARIO, Estado.COMENTARIO,
                    "carácter", posicion);
            avanzar();
        }

        String lexema = texto.substring(inicio, posicion);
        // Se conserva internamente para el resaltado, pero la GUI no lo
        // muestra en la tabla oficial de tokens porque es un elemento Skip.
        agregarToken(lexema, TipoToken.TOKEN_COMMENT, lineaInicio, columnaInicio, inicio);
    }

    /** Reconoce los operadores de un carácter admitidos por MathScript. */
    private void analizarOperador() {
        Estado estado = Estado.OPERADOR;
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;
        char c = actual();

        // Todos los operadores actuales tienen longitud uno. El cursor queda
        // inmediatamente preparado para reconocer el siguiente elemento.
        registrarTransicion(Estado.INICIO, Estado.OPERADOR,
                "operador", posicion);
        avanzar();

        if (estado == Estado.OPERADOR) {
            agregarToken(String.valueOf(c), TipoToken.TOKEN_OP,
                    lineaInicio, columnaInicio, inicio);
        }
    }

    /** Reconoce paréntesis, llaves y punto y coma como delimitadores. */
    private void analizarDelimitador() {
        Estado estado = Estado.DELIMITADOR;
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;
        char c = actual();
        registrarTransicion(Estado.INICIO, Estado.DELIMITADOR,
                "delimitador", posicion);
        avanzar();

        if (estado == Estado.DELIMITADOR) {
            agregarToken(String.valueOf(c), TipoToken.TOKEN_DELIM,
                    lineaInicio, columnaInicio, inicio);
        }
    }

    /**
     * Agrupa espacios, tabulaciones y saltos consecutivos.
     * Aunque no aparecen en la tabla de tokens, conservarlos permite que el
     * resultado cubra todo el texto y mantenga posiciones exactas.
     */
    private void ignorarEspacios() {
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;

        // Se agrupa toda la secuencia en un token ignorable en vez de producir
        // un token diferente por cada espacio o salto de línea.
        while (!fin() && esEspacio(actual())) {
            registrarTransicion(Estado.INICIO, Estado.INICIO,
                    "espacio", posicion);
            avanzar();
        }

        String lexema = texto.substring(inicio, posicion);
        agregarToken(lexema, TipoToken.TOKEN_WHITESPACE,
                lineaInicio, columnaInicio, inicio);
    }

    /**
     * Registra el carácter actual como error y avanza siempre una posición.
     * Ese avance garantiza que una entrada inválida no produzca un ciclo infinito.
     */
    private void registrarCaracterNoReconocido() {
        int inicio = posicion;
        int lineaInicio = linea;
        int columnaInicio = columna;
        String lexema = String.valueOf(actual());
        registrarTransicion(Estado.INICIO, Estado.ERROR,
                "otro carácter", posicion);
        avanzar();
        agregarError(lexema, "Carácter no reconocido", lineaInicio, columnaInicio, inicio);
    }

    private void agregarToken(String lexema, TipoToken tipo,
            int linea, int columna, int posicionInicial) {
        // Cada aparición se conserva, incluso si el mismo lexema ya apareció.
        tokens.add(new Token(lexema, tipo, linea, columna, posicionInicial));
    }

    private void agregarError(String lexema, String mensaje,
            int linea, int columna, int posicionInicial) {
        // Los errores se acumulan; encontrar uno no detiene el resto del análisis.
        errores.add(new ErrorLexico(lexema, mensaje, linea, columna, posicionInicial));
    }

    private void agregarSimboloSiNoExiste(String lexema, String tipo, int linea) {
        // La tabla representa lexemas únicos y conserva su primera aparición.
        for (Simbolo simbolo : simbolos) {
            if (simbolo.getLexema().equals(lexema)) {
                return;
            }
        }
        simbolos.add(new Simbolo(lexema, tipo, linea));
    }

    /** Registra una transición sin intervenir en la decisión léxica. */
    private void registrarTransicion(Estado origen, Estado destino,
            String condicion, int posicionInicial) {
        recorridoAFD.add(new PasoAFD(origen, destino, condicion, posicionInicial));
    }

    private boolean fin() {
        return posicion >= texto.length();
    }

    private char actual() {
        return texto.charAt(posicion);
    }

    /** Consulta el carácter siguiente sin mover el cursor (lookahead de uno). */
    private boolean siguienteEs(char esperado) {
        return posicion + 1 < texto.length()
                && texto.charAt(posicion + 1) == esperado;
    }

    /**
     * Avanza una posición y mantiene línea/columna. CRLF cuenta como un solo
     * salto de línea.
     */
    private void avanzar() {
        if (fin()) {
            return;
        }

        char c = texto.charAt(posicion);

        if (c == '\r') {
            // Windows suele representar un salto con dos caracteres (CRLF).
            // Ambos se consumen juntos para contarlos como una sola línea.
            posicion++;
            if (posicion < texto.length() && texto.charAt(posicion) == '\n') {
                posicion++;
            }
            linea++;
            columna = 1;
        } else if (c == '\n') {
            // Salto LF usado normalmente en Linux y en el texto normalizado.
            posicion++;
            linea++;
            columna = 1;
        } else {
            // Un carácter común solo desplaza el índice y la columna.
            posicion++;
            columna++;
        }
    }

    private boolean esInicioIdentificador(char c) {
        return esLetra(c) || c == '_';
    }

    private boolean esContinuacionIdentificador(char c) {
        return esLetra(c) || esDigito(c) || c == '_';
    }

    private boolean esLetra(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean esDigito(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean esOperador(char c) {
        return c == '+' || c == '-' || c == '*'
                || c == '/' || c == '=';
    }

    private boolean esDelimitador(char c) {
        return c == '(' || c == ')' || c == ';'
                || c == '{' || c == '}';
    }

    private boolean esEspacio(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }
}
