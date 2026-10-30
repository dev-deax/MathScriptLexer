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
import java.awt.geom.QuadCurve2D;
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
 * Lienzo de solo lectura que representa el AFD conceptual de MathScript.
 * El grafo siempre se dibuja completo; las transiciones del último análisis
 * formal se superponen con mayor color y grosor.
 */
public class AFDGraphPanel extends javax.swing.JPanel {

    private static final int ANCHO_LIENZO = 1360;
    private static final int ALTO_LIENZO = 850;
    private static final int ANCHO_NODO = 156;
    private static final int ALTO_NODO = 46;

    private static final BasicStroke TRAZO_NORMAL = new BasicStroke(
            1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            10f, new float[]{6f, 5f}, 0f);
    private static final BasicStroke TRAZO_RECORRIDO = new BasicStroke(
            3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final EnumMap<Estado, Nodo> nodos = new EnumMap<>(Estado.class);
    private final List<Arista> aristas = new ArrayList<>();
    private final EnumSet<Estado> estadosVisitados = EnumSet.noneOf(Estado.class);
    private final Set<String> transicionesVisitadas = new HashSet<>();

    public AFDGraphPanel() {
        setPreferredSize(new Dimension(ANCHO_LIENZO, ALTO_LIENZO));
        setMinimumSize(new Dimension(ANCHO_LIENZO, ALTO_LIENZO));
        setBackground(Style.COLOR_PANEL);
        setOpaque(true);
        setToolTipText("Grafo del autómata finito determinista de MathScript");

        getAccessibleContext().setAccessibleName("Grafo del AFD de MathScript");
        actualizarDescripcionAccesible(0);

        crearNodos();
        crearAristas();
    }

    /** Sustituye el recorrido resaltado por el de un análisis formal. */
    public void actualizarRecorrido(List<PasoAFD> pasos) {
        estadosVisitados.clear();
        transicionesVisitadas.clear();

        if (pasos != null && !pasos.isEmpty()) {
            estadosVisitados.add(Estado.INICIO);
            for (PasoAFD paso : pasos) {
                estadosVisitados.add(paso.getOrigen());
                estadosVisitados.add(paso.getDestino());
                transicionesVisitadas.add(clave(paso.getOrigen(), paso.getDestino()));
            }
        }

        actualizarDescripcionAccesible(pasos == null ? 0 : pasos.size());
        repaint();
    }

    /** Deja el grafo visible, pero elimina el recorrido del documento anterior. */
    public void limpiarRecorrido() {
        actualizarRecorrido(null);
    }

    private void crearNodos() {
        agregarNodo(Estado.INICIO, 70, 380, false);
        agregarNodo(Estado.IDENTIFICADOR, 340, 125, true);

        agregarNodo(Estado.ENTERO, 340, 300, true);
        agregarNodo(Estado.PUNTO_DECIMAL, 590, 300, false);
        agregarNodo(Estado.DECIMAL, 840, 300, true);

        agregarNodo(Estado.CADENA, 340, 480, false);
        agregarNodo(Estado.CADENA_CERRADA, 590, 480, true);

        agregarNodo(Estado.OPERADOR, 340, 665, true);
        agregarNodo(Estado.DELIMITADOR, 590, 665, true);
        agregarNodo(Estado.COMENTARIO, 840, 665, true);

        agregarNodo(Estado.ERROR, 1110, 480, false);
    }

    private void crearAristas() {
        agregarArista(Estado.INICIO, Estado.INICIO, "espacio", 0, 0, true);
        agregarArista(Estado.INICIO, Estado.IDENTIFICADOR, "letra | _", -38, -28, false);
        agregarArista(Estado.IDENTIFICADOR, Estado.IDENTIFICADOR,
                "letra | dígito | _", 0, 0, true);

        agregarArista(Estado.INICIO, Estado.ENTERO, "dígito", -10, -12, false);
        agregarArista(Estado.ENTERO, Estado.ENTERO, "dígito", 0, 0, true);
        agregarArista(Estado.ENTERO, Estado.PUNTO_DECIMAL, ".", 0, 0, false);
        agregarArista(Estado.PUNTO_DECIMAL, Estado.DECIMAL, "dígito", 0, 0, false);
        agregarArista(Estado.DECIMAL, Estado.DECIMAL, "dígito", 0, 0, true);

        agregarArista(Estado.INICIO, Estado.CADENA, "\"", -5, 10, false);
        agregarArista(Estado.CADENA, Estado.CADENA, "otro carácter", 0, 0, true);
        agregarArista(Estado.CADENA, Estado.CADENA_CERRADA, "\"", 0, 0, false);

        agregarRuta(Estado.INICIO, Estado.OPERADOR, "+ - * / =", 278, 615,
                252, 602, 300, 688);
        agregarRuta(Estado.INICIO, Estado.DELIMITADOR, "( ) { } ;", 420, 756,
                220, 640, 220, 770, 555, 770);
        agregarRuta(Estado.INICIO, Estado.COMENTARIO, "//", 650, 809,
                192, 658, 192, 820, 805, 820);
        agregarArista(Estado.COMENTARIO, Estado.COMENTARIO,
                "hasta fin de línea", 0, 0, true);

        agregarRuta(Estado.PUNTO_DECIMAL, Estado.ERROR,
                "sin dígito", 855, 220,
                668, 245, 1040, 245, 1160, 425);
        agregarArista(Estado.DECIMAL, Estado.ERROR,
                "segundo .", 18, -18, false);
        agregarRuta(Estado.CADENA, Estado.ERROR,
                "salto de línea | fin", 850, 558,
                425, 570, 1035, 570);
        agregarRuta(Estado.INICIO, Estado.ERROR,
                "otro carácter", 900, 95,
                205, 265, 1035, 85, 1188, 420);
        agregarArista(Estado.ERROR, Estado.ERROR, "dígito | .", 0, 0, true);
    }

    private void agregarNodo(Estado estado, int x, int y, boolean aceptacion) {
        nodos.put(estado, new Nodo(estado, x, y, aceptacion));
    }

    private void agregarArista(Estado origen, Estado destino, String etiqueta,
            int curvaX, int curvaY, boolean autoTransicion) {
        aristas.add(new Arista(origen, destino, etiqueta,
                curvaX, curvaY, autoTransicion, null,
                Double.NaN, Double.NaN));
    }

    /**
     * Crea una ruta poligonal por corredores reservados. Las coordenadas se
     * reciben como pares x,y y evitan que las aristas largas crucen otros nodos.
     */
    private void agregarRuta(Estado origen, Estado destino, String etiqueta,
            double etiquetaX, double etiquetaY, double... coordenadas) {
        Point2D[] ruta = new Point2D[coordenadas.length / 2];
        for (int i = 0; i < coordenadas.length; i += 2) {
            ruta[i / 2] = new Point2D.Double(coordenadas[i], coordenadas[i + 1]);
        }
        aristas.add(new Arista(origen, destino, etiqueta,
                0, 0, false, ruta, etiquetaX, etiquetaY));
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
            dibujarFlechaInicial(g2);

            for (Arista arista : aristas) {
                dibujarArista(g2, arista);
            }
            for (Nodo nodo : nodos.values()) {
                dibujarNodo(g2, nodo);
            }
        } finally {
            g2.dispose();
        }
    }

