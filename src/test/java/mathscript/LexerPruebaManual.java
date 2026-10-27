package mathscript;

import mathscript.lexer.Lexer;
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

    private static boolean contiene(ResultadoAnalisis r, TipoToken tipo) {
        for (Token token : r.getTokens()) {
            if (token.getTipo() == tipo) {
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
