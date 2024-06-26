package de.fwatermann.dungine.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import org.lwjgl.stb.STBImage;

/**
 * The TextureManager class is a singleton that manages the loading and caching of textures. It
 * provides methods to load textures from resources and files, and to remove textures from the
 * cache.
 */
public class TextureManager {

  private static TextureManager instance;

  /**
   * Returns the singleton instance of the TextureManager class. If the instance does not exist, it
   * is created.
   *
   * @return the singleton instance of the TextureManager class
   */
  public static TextureManager instance() {
    if (instance == null) {
      instance = new TextureManager();
    }
    return instance;
  }

  private final HashMap<String, Texture> resourceCache = new HashMap<>();

  private final HashMap<String, Texture> fileCache = new HashMap<>();

  private TextureManager() {}

  /**
   * Loads a texture from a resource and caches it. If the texture has already been loaded, the
   * cached texture is returned.
   *
   * @param resourcePath the path of the resource
   * @return the loaded texture
   * @throws RuntimeException if the resource is not found, if the image fails to load, or if an I/O
   *     error occurs
   */
  public Texture textureFromResource(String resourcePath) {
    return this.resourceCache.computeIfAbsent(
        resourcePath,
        p -> {
          try {
            InputStream is = TextureManager.class.getResourceAsStream(p);
            if (is == null) {
              throw new RuntimeException("Resource not found: " + p);
            }
            byte[] data = is.readAllBytes();
            is.close();

            int[] width = new int[1];
            int[] height = new int[1];
            int[] channels = new int[1];

            ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
            buffer.put(data);
            buffer.position(0);

            ByteBuffer pixels = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4);
            if (pixels == null) {
              throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            return new Texture(width[0], height[0], pixels);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  /**
   * Loads a texture from a file and caches it. If the texture has already been loaded, the cached
   * texture is returned.
   *
   * @param path the path of the texture
   * @return the loaded texture
   * @throws RuntimeException if the image fails to load or if an I/O error occurs
   */
  public Texture textureFromFile(String path) {
    return this.fileCache.computeIfAbsent(
        path,
        p -> {
          try {
            byte[] data = Files.readAllBytes(java.nio.file.Paths.get(p));

            int[] width = new int[1];
            int[] height = new int[1];
            int[] channels = new int[1];

            ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
            buffer.put(data);
            buffer.position(0);

            ByteBuffer pixels = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4);
            if (pixels == null) {
              throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            return new Texture(width[0], height[0], pixels);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  /**
   * Removes the texture from the file cache.
   *
   * @param path the path of the texture
   * @return the removed texture, or null if the texture was not in the cache
   */
  public Texture removeFromFileCache(String path) {
    return this.fileCache.remove(path);
  }

  /**
   * Removes the texture from the resource cache.
   *
   * @param path the path of the texture
   * @return the removed texture, or null if the texture was not in the cache
   */
  public Texture removeFromResourceCache(String path) {
    return this.resourceCache.remove(path);
  }
}