    private void dibujarEncabezado(Graphics2D g2) {
        g2.setColor(Style.COLOR_TEXTO);
        g2.setFont(Style.FUENTE_SUBTITULO);
        g2.drawString("Autómata finito determinista", 24, 28);

        g2.setColor(Style.COLOR_TEXTO_SUAVE);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(12f));
        g2.drawString("Las líneas continuas y gruesas pertenecen al último análisis formal.",
                24, 48);

        int x = 880;
        g2.setStroke(TRAZO_RECORRIDO);
        g2.setColor(Style.COLOR_PRIMARIO);
        g2.drawLine(x, 24, x + 38, 24);
        g2.setColor(Style.COLOR_TEXTO);
        g2.drawString("Recorrido", x + 46, 28);

        g2.setStroke(TRAZO_NORMAL);
        g2.setColor(Style.COLOR_TEXTO_SUAVE);
        g2.drawLine(x + 145, 24, x + 183, 24);
        g2.setColor(Style.COLOR_TEXTO);
        g2.drawString("No recorrido", x + 191, 28);

        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(Style.COLOR_CADENA);
        g2.drawOval(x, 37, 24, 14);
        g2.drawOval(x + 3, 40, 18, 8);
        g2.setColor(Style.COLOR_TEXTO);
        g2.drawString("Aceptación", x + 32, 49);

