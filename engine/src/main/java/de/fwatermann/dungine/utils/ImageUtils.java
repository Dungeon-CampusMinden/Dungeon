package de.fwatermann.dungine.utils;

import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.stb.STBImage;

/**
 * The `ImageUtils` class provides utility methods for working with images. It includes methods for
 * retrieving image size and other properties from a given resource.
 */
public class ImageUtils {

  private ImageUtils() {}

  /**
   * Retrieves the size and channel information of an image from the specified resource.
   *
   * @param resource the resource containing the image data
   * @return an `ImageInfo` object containing the width, height, and number of channels of the image
   * @throws RuntimeException if an I/O error occurs while reading the image data
   */
  public static ImageInfo getImageSize(Resource resource) {
    try {
      ByteBuffer data = resource.readBytes();
      int[] width = new int[1];
      int[] height = new int[1];
      int[] channels = new int[1];
      STBImage.stbi_info_from_memory(data, width, height, channels);
      return new ImageInfo(width[0], height[0], channels[0]);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The `ImageInfo` record holds information about an image's width, height, and number of
   * channels.
   *
   * @param width the width of the image
   * @param height the height of the image
   * @param channels the number of channels in the image
   */
  public record ImageInfo(int width, int height, int channels) {}
}
