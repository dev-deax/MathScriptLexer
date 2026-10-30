package mathscript.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import mathscript.lexer.Analizador;
import mathscript.lexer.Lexer;
import mathscript.model.ErrorLexico;
import mathscript.model.ResultadoAnalisis;
import mathscript.model.Simbolo;
import mathscript.model.TipoToken;
import mathscript.model.Token;
import mathscript.service.ArchivoService;
import mathscript.util.Style;
import mathscript.util.SyntaxStyle;

/**
 * Ventana principal de MathScript Lexer V2.
 *
 * Mantiene el Lexer separado de la interfaz. El editor utiliza el mismo Lexer
 * para el resaltado visual y el botón Analizar para presentar los resultados
 * formales en las tablas.
 */
public class MainFrame extends JFrame {

    private final JTextPane editor;
    private final JTabbedPane editorTabs;
    private final JTable tablaTokens;
    private final JTable tablaSimbolos;
    private final AFDGraphPanel grafoAFD;
    private final JTextArea consolaErrores;
    private final JLabel estadoLabel;
    private final JButton btnGuardar;

    private final DefaultTableModel modeloTokens;
    private final DefaultTableModel modeloSimbolos;

    private final Analizador analizador;
    private final ArchivoService archivoService;
    private final Timer timerResaltado;

    private File archivoActual;
    private boolean modificado;
    private boolean aplicandoEstilos;
    private boolean actualizandoDocumento;

