package mathscript.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mathscript.lexer.Estado;
import mathscript.lexer.PasoAFD;
import mathscript.util.Style;

/**
 * Diagrama del AFD formal de 27 estados usado por el lexer.
 *
 * <p>El lienzo separa las transiciones que consumen caracteres de la regla
 * externa de corte de lexema. Esa regla emite el token y reinicia en A sin
 * consumir el carácter pendiente; por ello no se representa como una falsa
 * transición del AFD hacia ERROR.</p>
 */
public class AFDFormalGraphPanel extends javax.swing.JPanel {

    private static final int ANCHO_LIENZO = 1840;
    private static final int ALTO_LIENZO = 1135;
    private static final int ANCHO_NODO = 154;
    private static final int ALTO_NODO = 54;
    private static final BasicStroke TRAZO_NORMAL = new BasicStroke(
            1.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            10f, new float[]{6f, 5f}, 0f);
    private static final BasicStroke TRAZO_ERROR = new BasicStroke(
            1.45f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            10f, new float[]{3f, 5f}, 0f);
    private static final BasicStroke TRAZO_RECORRIDO = new BasicStroke(
            3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final EnumMap<Estado, Nodo> nodos = new EnumMap<>(Estado.class);
    private final List<Arista> aristas = new ArrayList<>();
    private final EnumSet<Estado> estadosVisitados = EnumSet.noneOf(Estado.class);
    private final Set<String> transicionesVisitadas = new HashSet<>();
    private boolean retornoIdentificadorVisitado;

    public AFDFormalGraphPanel() {
        setPreferredSize(new Dimension(ANCHO_LIENZO, ALTO_LIENZO));
        setMinimumSize(new Dimension(ANCHO_LIENZO, ALTO_LIENZO));
        setBackground(Style.COLOR_PANEL);
        setOpaque(true);
        setToolTipText("AFD formal de 27 estados y control de fin de lexema");
        getAccessibleContext().setAccessibleName("Grafo formal del AFD de MathScript");
        actualizarDescripcionAccesible(0);
        crearNodos();
        crearAristas();
    }

    /** Sustituye el recorrido resaltado por el del último análisis formal. */
    public void actualizarRecorrido(List<PasoAFD> pasos) {
        estadosVisitados.clear();
        transicionesVisitadas.clear();
        retornoIdentificadorVisitado = false;
        if (pasos != null && !pasos.isEmpty()) {
            estadosVisitados.add(Estado.INICIO);
            for (PasoAFD paso : pasos) {
                estadosVisitados.add(paso.getOrigen());
                estadosVisitados.add(paso.getDestino());
                transicionesVisitadas.add(clave(paso.getOrigen(), paso.getDestino()));
                if (paso.getDestino() == Estado.IDENTIFICADOR
                        && paso.getOrigen() != Estado.INICIO
                        && paso.getOrigen() != Estado.IDENTIFICADOR) {
                    retornoIdentificadorVisitado = true;
                }
            }
        }
        actualizarDescripcionAccesible(pasos == null ? 0 : pasos.size());
        repaint();
    }

    public void limpiarRecorrido() {
        actualizarRecorrido(null);
    }

    private void crearNodos() {
        agregarNodo(Estado.INICIO, 45, 510);
        agregarNodo(Estado.ESPACIO, 320, 100);
        agregarNodo(Estado.CADENA, 320, 185);
        agregarNodo(Estado.CADENA_CERRADA, 590, 185);
        agregarNodo(Estado.DELIMITADOR, 320, 270);
        agregarNodo(Estado.OPERADOR, 320, 355);
        agregarNodo(Estado.BARRA, 320, 440);
        agregarNodo(Estado.COMENTARIO, 590, 440);
        agregarNodo(Estado.ENTERO, 320, 530);
        agregarNodo(Estado.PUNTO_DECIMAL, 590, 530);
        agregarNodo(Estado.DECIMAL, 860, 530);
        agregarNodo(Estado.IDENTIFICADOR, 320, 675);
        agregarNodo(Estado.PREFIJO_E, 320, 785);
        agregarNodo(Estado.PREFIJO_EL, 590, 785);
        agregarNodo(Estado.PREFIJO_ELS, 860, 785);
        agregarNodo(Estado.PALABRA_ELSE, 1130, 785);
        agregarNodo(Estado.PREFIJO_I, 320, 870);
        agregarNodo(Estado.PALABRA_IF, 590, 870);
        agregarNodo(Estado.PREFIJO_P, 320, 955);
        agregarNodo(Estado.PREFIJO_PR, 590, 955);
        agregarNodo(Estado.PREFIJO_PRI, 860, 955);
        agregarNodo(Estado.PREFIJO_PRIN, 1130, 955);
        agregarNodo(Estado.PALABRA_PRINT, 1400, 955);
        agregarNodo(Estado.PREFIJO_V, 320, 1040);
        agregarNodo(Estado.PREFIJO_VA, 590, 1040);
        agregarNodo(Estado.PALABRA_VAR, 860, 1040);
        agregarNodo(Estado.ERROR, 1640, 530);
    }

    private void crearAristas() {
        agregarDesdeInicio(Estado.ESPACIO, "espacio | tab | CR | LF", 105);
        agregarDesdeInicio(Estado.CADENA, "\"", 190);
        agregarDesdeInicio(Estado.DELIMITADOR, "( ) { } ;", 275);
        agregarDesdeInicio(Estado.OPERADOR, "+  -  *  =", 360);
        agregarDesdeInicio(Estado.BARRA, "/", 445);
        agregarDesdeInicio(Estado.ENTERO, "dígito", 535);
        agregarDesdeInicio(Estado.IDENTIFICADOR,
                "letra | _  salvo e, i, p, v", 680);
        agregarDesdeInicio(Estado.PREFIJO_E, "e", 790);
        agregarDesdeInicio(Estado.PREFIJO_I, "i", 875);
        agregarDesdeInicio(Estado.PREFIJO_P, "p", 960);
        agregarDesdeInicio(Estado.PREFIJO_V, "v", 1045);

        agregarLoop(Estado.ESPACIO, "espacio | tab | CR | LF");
        agregarLoop(Estado.CADENA, "otro salvo \" | CR | LF");
        agregarArista(Estado.CADENA, Estado.CADENA_CERRADA, "\"");
        agregarArista(Estado.BARRA, Estado.COMENTARIO, "/");
        agregarLoop(Estado.COMENTARIO, "otro salvo CR | LF");
        agregarLoop(Estado.ENTERO, "dígito");
        agregarArista(Estado.ENTERO, Estado.PUNTO_DECIMAL, ".");
        agregarArista(Estado.PUNTO_DECIMAL, Estado.DECIMAL, "dígito");
        agregarLoop(Estado.DECIMAL, "dígito");
        agregarLoop(Estado.IDENTIFICADOR, "letra | dígito | _");
        agregarArista(Estado.PREFIJO_E, Estado.PREFIJO_EL, "l");
        agregarArista(Estado.PREFIJO_EL, Estado.PREFIJO_ELS, "s");
        agregarArista(Estado.PREFIJO_ELS, Estado.PALABRA_ELSE, "e");
        agregarArista(Estado.PREFIJO_I, Estado.PALABRA_IF, "f");
        agregarArista(Estado.PREFIJO_P, Estado.PREFIJO_PR, "r");
        agregarArista(Estado.PREFIJO_PR, Estado.PREFIJO_PRI, "i");
        agregarArista(Estado.PREFIJO_PRI, Estado.PREFIJO_PRIN, "n");
        agregarArista(Estado.PREFIJO_PRIN, Estado.PALABRA_PRINT, "t");
        agregarArista(Estado.PREFIJO_V, Estado.PREFIJO_VA, "a");
        agregarArista(Estado.PREFIJO_VA, Estado.PALABRA_VAR, "r");

        agregarRutaError(Estado.INICIO, Estado.ERROR, "punto | otro", 1460, 84,
                230, 82, 1725, 82, 1725, 510);
        agregarRutaError(Estado.CADENA, Estado.ERROR, "CR | LF | fin", 1390, 154,
                520, 155, 1580, 155, 1580, 495);
        agregarRutaError(Estado.PUNTO_DECIMAL, Estado.ERROR, "sin dígito | fin", 1300, 620,
                760, 620, 1580, 620, 1580, 610);
        agregarAristaError(Estado.DECIMAL, Estado.ERROR, "segundo punto");
        agregarLoopError(Estado.ERROR, "cualquier carácter");
    }

    private void agregarNodo(Estado estado, int x, int y) {
        nodos.put(estado, new Nodo(estado, x, y));
    }

    private void agregarDesdeInicio(Estado destino, String etiqueta, double etiquetaY) {
        Nodo nodo = nodos.get(destino);
        agregarRuta(Estado.INICIO, destino, etiqueta, false,
                266, etiquetaY, 230, 537, 230, nodo.centroY());
    }

    private void agregarArista(Estado origen, Estado destino, String etiqueta) {
        aristas.add(new Arista(origen, destino, etiqueta, false, false,
                null, Double.NaN, Double.NaN));
    }

    private void agregarAristaError(Estado origen, Estado destino, String etiqueta) {
        aristas.add(new Arista(origen, destino, etiqueta, false, true,
                null, Double.NaN, Double.NaN));
    }

    private void agregarLoop(Estado estado, String etiqueta) {
        aristas.add(new Arista(estado, estado, etiqueta, true, false,
                null, Double.NaN, Double.NaN));
    }

    private void agregarLoopError(Estado estado, String etiqueta) {
        aristas.add(new Arista(estado, estado, etiqueta, true, true,
                null, Double.NaN, Double.NaN));
    }

    private void agregarRutaError(Estado origen, Estado destino, String etiqueta,
            double etiquetaX, double etiquetaY, double... coordenadas) {
        agregarRuta(origen, destino, etiqueta, true, etiquetaX, etiquetaY, coordenadas);
    }

    private void agregarRuta(Estado origen, Estado destino, String etiqueta,
            boolean error, double etiquetaX, double etiquetaY, double... coordenadas) {
        Point2D[] ruta = new Point2D[coordenadas.length / 2];
        for (int i = 0; i < coordenadas.length; i += 2) {
            ruta[i / 2] = new Point2D.Double(coordenadas[i], coordenadas[i + 1]);
        }
        aristas.add(new Arista(origen, destino, etiqueta, false, error,
                ruta, etiquetaX, etiquetaY));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            dibujarEncabezado(g2);
            dibujarSeparadores(g2);
            dibujarFlechaInicial(g2);
            for (Arista arista : aristas) dibujarArista(g2, arista);
            dibujarReglaFallback(g2);
            dibujarControlLexer(g2);
            for (Nodo nodo : nodos.values()) dibujarNodo(g2, nodo);
        } finally {
            g2.dispose();
        }
    }

