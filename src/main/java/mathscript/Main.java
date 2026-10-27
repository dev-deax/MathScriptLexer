package mathscript;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mathscript.util.Style;
import mathscript.view.MainFrame;

public class Main {

    public static void main(String[] args) {
        // Swing exige construir y modificar sus componentes desde el hilo de
        // despacho de eventos (EDT). invokeLater agenda allí toda la interfaz.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // El Look & Feel multiplataforma permite que la paleta oscura
                    // definida por la aplicación sea consistente en NetBeans/Windows.
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ex) {
                    // Si falla, Swing continúa con el estilo disponible.
                }

                Style.configurarLookAndFeelOscuro();

                // Al mostrarse la ventana se conectan editor, Lexer, tablas,
                // resaltado y operaciones de archivo.
                MainFrame ventana = new MainFrame();
                ventana.setVisible(true);
            }
        });
    }
}
