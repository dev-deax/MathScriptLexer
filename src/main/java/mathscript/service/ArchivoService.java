package mathscript.service;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Operaciones sencillas de apertura y guardado de archivos MathScript.
 */
public class ArchivoService {

    public File seleccionarArchivoParaAbrir(Component padre) {
        JFileChooser selector = crearSelector();
        selector.setDialogTitle("Abrir código MathScript");

        int opcion = selector.showOpenDialog(padre);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return selector.getSelectedFile();
    }

    public File seleccionarArchivoParaGuardar(Component padre, File archivoActual) {
        JFileChooser selector = crearSelector();
        selector.setDialogTitle("Guardar código MathScript");

        if (archivoActual != null) {
            selector.setSelectedFile(archivoActual);
        } else {
            selector.setSelectedFile(new File("programa.ms"));
        }

        int opcion = selector.showSaveDialog(padre);
        if (opcion != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File archivo = asegurarExtension(selector.getSelectedFile());

        if (archivo.exists() && (archivoActual == null || !archivo.equals(archivoActual))) {
            int respuesta = JOptionPane.showConfirmDialog(
                    padre,
                    "El archivo ya existe. ¿Desea reemplazarlo?",
                    "Confirmar reemplazo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (respuesta != JOptionPane.YES_OPTION) {
                return null;
            }
        }

        return archivo;
    }

    public String leerArchivo(File archivo) throws IOException {
        StringBuilder contenido = new StringBuilder();

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            boolean primera = true;
            while ((linea = lector.readLine()) != null) {
                if (!primera) {
                    contenido.append('\n');
                }
                contenido.append(linea);
                primera = false;
            }
        }

        return contenido.toString();
    }

    public void guardarArchivo(File archivo, String contenido) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo))) {
            escritor.write(contenido == null ? "" : contenido);
        }
    }

    private JFileChooser crearSelector() {
        JFileChooser selector = new JFileChooser();
        selector.setFileFilter(new FileNameExtensionFilter(
                "MathScript y texto (*.ms, *.txt)", "ms", "txt"));
        return selector;
    }

    private File asegurarExtension(File archivo) {
        String nombre = archivo.getName().toLowerCase();
        if (nombre.endsWith(".ms") || nombre.endsWith(".txt")) {
            return archivo;
        }

        File padre = archivo.getParentFile();
        if (padre == null) {
            return new File(archivo.getName() + ".ms");
        }
        return new File(padre, archivo.getName() + ".ms");
    }
}
