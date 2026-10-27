package mathscript.view;

import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mathscript.util.Style;

/**
 * Columna dinámica de números de línea para el editor.
 */
public class LineNumberView extends JTextArea implements DocumentListener {

    private final JTextPane editor;

    public LineNumberView(JTextPane editor) {
        this.editor = editor;
        setEditable(false);
        setFocusable(false);
        setFont(Style.FUENTE_CODIGO);
        setBackground(Style.COLOR_LINEAS);
        setForeground(Style.COLOR_TEXTO_SUAVE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Style.COLOR_BORDE),
                BorderFactory.createEmptyBorder(12, 8, 12, 8)));
        setColumns(3);
        editor.getDocument().addDocumentListener(this);
        actualizar();
    }

    private void actualizar() {
        int total = editor.getDocument().getDefaultRootElement().getElementCount();
        StringBuilder numeros = new StringBuilder();

        for (int i = 1; i <= total; i++) {
            numeros.append(i);
            if (i < total) {
                numeros.append('\n');
            }
        }

        setText(numeros.toString());
        int digitos = Math.max(3, String.valueOf(total).length() + 1);
        setColumns(digitos);
        setPreferredSize(new Dimension(getPreferredSize().width, editor.getPreferredSize().height));
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        actualizar();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        actualizar();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // Los cambios de estilo no alteran la cantidad de líneas.
    }
}
