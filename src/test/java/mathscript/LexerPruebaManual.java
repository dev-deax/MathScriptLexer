package mathscript;

import mathscript.lexer.Lexer;
import mathscript.lexer.Estado;
import mathscript.lexer.PasoAFD;
import mathscript.model.ResultadoAnalisis;
import mathscript.model.TipoToken;
import mathscript.model.Token;

/**
 * Prueba sin JUnit para mantener el proyecto sin dependencias externas.
 */
public class LexerPruebaManual {

    public static void main(String[] args) {
        probarCodigoValido();
        probarErrores();
        probarPrioridadReservadas();
        probarTablaSimbolosSinDuplicados();
        probarRecorridoIdentificadores();
        probarRecorridoNumeros();
        probarRecorridoCadenaYComentario();
        probarRecorridoErrores();
        System.out.println("Todas las pruebas manuales terminaron correctamente.");
    }

    private static void probarCodigoValido() {
        String codigo = "var total = 42;\nprint(3.14);\nvar s = \"hola\";";
        ResultadoAnalisis r = new Lexer().analizar(codigo);
        verificar(r.getErrores().isEmpty(), "El código válido no debe producir errores");
        verificar(contiene(r, TipoToken.TOKEN_VAR), "Debe reconocer var");
        verificar(contiene(r, TipoToken.TOKEN_INT), "Debe reconocer enteros");
        verificar(contiene(r, TipoToken.TOKEN_FLOAT), "Debe reconocer decimales");
        verificar(contiene(r, TipoToken.TOKEN_STRING), "Debe reconocer cadenas");
        verificar(contiene(r, TipoToken.TOKEN_PRINT), "Debe reconocer print");
    }

    private static void probarErrores() {
        String codigo = "var total = 12.3.4;\nvar @x = 2;\nprint(\"hola);";
        ResultadoAnalisis r = new Lexer().analizar(codigo);
        verificar(r.getErrores().size() == 3,
                "Se esperaban exactamente 3 errores léxicos");
        verificar(r.getErrores().get(0).getLinea() == 1, "Error numérico en línea 1");
        verificar(r.getErrores().get(1).getLinea() == 2, "Error de carácter en línea 2");
        verificar(r.getErrores().get(2).getLinea() == 3, "Error de cadena en línea 3");
    }

    private static void probarPrioridadReservadas() {
        ResultadoAnalisis r = new Lexer().analizar("var variable print printer if ifx else elsex");
        TipoToken[] esperados = {
            TipoToken.TOKEN_VAR, TipoToken.TOKEN_ID,
            TipoToken.TOKEN_PRINT, TipoToken.TOKEN_ID,
            TipoToken.TOKEN_IF, TipoToken.TOKEN_ID,
            TipoToken.TOKEN_ELSE, TipoToken.TOKEN_ID
        };

        int indice = 0;
        for (Token token : r.getTokens()) {
            if (!token.esIgnorable()) {
                verificar(token.getTipo() == esperados[indice],
                        "Prioridad incorrecta en token " + token.getLexema());
                indice++;
            }
        }
        verificar(indice == esperados.length, "Cantidad de tokens inesperada");
    }

    private static void probarTablaSimbolosSinDuplicados() {
        String codigo = "var contador;\ncontador = 20;\nprint(contador);\nprint(contador);";
        ResultadoAnalisis r = new Lexer().analizar(codigo);

        verificar(r.getSimbolos().size() == 3,
                "La tabla de símbolos debe contener var, contador y print una sola vez");
        verificar(r.getSimbolos().get(0).getLexema().equals("var"),
                "La primera palabra reservada debe ser var");
        verificar(r.getSimbolos().get(1).getLexema().equals("contador"),
                "Debe registrar el identificador contador");
        verificar(r.getSimbolos().get(2).getLexema().equals("print"),
                "Debe registrar print como palabra reservada");
    }

    private static void probarRecorridoIdentificadores() {
        ResultadoAnalisis r = new Lexer().analizar("if dato");

        verificar(contieneTransicion(r, Estado.INICIO, Estado.IDENTIFICADOR),
                "Un identificador debe salir de INICIO");
        verificar(contieneTransicion(r, Estado.IDENTIFICADOR, Estado.IDENTIFICADOR),
                "Los caracteres restantes deben permanecer en IDENTIFICADOR");
    }

    private static void probarRecorridoNumeros() {
        ResultadoAnalisis r = new Lexer().analizar("7 12.34");

        verificar(contieneTransicion(r, Estado.INICIO, Estado.ENTERO),
                "Un número debe comenzar en ENTERO");
        verificar(contieneTransicion(r, Estado.ENTERO, Estado.ENTERO),
                "Los enteros de varios dígitos deben permanecer en ENTERO");
        verificar(contieneTransicion(r, Estado.ENTERO, Estado.PUNTO_DECIMAL),
                "El punto debe abrir la parte decimal");
        verificar(contieneTransicion(r, Estado.PUNTO_DECIMAL, Estado.DECIMAL),
                "El primer dígito decimal debe alcanzar DECIMAL");
        verificar(contieneTransicion(r, Estado.DECIMAL, Estado.DECIMAL),
                "Los dígitos decimales restantes deben permanecer en DECIMAL");
    }

    private static void probarRecorridoCadenaYComentario() {
        ResultadoAnalisis r = new Lexer().analizar("\"ok\" // nota");

        verificar(contieneTransicion(r, Estado.INICIO, Estado.CADENA),
                "La comilla de apertura debe iniciar CADENA");
        verificar(contieneTransicion(r, Estado.CADENA, Estado.CADENA),
                "El contenido debe permanecer en CADENA");
        verificar(contieneTransicion(r, Estado.CADENA, Estado.CADENA_CERRADA),
                "La comilla final debe cerrar la cadena");
        verificar(contieneTransicion(r, Estado.INICIO, Estado.COMENTARIO),
                "Las dos barras deben iniciar COMENTARIO");
        verificar(contieneTransicion(r, Estado.COMENTARIO, Estado.COMENTARIO),
                "El contenido debe permanecer en COMENTARIO");
    }

    private static void probarRecorridoErrores() {
        ResultadoAnalisis r = new Lexer().analizar("@ 12. 12.3.4 \"sin cerrar");

        verificar(r.getErrores().size() == 4,
                "Se esperaban cuatro clases de error en el recorrido");
        verificar(contieneTransicion(r, Estado.INICIO, Estado.ERROR),
                "Un carácter desconocido debe ir de INICIO a ERROR");
        verificar(contieneTransicion(r, Estado.PUNTO_DECIMAL, Estado.ERROR),
                "Un decimal incompleto debe ir de PUNTO_DECIMAL a ERROR");
        verificar(contieneTransicion(r, Estado.DECIMAL, Estado.ERROR),
                "Un segundo punto debe ir de DECIMAL a ERROR");
        verificar(contieneTransicion(r, Estado.CADENA, Estado.ERROR),
                "Una cadena sin cerrar debe ir de CADENA a ERROR");
    }

    private static boolean contiene(ResultadoAnalisis r, TipoToken tipo) {
        for (Token token : r.getTokens()) {
            if (token.getTipo() == tipo) {
                return true;
            }
        }
        return false;
    }

    private static boolean contieneTransicion(ResultadoAnalisis r,
            Estado origen, Estado destino) {
        for (PasoAFD paso : r.getRecorridoAFD()) {
            if (paso.getOrigen() == origen && paso.getDestino() == destino) {
                return true;
            }
        }
        return false;
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new RuntimeException("PRUEBA FALLIDA: " + mensaje);
        }
    }
}
