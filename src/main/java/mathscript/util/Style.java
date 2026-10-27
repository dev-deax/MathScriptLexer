package mathscript.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

/**
 * Estilos reutilizables de toda la interfaz V2.
 * La aplicación utiliza un único tema oscuro.
 */
public final class Style {

    private Style() {
    }

    // Superficies generales.
    public static final Color COLOR_FONDO = new Color(15, 17, 23);          // #0F1117
    public static final Color COLOR_PANEL = new Color(23, 26, 34);          // #171A22
    public static final Color COLOR_SUPERFICIE = new Color(30, 35, 45);     // #1E232D
    public static final Color COLOR_SUPERFICIE_HOVER = new Color(39, 45, 58);
    public static final Color COLOR_BORDE = new Color(43, 50, 64);          // #2B3240

    // Texto.
    public static final Color COLOR_TEXTO = new Color(230, 234, 240);       // #E6EAF0
    public static final Color COLOR_TEXTO_SUAVE = new Color(152, 162, 179); // #98A2B3
    public static final Color COLOR_TEXTO_DESHABILITADO = new Color(96, 106, 123);

    // Acción principal.
    public static final Color COLOR_PRIMARIO = new Color(109, 142, 255);    // #6D8EFF
    public static final Color COLOR_PRIMARIO_HOVER = new Color(126, 155, 255);
    public static final Color COLOR_PRIMARIO_PRESSED = new Color(87, 119, 226);
    public static final Color COLOR_TEXTO_PRIMARIO = new Color(248, 250, 255);

    // Editor.
    public static final Color COLOR_EDITOR = new Color(18, 21, 27);         // #12151B
    public static final Color COLOR_LINEAS = new Color(13, 16, 21);         // #0D1015
    public static final Color COLOR_SELECCION = new Color(41, 74, 115);
    public static final Color COLOR_LINEA_ACTUAL = new Color(197, 204, 216);

    // Resaltado de sintaxis pensado específicamente para el fondo oscuro.
    public static final Color COLOR_RESERVADA = new Color(196, 167, 255);   // violeta suave
    public static final Color COLOR_IDENTIFICADOR = COLOR_TEXTO;
    public static final Color COLOR_NUMERO = new Color(242, 193, 133);      // ámbar suave
    public static final Color COLOR_CADENA = new Color(159, 211, 168);      // verde suave
    public static final Color COLOR_OPERADOR = new Color(124, 199, 232);    // celeste
    public static final Color COLOR_DELIMITADOR = new Color(197, 204, 216); // gris claro
    public static final Color COLOR_COMENTARIO = new Color(111, 122, 140);  // gris apagado
    public static final Color COLOR_ERROR = new Color(240, 128, 128);       // rojo moderado

