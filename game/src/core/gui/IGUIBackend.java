package core.gui;

import core.Assets;
import core.gui.backend.BackendImage;

import java.nio.ByteBuffer;
import java.util.List;

public interface IGUIBackend {

    /**
     * Renders the given elements.
     *
     * @param elements The elements to be drawn.
     */
    void render(List<GUIElement> elements, boolean updateNextFrame);

    void resize(int width, int height);

    /**
     * Load an image from the given {@link Assets.Images} enum
     *
     * @param image The image to load
     * @return The loaded image
     */
    BackendImage loadImage(Assets.Images image);

    /**
     * Load an image from the given bitmap data.
     *
     * <p>The bitmap data is expected to be uncompressed. The expected formats are:
     *
     * <ul>
     *   <li>1 channel: grayscale / alpha
     *   <li>2 channels: grayscale + alpha
     *   <li>3 channels: RGB
     *   <li>4 channels: RGBA
     * </ul>
     *
     * <p>This method may not check if the same image was already loaded. It is up to the caller to
     * check if the image was already loaded. Also note that the image should be freed using {@link
     * BackendImage#free()} after it is no longer used.
     *
     * @param bitmap The bitmap data
     * @param width The width of the image
     * @param height The height of the image
     * @param channels The number of channels of the image
     * @return The loaded image
     */
    default BackendImage loadImageFromBitmap(
            ByteBuffer bitmap, int width, int height, int channels) {
        return loadImageFromBitmap(bitmap.array(), width, height, channels);
    }

    /**
     * Load an image from the given bitmap data.
     *
     * <p>The bitmap data is expected to be uncompressed. The expected formats are:
     *
     * <ul>
     *   <li>1 channel: grayscale / alpha
     *   <li>2 channels: grayscale + alpha
     *   <li>3 channels: RGB
     *   <li>4 channels: RGBA
     * </ul>
     *
     * <p>This method may not check if the same image was already loaded. It is up to the caller to
     * check if the image was already loaded. Also note that the image should be freed using {@link
     * BackendImage#free()} after it is no longer used.
     *
     * @param bitmap The bitmap data
     * @param width The width of the image
     * @param height The height of the image
     * @param channels The number of channels of the image
     * @return The loaded image
     */
    BackendImage loadImageFromBitmap(byte[] bitmap, int width, int height, int channels);
}