    public MainFrame() {
        super("MathScript Lexer — Sin título");

        // La interfaz conoce el contrato Analizador, no necesita conocer los
        // detalles del AFD. En esta aplicación se conecta con el Lexer manual.
        analizador = new Lexer();
        archivoService = new ArchivoService();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));
        setSize(1320, 790);
        setLocationRelativeTo(null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Style.COLOR_FONDO);

        editor = new JTextPane();
        Style.aplicarEditor(editor);

        editorTabs = new JTabbedPane();
        Style.aplicarTabs(editorTabs);

        modeloTokens = new DefaultTableModel(
                new Object[]{"Lexema", "Token", "Línea", "Columna"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaTokens = new JTable(modeloTokens);
        Style.aplicarTabla(tablaTokens);

        modeloSimbolos = new DefaultTableModel(
                new Object[]{"Lexema", "Tipo", "Primera línea"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaSimbolos = new JTable(modeloSimbolos);
        Style.aplicarTabla(tablaSimbolos);

        grafoAFD = new AFDGraphPanel();

        consolaErrores = new JTextArea(7, 20);
        consolaErrores.setEditable(false);
        consolaErrores.setLineWrap(true);
        consolaErrores.setWrapStyleWord(true);
        Style.aplicarConsola(consolaErrores);

        estadoLabel = Style.crearTextoEstado("Listo. Escribe código o abre un archivo.");

        btnGuardar = new JButton("Guardar");
        Style.aplicarBotonSecundario(btnGuardar);

        getContentPane().add(crearCabecera(), BorderLayout.NORTH);
        getContentPane().add(crearContenido(), BorderLayout.CENTER);
        getContentPane().add(crearBarraEstado(), BorderLayout.SOUTH);

        timerResaltado = new Timer(400, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Este análisis es únicamente visual: obtiene posiciones y tipos
                // para colorear, pero todavía no llena las tablas de resultados.
                actualizarResaltado(false);
            }
        });
        timerResaltado.setRepeats(false);

        conectarEventosEditor();
        conectarCierreSeguro();
        crearDocumentoNuevo();
    }

    private JPanel crearCabecera() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Style.COLOR_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Style.COLOR_BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel titulo = new JLabel("MathScript Lexer");
        Style.aplicarTitulo(titulo);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);

        JButton btnAbrir = new JButton("Abrir");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnAnalizar = new JButton("Analizar");

        Style.aplicarBotonSecundario(btnAbrir);
        Style.aplicarBotonNeutral(btnLimpiar);
        Style.aplicarBotonPrimario(btnAnalizar);

        btnAbrir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirArchivo();
            }
        });

        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarDocumento();
            }
        });

        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });

        btnAnalizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analizarFormal();
            }
        });

        acciones.add(btnAbrir);
        acciones.add(btnGuardar);
        acciones.add(btnLimpiar);
        acciones.add(btnAnalizar);

        header.add(titulo, BorderLayout.WEST);
        header.add(acciones, BorderLayout.EAST);
        return header;
    }

    private JSplitPane crearContenido() {
        JPanel panelEditor = new JPanel(new BorderLayout());
        panelEditor.setBackground(Style.COLOR_FONDO);
        panelEditor.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 7));

        JScrollPane scrollEditor = new JScrollPane(editor);
        Style.aplicarScrollPane(scrollEditor, Style.COLOR_EDITOR);
        scrollEditor.setRowHeaderView(new LineNumberView(editor));

        editorTabs.addTab("Sin título", scrollEditor);
        editorTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        panelEditor.add(editorTabs, BorderLayout.CENTER);

        JPanel panelDerecho = new JPanel(new BorderLayout(0, 10));
        panelDerecho.setBackground(Style.COLOR_FONDO);
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(14, 7, 10, 14));

        JScrollPane scrollTokens = new JScrollPane(tablaTokens);
        JScrollPane scrollSimbolos = new JScrollPane(tablaSimbolos);
        JScrollPane scrollGrafo = new JScrollPane(grafoAFD);
        Style.aplicarScrollPane(scrollTokens, Style.COLOR_PANEL);
        Style.aplicarScrollPane(scrollSimbolos, Style.COLOR_PANEL);
        Style.aplicarScrollPane(scrollGrafo, Style.COLOR_PANEL);
        scrollGrafo.getHorizontalScrollBar().setUnitIncrement(18);
        scrollGrafo.getVerticalScrollBar().setUnitIncrement(18);

        JTabbedPane tabsResultados = new JTabbedPane();
        Style.aplicarTabs(tabsResultados);
        tabsResultados.addTab("Tokens", scrollTokens);
        tabsResultados.addTab("Tabla de símbolos", scrollSimbolos);
        tabsResultados.addTab("Grafo AFD", scrollGrafo);

        JPanel resultados = new JPanel(new BorderLayout(0, 7));
        resultados.setBackground(Style.COLOR_FONDO);
        resultados.add(crearEtiquetaSeccion("Resultado del análisis"), BorderLayout.NORTH);
        resultados.add(tabsResultados, BorderLayout.CENTER);

        JPanel errores = new JPanel(new BorderLayout(0, 7));
        errores.setBackground(Style.COLOR_FONDO);
        errores.setPreferredSize(new Dimension(400, 190));
        errores.add(crearEtiquetaSeccion("Consola de errores"), BorderLayout.NORTH);

        JScrollPane scrollErrores = new JScrollPane(consolaErrores);
        Style.aplicarScrollPane(scrollErrores, Style.COLOR_EDITOR);
        errores.add(scrollErrores, BorderLayout.CENTER);

        JSplitPane splitDerecho = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultados, errores);
        splitDerecho.setResizeWeight(0.72);
        splitDerecho.setBorder(null);
        splitDerecho.setDividerSize(8);
        splitDerecho.setBackground(Style.COLOR_FONDO);

        panelDerecho.add(splitDerecho, BorderLayout.CENTER);

        JSplitPane principal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEditor, panelDerecho);
        principal.setResizeWeight(0.58);
        principal.setDividerSize(8);
        principal.setBorder(null);
        principal.setBackground(Style.COLOR_FONDO);
        return principal;
    }

    private JLabel crearEtiquetaSeccion(String texto) {
        JLabel label = new JLabel(texto);
        Style.aplicarEtiquetaSeccion(label);
        return label;
    }

    private JPanel crearBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Style.COLOR_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Style.COLOR_BORDE),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));
        panel.add(estadoLabel, BorderLayout.WEST);
        return panel;
    }

    private void conectarEventosEditor() {
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentoModificadoPorUsuario();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentoModificadoPorUsuario();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Aplicar atributos de color produce changedUpdate.
                // No modifica el estado del archivo ni reinicia el timer.
            }
        });
    }

    private void documentoModificadoPorUsuario() {
        if (actualizandoDocumento || aplicandoEstilos) {
            return;
        }

        if (!modificado) {
            modificado = true;
            actualizarIdentidadDocumento();
        }
        programarResaltado();
    }

    private void conectarCierreSeguro() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarCambiosPendientes()) {
                    dispose();
                }
            }
        });
    }

    private void programarResaltado() {
        if (!aplicandoEstilos) {
            // Debounce: cada pulsación reinicia la espera. El análisis visual
            // ocurre solo cuando el usuario deja de escribir durante 400 ms.
            timerResaltado.restart();
        }
    }

    /**
     * Ejecuta el lexer para colorear el editor, sin actualizar las tablas.
     * Esta separación evita presentar resultados formales incompletos mientras
     * el usuario todavía está escribiendo.
     */
    private void actualizarResaltado(boolean incluirErrores) {
        ResultadoAnalisis resultado = analizador.analizar(editor.getText());
        aplicarEstilos(resultado, incluirErrores);
    }

    private void aplicarEstilos(ResultadoAnalisis resultado, boolean incluirErrores) {
        aplicandoEstilos = true;
        try {
            StyledDocument documento = editor.getStyledDocument();

            // Primero se normaliza todo el documento; luego cada token superpone
            // únicamente sus atributos. Así no quedan colores de análisis previos.
            SimpleAttributeSet normal = new SimpleAttributeSet();
            StyleConstants.setForeground(normal, Style.COLOR_IDENTIFICADOR);
            StyleConstants.setFontFamily(normal, Font.MONOSPACED);
            StyleConstants.setFontSize(normal, 15);
            StyleConstants.setBold(normal, false);
            StyleConstants.setUnderline(normal, false);
            documento.setCharacterAttributes(0, documento.getLength(), normal, true);

            for (Token token : resultado.getTokens()) {
                // posicionInicial es el mismo índice absoluto calculado por el
                // Lexer. Permite localizar el lexema sin volver a buscarlo.
                if (token.getPosicionInicial() >= documento.getLength()) {
                    continue;
                }

                SimpleAttributeSet atributo = new SimpleAttributeSet();

                // Aquí ocurre el cambio de color: SyntaxStyle convierte la
                // categoría léxica (TOKEN_STRING, TOKEN_INT, etc.) en un Color.
                StyleConstants.setForeground(atributo, SyntaxStyle.getColor(token.getTipo()));

                // Las reservadas comparten color y además se muestran en negrita.
                StyleConstants.setBold(atributo, esPalabraReservada(token.getTipo()));

                // Se protege el límite por si el documento cambió entre el
                // análisis y la aplicación de estilos.
                int longitud = Math.min(token.getLongitud(),
                        documento.getLength() - token.getPosicionInicial());
                documento.setCharacterAttributes(token.getPosicionInicial(), longitud, atributo, false);
            }

            if (incluirErrores) {
                // Los errores se aplican al final para que el subrayado tenga
                // prioridad visual sobre el color normal de cualquier fragmento.
                for (ErrorLexico error : resultado.getErrores()) {
                    if (error.getPosicionInicial() >= documento.getLength()) {
                        continue;
                    }

                    SimpleAttributeSet atributoError = new SimpleAttributeSet();
                    StyleConstants.setForeground(atributoError, Style.COLOR_ERROR);
                    StyleConstants.setUnderline(atributoError, true);

                    int longitud = Math.max(1, Math.min(error.getLongitud(),
                            documento.getLength() - error.getPosicionInicial()));
                    documento.setCharacterAttributes(error.getPosicionInicial(), longitud,
                            atributoError, false);
                }
            }
        } finally {
            aplicandoEstilos = false;
        }
    }

    private boolean esPalabraReservada(TipoToken tipo) {
        // La decisión se basa en el tipo ya resuelto por el Lexer, no se vuelve
        // a comparar el texto con "var", "print", "if" o "else".
        return tipo == TipoToken.TOKEN_VAR
                || tipo == TipoToken.TOKEN_PRINT
                || tipo == TipoToken.TOKEN_IF
                || tipo == TipoToken.TOKEN_ELSE;
    }

    private void analizarFormal() {
        // Una sola ejecución alimenta las tres salidas y el resaltado. Esto
        // garantiza que tablas, consola y editor describan la misma fuente.
        ResultadoAnalisis resultado = analizador.analizar(editor.getText());

        // El resultado compuesto se distribuye a cada representación de la GUI.
        llenarTokens(resultado);
        llenarSimbolos(resultado);
        llenarErrores(resultado);
        aplicarEstilos(resultado, true);
        grafoAFD.actualizarRecorrido(resultado.getRecorridoAFD());

        // El total mostrado excluye elementos Skip, igual que la tabla visible.
        int tokensVisibles = 0;
        for (Token token : resultado.getTokens()) {
            if (!token.esIgnorable()) {
                tokensVisibles++;
            }
        }

        if (resultado.getErrores().isEmpty()) {
            estadoLabel.setText("Análisis completado: " + tokensVisibles
                    + " tokens, sin errores léxicos.");
        } else {
            estadoLabel.setText("Análisis completado: " + tokensVisibles
                    + " tokens y " + resultado.getErrores().size() + " error(es).");
        }
    }

    private void llenarTokens(ResultadoAnalisis resultado) {
        modeloTokens.setRowCount(0);
        for (Token token : resultado.getTokens()) {
            // Espacios y comentarios se usan para resaltar, pero la gramática
            // los considera elementos Skip y no se muestran como tokens formales.
            if (!token.esIgnorable()) {
                modeloTokens.addRow(new Object[]{
                    token.getLexema(),
                    token.getTipo(),
                    token.getLinea(),
                    token.getColumna()
                });
            }
        }
    }

    private void llenarSimbolos(ResultadoAnalisis resultado) {
        modeloSimbolos.setRowCount(0);
        for (Simbolo simbolo : resultado.getSimbolos()) {
            modeloSimbolos.addRow(new Object[]{
                simbolo.getLexema(),
                simbolo.getTipo(),
                simbolo.getLineaPrimeraAparicion()
            });
        }
    }

    private void llenarErrores(ResultadoAnalisis resultado) {
        if (resultado.getErrores().isEmpty()) {
            consolaErrores.setText("Sin errores léxicos.");
            return;
        }

        StringBuilder textoErrores = new StringBuilder();
        for (ErrorLexico error : resultado.getErrores()) {
            textoErrores.append(error).append('\n');
        }
        consolaErrores.setText(textoErrores.toString());
        consolaErrores.setCaretPosition(0);
    }

    private void abrirArchivo() {
        if (!confirmarCambiosPendientes()) {
            return;
        }

        File seleccionado = archivoService.seleccionarArchivoParaAbrir(this);
        if (seleccionado == null) {
            return;
        }

        try {
            String contenido = archivoService.leerArchivo(seleccionado);
            cargarContenido(contenido);
            archivoActual = seleccionado;
            modificado = false;
            limpiarResultados();
            actualizarIdentidadDocumento();
            estadoLabel.setText("Archivo abierto: " + seleccionado.getName()
                    + ". Presiona Analizar para obtener resultados.");
        } catch (IOException ex) {
            mostrarError("No se pudo abrir el archivo: " + ex.getMessage());
        }
    }

    private boolean guardarDocumento() {
        File destino = archivoActual;

        if (destino == null) {
            destino = archivoService.seleccionarArchivoParaGuardar(this, null);
            if (destino == null) {
                return false;
            }
        }

        try {
            archivoService.guardarArchivo(destino, editor.getText());
            archivoActual = destino;
            modificado = false;
            actualizarIdentidadDocumento();
            estadoLabel.setText("Guardado: " + destino.getName());
            return true;
        } catch (IOException ex) {
            mostrarError("No se pudo guardar el archivo: " + ex.getMessage());
            return false;
        }
    }

    private void limpiar() {
        if (!confirmarCambiosPendientes()) {
            return;
        }

        crearDocumentoNuevo();
        estadoLabel.setText("Nuevo documento listo.");
        editor.requestFocusInWindow();
    }

    private boolean confirmarCambiosPendientes() {
        if (!modificado) {
            return true;
        }

        Object[] opciones = {"Guardar", "No guardar", "Cancelar"};
        int respuesta = JOptionPane.showOptionDialog(
                this,
                "Hay cambios sin guardar.\n¿Desea guardarlos antes de continuar?",
                "Cambios sin guardar",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (respuesta == 0) {
            return guardarDocumento();
        }
        if (respuesta == 1) {
            return true;
        }
        return false;
    }

    private void crearDocumentoNuevo() {
        archivoActual = null;
        modificado = false;
        cargarContenido("");
        limpiarResultados();
        actualizarIdentidadDocumento();
        estadoLabel.setText("Listo. Escribe código o abre un archivo.");
    }

    private void cargarContenido(String contenido) {
        actualizandoDocumento = true;
        try {
            editor.setText(contenido == null ? "" : contenido);
            editor.setCaretPosition(0);
        } finally {
            actualizandoDocumento = false;
        }
        timerResaltado.restart();
    }

    private void limpiarResultados() {
        modeloTokens.setRowCount(0);
        modeloSimbolos.setRowCount(0);
        consolaErrores.setText("");
        grafoAFD.limpiarRecorrido();
    }

    private void actualizarIdentidadDocumento() {
        String nombre = archivoActual == null ? "Sin título" : archivoActual.getName();
        String indicador = modificado ? " *" : "";
        String visible = nombre + indicador;

        if (editorTabs.getTabCount() > 0) {
            editorTabs.setTitleAt(0, visible);
            editorTabs.setToolTipTextAt(0,
                    archivoActual == null ? "Documento nuevo" : archivoActual.getAbsolutePath());
        }

        setTitle("MathScript Lexer — " + visible);

        // En un documento nuevo Guardar permite crear el archivo aunque esté vacío.
        boolean habilitarGuardar = modificado || archivoActual == null;
        Style.actualizarEstadoBoton(btnGuardar, habilitarGuardar);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
