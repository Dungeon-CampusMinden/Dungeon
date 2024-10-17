package newdsl.foreigncode;

import com.badlogic.gdx.assets.loaders.PixmapLoader;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import java.awt.image.BufferedImage;

public class BufferedImageToLibGDXImage {

    public static Image convertBufferedImageToLibGDXImage(BufferedImage bufferedImage) {
        // Convert BufferedImage to Pixmap
        Pixmap pixmap = convertBufferedImageToPixmap(bufferedImage);

        // Create a Texture from the Pixmap
        Texture texture = new Texture(pixmap);

        // Dispose of the Pixmap as it is no longer needed
        pixmap.dispose();

        // Create a libGDX Image from the Texture
        return new Image(texture);
    }

    private static Pixmap convertBufferedImageToPixmap(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Create a Pixmap with the same dimensions and format as the BufferedImage
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Get the pixel data from the BufferedImage and set it to the Pixmap
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bufferedImage.getRGB(x, y);

                // Extract ARGB components
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                // Combine into RGBA format
                int rgba = (r << 24) | (g << 16) | (b << 8) | a;

                pixmap.drawPixel(x, y, rgba);
            }
        }

        return pixmap;
    }
}
