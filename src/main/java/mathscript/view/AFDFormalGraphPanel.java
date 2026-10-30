package mathscript.view;

import java.awt.BasicStroke;
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

    // El ancho reserva espacio para I, su auto-transición y la etiqueta lateral.
    private static final int ANCHO_LIENZO = 2020;
    private static final int ALTO_LIENZO = 1450;
    private static final int ANCHO_NODO = 154;
    private static final int ALTO_NODO = 54;
    // Los guiones distinguen las conexiones disponibles del recorrido sólido.
    private static final BasicStroke TRAZO_NORMAL = new BasicStroke(
            1.35f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            10f, new float[]{6f, 5f}, 0f);
    private static final BasicStroke TRAZO_ERROR = new BasicStroke(
            1.45f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            10f, new float[]{3f, 5f}, 0f);
    private static final BasicStroke TRAZO_RECORRIDO = new BasicStroke(
            3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    // EnumMap permite localizar la geometría directamente por estado del lexer.
    private final EnumMap<Estado, Nodo> nodos = new EnumMap<>(Estado.class);
    private final List<Arista> aristas = new ArrayList<>();
    // Los conjuntos eliminan repeticiones: el grafo marca uso, no frecuencia.
    private final EnumSet<Estado> estadosVisitados = EnumSet.noneOf(Estado.class);
    private final Set<String> transicionesVisitadas = new HashSet<>();

    public AFDFormalGraphPanel() {
        // El JScrollPane usa estas dimensiones para desplazar el lienzo completo.
        setPreferredSize(new Dimension(ANCHO_LIENZO, ALTO_LIENZO));
        setMinimumSize(new Dimension(ANCHO_LIENZO, ALTO_LIENZO));
        setBackground(Style.COLOR_PANEL);
        setOpaque(true); // Swing debe borrar el fondo antes de cada repintado.
        setToolTipText("AFD formal de 27 estados y control de fin de lexema");
        getAccessibleContext().setAccessibleName("Grafo formal del AFD de MathScript");
        actualizarDescripcionAccesible(0);
        crearNodos(); // Las aristas necesitan que sus extremos ya existan.
        crearAristas();
    }

    /** Sustituye el recorrido resaltado por el del último análisis formal. */
    public void actualizarRecorrido(List<PasoAFD> pasos) {
        // Sustituir, en lugar de acumular, evita mezclar análisis de documentos.
        estadosVisitados.clear();
        transicionesVisitadas.clear();
        if (pasos != null && !pasos.isEmpty()) {
            estadosVisitados.add(Estado.INICIO);
            for (PasoAFD paso : pasos) {
                // Una transición visita ambos extremos, incluso sin auto-bucle.
                estadosVisitados.add(paso.getOrigen());
                estadosVisitados.add(paso.getDestino());
                transicionesVisitadas.add(clave(paso.getOrigen(), paso.getDestino()));
            }
        }
        actualizarDescripcionAccesible(pasos == null ? 0 : pasos.size());
        repaint(); // Solicita a Swing un repintado; no dibuja fuera de su ciclo.
    }

    /** Conserva la topología y elimina únicamente el resaltado anterior. */
    public void limpiarRecorrido() {
        actualizarRecorrido(null);
    }

    /** Coordenadas de esquina superior izquierda; cada fila conserva el orden del PDF. */
    private void crearNodos() {
        // Las filas superiores agrupan espacios, cadenas, símbolos y números.
        agregarNodo(Estado.INICIO, 45, 630);
        agregarNodo(Estado.ESPACIO, 320, 60);
        agregarNodo(Estado.CADENA, 320, 175);
        agregarNodo(Estado.CADENA_CERRADA, 590, 175);
        agregarNodo(Estado.DELIMITADOR, 320, 290);
        agregarNodo(Estado.OPERADOR, 320, 405);
        agregarNodo(Estado.BARRA, 320, 520);
        agregarNodo(Estado.COMENTARIO, 590, 520);
        agregarNodo(Estado.ENTERO, 320, 635);
        agregarNodo(Estado.PUNTO_DECIMAL, 590, 635);
        agregarNodo(Estado.DECIMAL, 860, 635);
        // I recibe las continuaciones de todas las ramas por el corredor derecho.
        agregarNodo(Estado.IDENTIFICADOR, 1640, 1080);
        // Cada prefijo sigue siendo aceptador ID aunque aún no complete una reservada.
        agregarNodo(Estado.PREFIJO_E, 320, 920);
        agregarNodo(Estado.PREFIJO_EL, 590, 920);
        agregarNodo(Estado.PREFIJO_ELS, 860, 920);
        agregarNodo(Estado.PALABRA_ELSE, 1130, 920);
        agregarNodo(Estado.PREFIJO_I, 320, 1035);
        agregarNodo(Estado.PALABRA_IF, 590, 1035);
        agregarNodo(Estado.PREFIJO_P, 320, 1150);
        agregarNodo(Estado.PREFIJO_PR, 590, 1150);
        agregarNodo(Estado.PREFIJO_PRI, 860, 1150);
        agregarNodo(Estado.PREFIJO_PRIN, 1130, 1150);
        agregarNodo(Estado.PALABRA_PRINT, 1400, 1150);
        agregarNodo(Estado.PREFIJO_V, 320, 1265);
        agregarNodo(Estado.PREFIJO_VA, 590, 1265);
        agregarNodo(Estado.PALABRA_VAR, 860, 1265);
        agregarNodo(Estado.ERROR, 1640, 635);
    }

    /** Construye la representación visual; las decisiones léxicas están en Lexer. */
    private void crearAristas() {
        // ID_CHAR = letra ASCII o _. Las iniciales e/i/p/v tienen rama propia.
        agregarDesdeInicio(Estado.ESPACIO, "espacio | tab | CR | LF", 65);
        agregarDesdeInicio(Estado.CADENA, "\"", 180);
        agregarDesdeInicio(Estado.DELIMITADOR, "( ) { } ;", 295);
        agregarDesdeInicio(Estado.OPERADOR, "+  -  *  =", 410);
        agregarDesdeInicio(Estado.BARRA, "/", 525);
        agregarDesdeInicio(Estado.ENTERO, "dígito", 640);
        agregarRuta(Estado.INICIO, Estado.IDENTIFICADOR,
                "ID_CHAR salvo e, i, p, v", false, 780, 1390,
                230, 657, 230, 1400, 1580, 1400, 1580, 1107);
        agregarDesdeInicio(Estado.PREFIJO_E, "e", 925);
        agregarDesdeInicio(Estado.PREFIJO_I, "i", 1040);
        agregarDesdeInicio(Estado.PREFIJO_P, "p", 1155);
        agregarDesdeInicio(Estado.PREFIJO_V, "v", 1270);

        // Un loop consume otro carácter y mantiene el mismo estado.
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

        // ID_CONT = letra ASCII, dígito o _. El signo - excluye la letra que
        // continúa la reservada: K usa f -> R; cualquier otra continuación -> I.
        agregarSalidaID(Estado.PREFIJO_E, "ID_CONT - l", 1000);
        agregarSalidaID(Estado.PREFIJO_EL, "ID_CONT - s", 1000);
        agregarSalidaID(Estado.PREFIJO_ELS, "ID_CONT - e", 1000);
        agregarSalidaID(Estado.PALABRA_ELSE, "ID_CONT", 1000);
        agregarSalidaID(Estado.PREFIJO_I, "ID_CONT - f", 1120);
        agregarSalidaID(Estado.PALABRA_IF, "ID_CONT", 1120);
        agregarSalidaID(Estado.PREFIJO_P, "ID_CONT - r", 1230);
        agregarSalidaID(Estado.PREFIJO_PR, "ID_CONT - i", 1230);
        agregarSalidaID(Estado.PREFIJO_PRI, "ID_CONT - n", 1230);
        agregarSalidaID(Estado.PREFIJO_PRIN, "ID_CONT - t", 1230);
        agregarSalidaID(Estado.PALABRA_PRINT, "ID_CONT", 1230);
        agregarSalidaID(Estado.PREFIJO_V, "ID_CONT - a", 1350);
        agregarSalidaID(Estado.PREFIJO_VA, "ID_CONT - r", 1350);
        agregarSalidaID(Estado.PALABRA_VAR, "ID_CONT", 1350);

        // Los corredores de error evitan atravesar nodos. "fin" describe una
        // detección del controlador al agotar la fuente, no un carácter consumido.
        agregarRutaError(Estado.INICIO, Estado.ERROR, "punto | otro", 1460, 44,
                230, 42, 1725, 42, 1725, 615);
        agregarRutaError(Estado.CADENA, Estado.ERROR, "CR | LF | fin", 1390, 144,
                520, 145, 1580, 145, 1580, 600);
        agregarRutaError(Estado.PUNTO_DECIMAL, Estado.ERROR, "sin dígito | fin", 1300, 735,
                760, 735, 1580, 735, 1580, 715);
        agregarAristaError(Estado.DECIMAL, Estado.ERROR, "segundo punto");
        agregarLoopError(Estado.ERROR, "cualquier carácter");
    }

    /** Cada prefijo tiene su salida visible al identificador general I. */
    private void agregarSalidaID(Estado origen, String etiqueta, int corredorY) {
        // Baja al corredor de su fila y llega a I por el bus vertical derecho.
        Nodo nodo = nodos.get(origen);
        agregarRuta(origen, Estado.IDENTIFICADOR, etiqueta, false,
                nodo.centroX() + 48, corredorY + 16,
                nodo.centroX(), corredorY, 1580, corredorY, 1580, 1107);
    }

    /** Guarda geometría sin modificar la definición ni la aceptación del estado. */
    private void agregarNodo(Estado estado, int x, int y) {
        nodos.put(estado, new Nodo(estado, x, y));
    }

    private void agregarDesdeInicio(Estado destino, String etiqueta, double etiquetaY) {
        // El bus izquierdo reúne las salidas de A sin líneas diagonales entre filas.
        Nodo nodo = nodos.get(destino);
        agregarRuta(Estado.INICIO, destino, etiqueta, false,
                266, etiquetaY, 230, 657, 230, nodo.centroY());
    }

    private void agregarArista(Estado origen, Estado destino, String etiqueta) {
        // null selecciona una recta; NaN indica que su etiqueta se centra sola.
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
        // Cada par x,y representa un vértice; avanzar dos posiciones evita
        // interpretar una coordenada vertical como el siguiente punto horizontal.
        Point2D[] ruta = new Point2D[coordenadas.length / 2];
        for (int i = 0; i < coordenadas.length; i += 2) {
            ruta[i / 2] = new Point2D.Double(coordenadas[i], coordenadas[i + 1]);
        }
        aristas.add(new Arista(origen, destino, etiqueta, false, error,
                ruta, etiquetaX, etiquetaY));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics); // Borra los trazos del repintado previo.
        // La copia aísla colores, fuentes y trazos del contexto recibido de Swing.
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            dibujarSeparadores(g2);
            dibujarFlechaInicial(g2);
            // Primero conexiones y luego nodos: el relleno oculta extremos interiores.
            for (Arista arista : aristas) dibujarArista(g2, arista);
            for (Nodo nodo : nodos.values()) dibujarNodo(g2, nodo);
        } finally {
            g2.dispose(); // Libera sólo la copia; Swing conserva su contexto original.
        }
    }

    private void dibujarSeparadores(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(Style.COLOR_BORDE);
        g2.drawLine(285, 750, 1580, 750);
        g2.setFont(Style.FUENTE_NORMAL.deriveFont(Font.BOLD, 11f));
        g2.setColor(Style.COLOR_TEXTO_SUAVE);
        g2.drawString("TOKENS GENERALES", 24, 22);
        g2.drawString("IDENTIFICADORES Y PALABRAS RESERVADAS", 300, 772);
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
        // La clave consulta la misma pareja origen/destino registrada por Lexer.
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
        // Recortar en los bordes impide que la flecha termine dentro del texto.
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
        // El primer y último vértice determinan por qué lado se conecta cada nodo.
        Point2D primero = arista.ruta[0];
        Point2D ultimo = arista.ruta[arista.ruta.length - 1];
        Point2D inicio = puntoBorde(origen, primero.getX(), primero.getY());
        Point2D fin = puntoBorde(destino, ultimo.getX(), ultimo.getY());
        Path2D camino = new Path2D.Double(); // Une los corredores en un solo trazado.
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
        // B e I usan un bucle lateral para dejar libres las filas contiguas.
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
        // Grosor y patrón refuerzan el color para que el uso también se distinga.
        if (visitada) {
            g2.setStroke(TRAZO_RECORRIDO);
            g2.setColor(error ? Style.COLOR_ERROR : Style.COLOR_PRIMARIO);
        } else {
            g2.setStroke(error ? TRAZO_ERROR : TRAZO_NORMAL);
            g2.setColor(error ? Style.COLOR_ERROR : Style.COLOR_TEXTO_SUAVE);
        }
    }

    private void dibujarNodo(Graphics2D g2, Nodo nodo) {
        // La aceptación viene del enum, evitando duplicar reglas en la interfaz.
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

    private void dibujarEtiqueta(Graphics2D g2, String texto, double centroX,
            double baseY, boolean visitada, boolean error) {
        // Medir la fuente real permite cubrir el fondo sin cortar la etiqueta.
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
        // Se resta medio ancho del texto para alinear su centro con el nodo.
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(texto, (int) (centroX - fm.stringWidth(texto) / 2.0), baseY);
    }

    private void dibujarPunta(Graphics2D g2, double desdeX, double desdeY,
            double hastaX, double hastaY) {
        // atan2 conserva el cuadrante y orienta el triángulo hacia el destino.
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
        // El rayo centro->objetivo corta primero el borde horizontal o vertical.
        // Infinito descarta el eje paralelo y evita dividir por cero.
        double dx = objetivoX - nodo.centroX();
        double dy = objetivoY - nodo.centroY();
        if (dx == 0 && dy == 0) return new Point2D.Double(nodo.centroX(), nodo.centroY());
        double ex = dx == 0 ? Double.POSITIVE_INFINITY
                : (ANCHO_NODO / 2.0) / Math.abs(dx);
        double ey = dy == 0 ? Double.POSITIVE_INFINITY
                : (ALTO_NODO / 2.0) / Math.abs(dy);
        double escala = Math.min(ex, ey); // Primera intersección con la caja del nodo.
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

    /** Geometría local inmutable; x/y son esquina y centroX/Y se derivan de ella. */
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

    /** Datos de dibujo: loop, ruta por vértices o recta; no ejecuta el autómata. */
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