    public static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 23);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_CODIGO = new Font(Font.MONOSPACED, Font.PLAIN, 15);

    /**
     * Configura valores globales del Look & Feel para que controles como tabs,
     * menús, diálogos y scrollbars no conserven superficies claras.
     */
    public static void configurarLookAndFeelOscuro() {
        UIManager.put("Panel.background", COLOR_PANEL);
        UIManager.put("OptionPane.background", COLOR_PANEL);
        UIManager.put("OptionPane.messageForeground", COLOR_TEXTO);
        UIManager.put("Label.foreground", COLOR_TEXTO);

        UIManager.put("Button.background", COLOR_SUPERFICIE);
        UIManager.put("Button.foreground", COLOR_TEXTO);
        UIManager.put("Button.select", COLOR_PRIMARIO_PRESSED);
        UIManager.put("Button.disabledText", COLOR_TEXTO_DESHABILITADO);

        UIManager.put("TextField.background", COLOR_EDITOR);
        UIManager.put("TextField.foreground", COLOR_TEXTO);
        UIManager.put("TextField.caretForeground", COLOR_TEXTO);
        UIManager.put("TextArea.background", COLOR_EDITOR);
        UIManager.put("TextArea.foreground", COLOR_TEXTO);
        UIManager.put("TextPane.background", COLOR_EDITOR);
        UIManager.put("TextPane.foreground", COLOR_TEXTO);

        UIManager.put("Table.background", COLOR_PANEL);
        UIManager.put("Table.foreground", COLOR_TEXTO);
        UIManager.put("Table.gridColor", COLOR_BORDE);
        UIManager.put("Table.selectionBackground", COLOR_SELECCION);
        UIManager.put("Table.selectionForeground", COLOR_TEXTO);
        UIManager.put("TableHeader.background", COLOR_SUPERFICIE);
        UIManager.put("TableHeader.foreground", COLOR_TEXTO);

        UIManager.put("TabbedPane.background", COLOR_FONDO);
        UIManager.put("TabbedPane.foreground", COLOR_TEXTO_SUAVE);
        UIManager.put("TabbedPane.selected", COLOR_SUPERFICIE);
        UIManager.put("TabbedPane.contentAreaColor", COLOR_PANEL);
        UIManager.put("TabbedPane.focus", COLOR_PRIMARIO);
        UIManager.put("TabbedPane.highlight", COLOR_BORDE);
        UIManager.put("TabbedPane.light", COLOR_BORDE);
        UIManager.put("TabbedPane.shadow", COLOR_BORDE);
        UIManager.put("TabbedPane.darkShadow", COLOR_FONDO);
        UIManager.put("TabbedPane.borderHightlightColor", COLOR_BORDE);

        UIManager.put("ScrollPane.background", COLOR_PANEL);
        UIManager.put("Viewport.background", COLOR_PANEL);
        UIManager.put("ScrollBar.background", COLOR_FONDO);
        UIManager.put("ScrollBar.track", COLOR_FONDO);
        UIManager.put("ScrollBar.thumb", new Color(62, 71, 88));
        UIManager.put("ScrollBar.thumbHighlight", new Color(76, 87, 106));
        UIManager.put("ScrollBar.thumbDarkShadow", new Color(48, 55, 69));
        UIManager.put("ScrollBar.thumbShadow", new Color(48, 55, 69));
        UIManager.put("ScrollBar.width", 12);

        UIManager.put("SplitPane.background", COLOR_FONDO);
        UIManager.put("SplitPane.dividerSize", 8);

        UIManager.put("FileChooser.background", COLOR_PANEL);
        UIManager.put("FileChooser.foreground", COLOR_TEXTO);
        UIManager.put("List.background", COLOR_EDITOR);
        UIManager.put("List.foreground", COLOR_TEXTO);
        UIManager.put("List.selectionBackground", COLOR_SELECCION);
        UIManager.put("List.selectionForeground", COLOR_TEXTO);
    }

    public static void aplicarPanel(JPanel panel) {
        panel.setBackground(COLOR_PANEL);
    }

    public static void aplicarTitulo(JLabel label) {
        label.setFont(FUENTE_TITULO);
        label.setForeground(COLOR_TEXTO);
    }

    public static void aplicarEtiquetaSeccion(JLabel label) {
        label.setFont(FUENTE_SUBTITULO);
        label.setForeground(COLOR_TEXTO);
    }

    public static void aplicarBotonPrimario(JButton boton) {
        aplicarBoton(boton,
                COLOR_PRIMARIO,
                COLOR_PRIMARIO_HOVER,
                COLOR_PRIMARIO_PRESSED,
                COLOR_TEXTO_PRIMARIO,
                COLOR_PRIMARIO);
    }

    public static void aplicarBotonSecundario(JButton boton) {
        aplicarBoton(boton,
                COLOR_SUPERFICIE,
                COLOR_SUPERFICIE_HOVER,
                new Color(34, 40, 52),
                COLOR_TEXTO,
                COLOR_BORDE);
    }

    public static void aplicarBotonNeutral(JButton boton) {
        aplicarBoton(boton,
                new Color(25, 29, 38),
                new Color(34, 39, 50),
                new Color(29, 34, 44),
                COLOR_TEXTO_SUAVE,
                COLOR_BORDE);
    }

    private static void aplicarBoton(final JButton boton,
            final Color normal,
            final Color hover,
            final Color pressed,
            final Color texto,
            final Color borde) {

        boton.setFont(FUENTE_NORMAL.deriveFont(Font.BOLD));
        boton.setBackground(normal);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(true);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borde),
                new EmptyBorder(9, 16, 9, 16)));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(boton.isEnabled() ? normal : COLOR_SUPERFICIE);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(pressed);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (boton.isEnabled()) {
                    boton.setBackground(boton.contains(e.getPoint()) ? hover : normal);
                }
            }
        });
    }

    public static void actualizarEstadoBoton(JButton boton, boolean habilitado) {
        boton.setEnabled(habilitado);
        if (habilitado) {
            boton.setForeground(COLOR_TEXTO);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            boton.setBackground(COLOR_SUPERFICIE);
            boton.setForeground(COLOR_TEXTO_DESHABILITADO);
            boton.setCursor(Cursor.getDefaultCursor());
        }
    }

    public static void aplicarEditor(JTextPane editor) {
        editor.setFont(FUENTE_CODIGO);
        editor.setForeground(COLOR_TEXTO);
        editor.setBackground(COLOR_EDITOR);
        editor.setBorder(new EmptyBorder(12, 12, 12, 12));
        editor.setCaretColor(COLOR_TEXTO);
        editor.setSelectionColor(COLOR_SELECCION);
        editor.setSelectedTextColor(COLOR_TEXTO);
    }

    public static void aplicarTabla(JTable tabla) {
        tabla.setFont(FUENTE_NORMAL);
        tabla.setBackground(COLOR_PANEL);
        tabla.setForeground(COLOR_TEXTO);
        tabla.setRowHeight(28);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setSelectionBackground(COLOR_SELECCION);
        tabla.setSelectionForeground(COLOR_TEXTO);
        tabla.setFillsViewportHeight(true);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_NORMAL.deriveFont(Font.BOLD));
        header.setBackground(COLOR_SUPERFICIE);
        header.setForeground(COLOR_TEXTO);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDE));
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 34));
    }

    public static void aplicarTabs(JTabbedPane tabs) {
        tabs.setFont(FUENTE_NORMAL);
        tabs.setBackground(COLOR_FONDO);
        tabs.setForeground(COLOR_TEXTO_SUAVE);
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createLineBorder(COLOR_BORDE));
    }

    public static void aplicarScrollPane(JScrollPane scroll, Color fondo) {
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDE));
        scroll.getViewport().setBackground(fondo);
        scroll.setBackground(fondo);
        if (scroll.getViewport() instanceof JViewport) {
            scroll.getViewport().setOpaque(true);
        }
    }

    public static void aplicarConsola(JTextArea area) {
        area.setFont(FUENTE_CODIGO.deriveFont(13f));
        area.setBackground(COLOR_EDITOR);
        area.setForeground(COLOR_TEXTO);
        area.setCaretColor(COLOR_TEXTO);
        area.setSelectionColor(COLOR_SELECCION);
        area.setSelectedTextColor(COLOR_TEXTO);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    public static JLabel crearTextoEstado(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.LEFT);
        label.setFont(FUENTE_NORMAL.deriveFont(12f));
        label.setForeground(COLOR_TEXTO_SUAVE);
        return label;
    }
}
