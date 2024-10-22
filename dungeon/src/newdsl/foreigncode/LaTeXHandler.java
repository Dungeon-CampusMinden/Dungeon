package newdsl.foreigncode;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LaTeXHandler {

    public static BufferedImage createLatexBufferedImage(String content) {
        // Don't let the dsl user do this, use text environment here instead
        String input = String.format("\\text{%s}", content);

        TeXFormula formula = new TeXFormula(input);

        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 40);

        return new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    public static Image createLatexImage(String content) {

        return BufferedImageToLibGDXImage.convertBufferedImageToLibGDXImage(createLatexBufferedImage(content));
    }

    public static void example(String text) {
        // Define the LaTeX expression with normal text
        String latex = String.format("\\text{%s}", text);

        // Create a TeXFormula object
        TeXFormula formula = new TeXFormula(latex);

        // Create an icon from the TeXFormula
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 40);

        // Create a BufferedImage to draw the formula
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        // Create a Graphics2D object to draw on the BufferedImage
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        JLabel jl = new JLabel();
        jl.setForeground(Color.BLACK);
        icon.paintIcon(jl, g2, 0, 0);

        // Save the image to a file
        try {
            ImageIO.write(image, "png", new File("latex_formula_with_text.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clean up
        g2.dispose();
    }
}