        g2.setColor(Style.COLOR_ERROR);
        g2.drawRoundRect(x + 145, 37, 24, 14, 7, 7);
        g2.setColor(Style.COLOR_TEXTO);
        g2.drawString("Error", x + 177, 49);
    }

    private void dibujarFlechaInicial(Graphics2D g2) {
        Nodo inicio = nodos.get(Estado.INICIO);
        double y = inicio.centroY();
        double x1 = 28;
        double x2 = inicio.x - 4;
        boolean visitada = estadosVisitados.contains(Estado.INICIO);
        configurarTrazo(g2, visitada);
        g2.drawLine((int) x1, (int) y, (int) x2, (int) y);
        dibujarPunta(g2, x2 - 12, y, x2, y);
        dibujarEtiqueta(g2, "inicio", (x1 + x2) / 2, y - 9, visitada);
    }

    private void dibujarArista(Graphics2D g2, Arista arista) {
        Nodo origen = nodos.get(arista.origen);
        Nodo destino = nodos.get(arista.destino);
        boolean visitada = transicionesVisitadas.contains(
                clave(arista.origen, arista.destino));
        configurarTrazo(g2, visitada);

        if (arista.autoTransicion) {
            dibujarAutoTransicion(g2, origen, arista.etiqueta, visitada);
            return;
        }

        if (arista.ruta != null) {
            dibujarRuta(g2, origen, destino, arista, visitada);
            return;
        }

        double controlX = (origen.centroX() + destino.centroX()) / 2.0 + arista.curvaX;
        double controlY = (origen.centroY() + destino.centroY()) / 2.0 + arista.curvaY;
        Point2D inicio = puntoBorde(origen, controlX, controlY);
        Point2D fin = puntoBorde(destino, controlX, controlY);

        QuadCurve2D curva = new QuadCurve2D.Double(
                inicio.getX(), inicio.getY(), controlX, controlY,
                fin.getX(), fin.getY());
        g2.draw(curva);
        dibujarPunta(g2, controlX, controlY, fin.getX(), fin.getY());

        double etiquetaX = 0.25 * inicio.getX() + 0.5 * controlX + 0.25 * fin.getX();
        double etiquetaY = 0.25 * inicio.getY() + 0.5 * controlY + 0.25 * fin.getY();
        dibujarEtiqueta(g2, arista.etiqueta, etiquetaX, etiquetaY - 7, visitada);
    }

    private void dibujarRuta(Graphics2D g2, Nodo origen, Nodo destino,
            Arista arista, boolean visitada) {
        Point2D primerPunto = arista.ruta[0];
        Point2D ultimoPunto = arista.ruta[arista.ruta.length - 1];
        Point2D inicio = puntoBorde(origen,
                primerPunto.getX(), primerPunto.getY());
        Point2D fin = puntoBorde(destino,
                ultimoPunto.getX(), ultimoPunto.getY());

        Path2D camino = new Path2D.Double();
        camino.moveTo(inicio.getX(), inicio.getY());
        for (Point2D punto : arista.ruta) {
            camino.lineTo(punto.getX(), punto.getY());
        }
        camino.lineTo(fin.getX(), fin.getY());
        g2.draw(camino);

        dibujarPunta(g2, ultimoPunto.getX(), ultimoPunto.getY(),
                fin.getX(), fin.getY());
        dibujarEtiqueta(g2, arista.etiqueta,
                arista.etiquetaX, arista.etiquetaY, visitada);
    }

    private void dibujarAutoTransicion(Graphics2D g2, Nodo nodo,
            String etiqueta, boolean visitada) {
        double inicioX = nodo.centroX() - 28;
        double finX = nodo.centroX() + 28;
        double y = nodo.y + 3;
        double alto = nodo.estado == Estado.INICIO ? 72 : 58;

        CubicCurve2D curva = new CubicCurve2D.Double(
                inicioX, y,
                inicioX - 18, y - alto,
                finX + 18, y - alto,
                finX, y);
        g2.draw(curva);
        dibujarPunta(g2, finX + 18, y - alto, finX, y);
        dibujarEtiqueta(g2, etiqueta, nodo.centroX(), y - alto - 6, visitada);
    }

    private void configurarTrazo(Graphics2D g2, boolean visitada) {
        g2.setStroke(visitada ? TRAZO_RECORRIDO : TRAZO_NORMAL);
        g2.setColor(visitada ? Style.COLOR_PRIMARIO : Style.COLOR_TEXTO_SUAVE);
    }

    private void dibujarNodo(Graphics2D g2, Nodo nodo) {
        boolean visitado = estadosVisitados.contains(nodo.estado);
        boolean error = nodo.estado == Estado.ERROR;

        RoundRectangle2D forma = new RoundRectangle2D.Double(
                nodo.x, nodo.y, ANCHO_NODO, ALTO_NODO, 22, 22);
        g2.setColor(visitado ? Style.COLOR_SUPERFICIE_HOVER : Style.COLOR_SUPERFICIE);
        g2.fill(forma);

        g2.setStroke(visitado ? TRAZO_RECORRIDO : new BasicStroke(1.6f));
        g2.setColor(error ? Style.COLOR_ERROR
                : visitado ? Style.COLOR_PRIMARIO : Style.COLOR_BORDE);
        g2.draw(forma);

        if (nodo.aceptacion) {
            g2.setStroke(new BasicStroke(1.4f));
            g2.setColor(visitado ? Style.COLOR_CADENA : Style.COLOR_TEXTO_SUAVE);
            g2.draw(new RoundRectangle2D.Double(
                    nodo.x + 5, nodo.y + 5,
                    ANCHO_NODO - 10, ALTO_NODO - 10, 18, 18));
        }

        g2.setFont(Style.FUENTE_NORMAL.deriveFont(Font.BOLD, 12f));
        g2.setColor(error ? Style.COLOR_ERROR : Style.COLOR_TEXTO);
        FontMetrics metricas = g2.getFontMetrics();
        String nombre = nodo.estado.name();
        int textoX = (int) (nodo.centroX() - metricas.stringWidth(nombre) / 2.0);
        int textoY = (int) (nodo.centroY()
                + (metricas.getAscent() - metricas.getDescent()) / 2.0);
        g2.drawString(nombre, textoX, textoY);
    }

    private void dibujarEtiqueta(Graphics2D g2, String texto,
            double centroX, double baseY, boolean visitada) {
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(11f));
        FontMetrics metricas = g2.getFontMetrics();
        int ancho = metricas.stringWidth(texto) + 10;
        int alto = metricas.getHeight();
        int x = (int) Math.round(centroX - ancho / 2.0);
        int y = (int) Math.round(baseY - metricas.getAscent());

        g2.setColor(Style.COLOR_PANEL);
        g2.fillRoundRect(x, y, ancho, alto, 8, 8);
        g2.setColor(visitada ? Style.COLOR_PRIMARIO : Style.COLOR_TEXTO_SUAVE);
        g2.drawString(texto, x + 5, y + metricas.getAscent());
    }

    private void dibujarPunta(Graphics2D g2, double desdeX, double desdeY,
            double hastaX, double hastaY) {
        double angulo = Math.atan2(hastaY - desdeY, hastaX - desdeX);
        double longitud = 10;
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
        if (dx == 0 && dy == 0) {
            return new Point2D.Double(nodo.centroX(), nodo.centroY());
        }

        double escalaX = dx == 0 ? Double.POSITIVE_INFINITY
                : (ANCHO_NODO / 2.0) / Math.abs(dx);
        double escalaY = dy == 0 ? Double.POSITIVE_INFINITY
                : (ALTO_NODO / 2.0) / Math.abs(dy);
        double escala = Math.min(escalaX, escalaY);
        return new Point2D.Double(
                nodo.centroX() + dx * escala,
                nodo.centroY() + dy * escala);
    }

    private String clave(Estado origen, Estado destino) {
        return origen.name() + "->" + destino.name();
    }

    private void actualizarDescripcionAccesible(int pasos) {
        String detalle = pasos == 0
                ? "No hay un recorrido resaltado."
                : "El último análisis recorrió " + pasos + " transiciones.";
        getAccessibleContext().setAccessibleDescription(
                "Diagrama completo de estados y transiciones del lexer. " + detalle
                + " Los estados de aceptación usan doble contorno y ERROR está etiquetado.");
    }

    private static final class Nodo {

        private final Estado estado;
        private final int x;
        private final int y;
        private final boolean aceptacion;

        private Nodo(Estado estado, int x, int y, boolean aceptacion) {
            this.estado = estado;
            this.x = x;
            this.y = y;
            this.aceptacion = aceptacion;
        }

        private double centroX() {
            return x + ANCHO_NODO / 2.0;
        }

        private double centroY() {
            return y + ALTO_NODO / 2.0;
        }
    }

    private static final class Arista {

        private final Estado origen;
        private final Estado destino;
        private final String etiqueta;
        private final int curvaX;
        private final int curvaY;
        private final boolean autoTransicion;
        private final Point2D[] ruta;
        private final double etiquetaX;
        private final double etiquetaY;

        private Arista(Estado origen, Estado destino, String etiqueta,
                int curvaX, int curvaY, boolean autoTransicion,
                Point2D[] ruta, double etiquetaX, double etiquetaY) {
            this.origen = origen;
            this.destino = destino;
            this.etiqueta = etiqueta;
            this.curvaX = curvaX;
            this.curvaY = curvaY;
            this.autoTransicion = autoTransicion;
            this.ruta = ruta;
            this.etiquetaX = etiquetaX;
            this.etiquetaY = etiquetaY;
        }
    }
}
