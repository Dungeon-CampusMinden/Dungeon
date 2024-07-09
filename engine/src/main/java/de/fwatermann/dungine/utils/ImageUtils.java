package de.fwatermann.dungine.utils;

import de.fwatermann.dungine.resource.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.lwjgl.stb.STBImage;

public class ImageUtils {

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

  public record ImageInfo(int width, int height, int channels) {}

}