    private void dibujarEncabezado(Graphics2D g2) {
        g2.setColor(Style.COLOR_TEXTO);
        g2.setFont(Style.FUENTE_SUBTITULO);
        g2.drawString("AFD léxico formal · 27 estados", 24, 28);
        g2.setColor(Style.COLOR_TEXTO_SUAVE);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(12f));
        g2.drawString("A-AA corresponden al documento. El fin de lexema pertenece al controlador, no a ERROR.", 24, 49);
        int x = 1210;
        dibujarMuestraLinea(g2, x, 23, Style.COLOR_PRIMARIO, TRAZO_RECORRIDO, "Recorrido");
        dibujarMuestraLinea(g2, x + 185, 23, Style.COLOR_TEXTO_SUAVE, TRAZO_NORMAL, "Transición");
        dibujarMuestraLinea(g2, x + 370, 23, Style.COLOR_ERROR, TRAZO_ERROR, "Error real");
    }

    private void dibujarMuestraLinea(Graphics2D g2, int x, int y, Color color,
            BasicStroke stroke, String texto) {
        g2.setColor(color);
        g2.setStroke(stroke);
        g2.drawLine(x, y, x + 35, y);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(11f));
        g2.setColor(Style.COLOR_TEXTO);
        g2.drawString(texto, x + 44, y + 4);
    }

    private void dibujarSeparadores(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Style.COLOR_BORDE);
        g2.drawLine(24, 72, ANCHO_LIENZO - 24, 72);
        g2.drawLine(285, 620, 1580, 620);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(Font.BOLD, 11f));
        g2.setColor(Style.COLOR_TEXTO_SUAVE);
        g2.drawString("TOKENS GENERALES", 24, 92);
        g2.drawString("IDENTIFICADORES Y PALABRAS RESERVADAS", 300, 642);
    }

    private void dibujarFlechaInicial(Graphics2D g2) {
        Nodo inicio = nodos.get(Estado.INICIO);
        boolean visitada = estadosVisitados.contains(Estado.INICIO);
        configurarTrazo(g2, visitada, false);
        double y = inicio.centroY();
        g2.drawLine(18, (int) y, inicio.x - 5, (int) y);
        dibujarPunta(g2, inicio.x - 18, y, inicio.x - 5, y);
        dibujarEtiqueta(g2, "inicio", 30, y - 9, visitada, false);
    }

    private void dibujarArista(Graphics2D g2, Arista arista) {
        Nodo origen = nodos.get(arista.origen);
        Nodo destino = nodos.get(arista.destino);
        boolean visitada = transicionesVisitadas.contains(clave(arista.origen, arista.destino));
        configurarTrazo(g2, visitada, arista.error);
        if (arista.loop) dibujarLoop(g2, origen, arista, visitada);
        else if (arista.ruta != null) dibujarRuta(g2, origen, destino, arista, visitada);
        else dibujarRecta(g2, origen, destino, arista, visitada);
    }

    private void dibujarRecta(Graphics2D g2, Nodo origen, Nodo destino,
            Arista arista, boolean visitada) {
        Point2D inicio = puntoBorde(origen, destino.centroX(), destino.centroY());
        Point2D fin = puntoBorde(destino, origen.centroX(), origen.centroY());
        g2.drawLine((int) inicio.getX(), (int) inicio.getY(),
                (int) fin.getX(), (int) fin.getY());
        dibujarPunta(g2, inicio.getX(), inicio.getY(), fin.getX(), fin.getY());
        dibujarEtiqueta(g2, arista.etiqueta,
                (inicio.getX() + fin.getX()) / 2,
                (inicio.getY() + fin.getY()) / 2 - 8,
                visitada, arista.error);
    }

    private void dibujarRuta(Graphics2D g2, Nodo origen, Nodo destino,
            Arista arista, boolean visitada) {
        Point2D primero = arista.ruta[0];
        Point2D ultimo = arista.ruta[arista.ruta.length - 1];
        Point2D inicio = puntoBorde(origen, primero.getX(), primero.getY());
        Point2D fin = puntoBorde(destino, ultimo.getX(), ultimo.getY());
        Path2D camino = new Path2D.Double();
        camino.moveTo(inicio.getX(), inicio.getY());
        for (Point2D punto : arista.ruta) camino.lineTo(punto.getX(), punto.getY());
        camino.lineTo(fin.getX(), fin.getY());
        g2.draw(camino);
        dibujarPunta(g2, ultimo.getX(), ultimo.getY(), fin.getX(), fin.getY());
        dibujarEtiqueta(g2, arista.etiqueta, arista.etiquetaX,
                arista.etiquetaY, visitada, arista.error);
    }

    private void dibujarLoop(Graphics2D g2, Nodo nodo, Arista arista,
            boolean visitada) {
        if (nodo.estado == Estado.ESPACIO
                || nodo.estado == Estado.IDENTIFICADOR) {
            double x = nodo.x + ANCHO_NODO - 3;
            double y1 = nodo.centroY() - 17;
            double y2 = nodo.centroY() + 17;
            CubicCurve2D curvaLateral = new CubicCurve2D.Double(
                    x, y1, x + 68, y1 - 18, x + 68, y2 + 18, x, y2);
            g2.draw(curvaLateral);
            dibujarPunta(g2, x + 68, y2 + 18, x, y2);
            dibujarEtiqueta(g2, arista.etiqueta, x + 82, nodo.centroY() + 3,
                    visitada, arista.error);
            return;
        }
        double x1 = nodo.centroX() - 28;
        double x2 = nodo.centroX() + 28;
        double y = nodo.y + 3;
        CubicCurve2D curva = new CubicCurve2D.Double(
                x1, y, x1 - 18, y - 58, x2 + 18, y - 58, x2, y);
        g2.draw(curva);
        dibujarPunta(g2, x2 + 18, y - 58, x2, y);
        dibujarEtiqueta(g2, arista.etiqueta, nodo.centroX(), y - 64,
                visitada, arista.error);
    }

    private void configurarTrazo(Graphics2D g2, boolean visitada, boolean error) {
        if (visitada) {
            g2.setStroke(TRAZO_RECORRIDO);
            g2.setColor(error ? Style.COLOR_ERROR : Style.COLOR_PRIMARIO);
        } else {
            g2.setStroke(error ? TRAZO_ERROR : TRAZO_NORMAL);
            g2.setColor(error ? Style.COLOR_ERROR : Style.COLOR_TEXTO_SUAVE);
        }
    }

    private void dibujarNodo(Graphics2D g2, Nodo nodo) {
        boolean visitado = estadosVisitados.contains(nodo.estado);
        boolean error = nodo.estado == Estado.ERROR;
        RoundRectangle2D forma = new RoundRectangle2D.Double(
                nodo.x, nodo.y, ANCHO_NODO, ALTO_NODO, 20, 20);
        g2.setColor(visitado ? Style.COLOR_SUPERFICIE_HOVER : Style.COLOR_SUPERFICIE);
        g2.fill(forma);
        g2.setStroke(visitado ? TRAZO_RECORRIDO : new BasicStroke(1.6f));
        g2.setColor(error ? Style.COLOR_ERROR
                : visitado ? Style.COLOR_PRIMARIO : Style.COLOR_BORDE);
        g2.draw(forma);
        if (nodo.estado.esAceptacion()) {
            g2.setStroke(new BasicStroke(1.25f));
            g2.setColor(visitado ? Style.COLOR_CADENA : Style.COLOR_TEXTO_SUAVE);
            g2.draw(new RoundRectangle2D.Double(nodo.x + 5, nodo.y + 5,
                    ANCHO_NODO - 10, ALTO_NODO - 10, 16, 16));
        }
        g2.setColor(error ? Style.COLOR_ERROR : Style.COLOR_TEXTO);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(Font.BOLD, 12f));
        centrarTexto(g2, nodo.estado.getCodigo(), nodo.centroX(), nodo.y + 20);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(10.5f));
        centrarTexto(g2, nodo.estado.getEtiqueta(), nodo.centroX(), nodo.y + 39);
    }

    private void dibujarReglaFallback(Graphics2D g2) {
        int x = 1305, y = 815, ancho = 445, alto = 82;
        g2.setColor(retornoIdentificadorVisitado
                ? Style.COLOR_SUPERFICIE_HOVER : Style.COLOR_SUPERFICIE);
        g2.fillRoundRect(x, y, ancho, alto, 16, 16);
        g2.setStroke(retornoIdentificadorVisitado ? TRAZO_RECORRIDO : TRAZO_NORMAL);
        g2.setColor(retornoIdentificadorVisitado
                ? Style.COLOR_PRIMARIO : Style.COLOR_TEXTO_SUAVE);
        g2.drawRoundRect(x, y, ancho, alto, 16, 16);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(Font.BOLD, 11.5f));
        g2.setColor(Style.COLOR_TEXTO);
        g2.drawString("REGLA COMÚN DE IDENTIFICADORES", x + 16, y + 23);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(11f));
        g2.setColor(Style.COLOR_TEXTO_SUAVE);
        g2.drawString("Desde cualquier prefijo o palabra reservada:", x + 16, y + 45);
        g2.drawString("otra letra, dígito o _  →  I · ID", x + 16, y + 65);
    }

    private void dibujarControlLexer(Graphics2D g2) {
        int x = 1130, y = 650, ancho = 620, alto = 84;
        g2.setColor(Style.COLOR_SUPERFICIE);
        g2.fillRoundRect(x, y, ancho, alto, 16, 16);
        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(Style.COLOR_CADENA);
        g2.drawRoundRect(x, y, ancho, alto, 16, 16);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(Font.BOLD, 11.5f));
        g2.setColor(Style.COLOR_TEXTO);
        g2.drawString("CONTROL DEL LEXER · NO ES UNA TRANSICIÓN DEL AFD", x + 16, y + 23);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(11f));
        g2.setColor(Style.COLOR_TEXTO_SUAVE);
        g2.drawString("Estado aceptador + carácter que no continúa → emitir token → volver a A", x + 16, y + 46);
        g2.drawString("El carácter pendiente no se consume y se analiza como inicio del siguiente lexema.", x + 16, y + 66);
    }

    private void dibujarEtiqueta(Graphics2D g2, String texto, double centroX,
            double baseY, boolean visitada, boolean error) {
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(10.5f));
        FontMetrics fm = g2.getFontMetrics();
        int ancho = fm.stringWidth(texto) + 10;
        int alto = fm.getHeight();
        int x = (int) Math.round(centroX - ancho / 2.0);
        int y = (int) Math.round(baseY - fm.getAscent());
        g2.setColor(Style.COLOR_PANEL);
        g2.fillRoundRect(x, y, ancho, alto, 8, 8);
        g2.setColor(error ? Style.COLOR_ERROR
                : visitada ? Style.COLOR_PRIMARIO : Style.COLOR_TEXTO_SUAVE);
        g2.drawString(texto, x + 5, y + fm.getAscent());
    }

    private void centrarTexto(Graphics2D g2, String texto, double centroX, int baseY) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (int) (centroX - fm.stringWidth(texto) / 2.0), baseY);
    }

    private void dibujarPunta(Graphics2D g2, double desdeX, double desdeY,
            double hastaX, double hastaY) {
        double angulo = Math.atan2(hastaY - desdeY, hastaX - desdeX);
        double longitud = 9;
        double apertura = Math.PI / 7;
        Path2D punta = new Path2D.Double();
        punta.moveTo(hastaX, hastaY);
        punta.lineTo(hastaX - longitud * Math.cos(angulo - apertura),
                hastaY - longitud * Math.sin(angulo - apertura));
        punta.lineTo(hastaX - longitud * Math.cos(angulo + apertura),
                hastaY - longitud * Math.sin(angulo + apertura));
        punta.closePath();
        g2.fill(punta);
    }

    private Point2D puntoBorde(Nodo nodo, double objetivoX, double objetivoY) {
        double dx = objetivoX - nodo.centroX();
        double dy = objetivoY - nodo.centroY();
        if (dx == 0 && dy == 0) return new Point2D.Double(nodo.centroX(), nodo.centroY());
        double ex = dx == 0 ? Double.POSITIVE_INFINITY
                : (ANCHO_NODO / 2.0) / Math.abs(dx);
        double ey = dy == 0 ? Double.POSITIVE_INFINITY
                : (ALTO_NODO / 2.0) / Math.abs(dy);
        double escala = Math.min(ex, ey);
        return new Point2D.Double(nodo.centroX() + dx * escala,
                nodo.centroY() + dy * escala);
    }

    private String clave(Estado origen, Estado destino) {
        return origen.name() + "->" + destino.name();
    }

    private void actualizarDescripcionAccesible(int pasos) {
        String detalle = pasos == 0 ? "No hay recorrido resaltado."
                : "El último análisis recorrió " + pasos + " transiciones.";
        getAccessibleContext().setAccessibleDescription(
                "Diagrama de 27 estados A-AA. Los estados aceptadores tienen doble contorno; "
                + "las transiciones de error están etiquetadas y el control de fin de lexema "
                + "se explica por separado. " + detalle);
    }

    private static final class Nodo {
        private final Estado estado;
        private final int x;
        private final int y;
        private Nodo(Estado estado, int x, int y) {
            this.estado = estado;
            this.x = x;
            this.y = y;
        }
        private double centroX() { return x + ANCHO_NODO / 2.0; }
        private double centroY() { return y + ALTO_NODO / 2.0; }
    }

    private static final class Arista {
        private final Estado origen;
        private final Estado destino;
        private final String etiqueta;
        private final boolean loop;
        private final boolean error;
        private final Point2D[] ruta;
        private final double etiquetaX;
        private final double etiquetaY;
        private Arista(Estado origen, Estado destino, String etiqueta,
                boolean loop, boolean error, Point2D[] ruta,
                double etiquetaX, double etiquetaY) {
            this.origen = origen;
            this.destino = destino;
            this.etiqueta = etiqueta;
            this.loop = loop;
            this.error = error;
            this.ruta = ruta;
            this.etiquetaX = etiquetaX;
            this.etiquetaY = etiquetaY;
        }
    }
}
