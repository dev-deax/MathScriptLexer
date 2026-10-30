package mathscript;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import mathscript.lexer.Lexer;
import mathscript.model.ResultadoAnalisis;
import mathscript.view.AFDFormalGraphPanel;

/** Genera una imagen completa del lienzo para la revisión visual manual. */
public final class AFDGraphPreview {

    private AFDGraphPreview() {
    }

    public static void main(String[] args) throws Exception {
        ResultadoAnalisis resultado = new Lexer().analizar(
                "var total = 12.5; print(total); ifx // comentario");
        AFDFormalGraphPanel panel = new AFDFormalGraphPanel();
        panel.actualizarRecorrido(resultado.getRecorridoAFD());
        Dimension size = panel.getPreferredSize();
        panel.setSize(size);

        BufferedImage image = new BufferedImage(
                size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        panel.paint(graphics);
        graphics.dispose();

        File output = new File("target/afd-graph-preview.png");
        ImageIO.write(image, "png", output);
        System.out.println(output.getAbsolutePath());
    }
}
