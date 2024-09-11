package de.fwatermann.dungine.graphics.texture;

import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;

/**
 * The TextureManager class is a singleton that manages the loading and caching of textures. It
 * provides methods to load textures from resources and files, and to remove textures from the
 * cache.
 */
public class TextureManager {

  private static final Logger LOGGER = LogManager.getLogger(TextureManager.class);

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

  private final ReferenceQueue<Texture> refQueue = new ReferenceQueue<>();
  private final Map<Resource, WeakReference<Texture>> resourceCache = new HashMap<>();
  private final Map<WeakReference<Texture>, Integer> refTextureHandle = new HashMap<>();

  private TextureManager() {}

  /**
   * Loads a texture from a resource and caches it. If the texture has already been loaded, the
   * cached texture is returned.
   *
   * @param resource the Resource that contains the texture data
   * @return the loaded texture
   * @throws RuntimeException if the image fails to load or if an I/O error occurs
   */
  public Texture load(Resource resource) {
    WeakReference<Texture> texRef = this.resourceCache.get(resource);
    if(texRef != null && texRef.get() != null) {
      return texRef.get();
    }
    Texture texture;
    try {
      texture = loadTexture(resource.readBytes());
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    }
    texRef = new WeakReference<>(texture, this.refQueue);
    this.resourceCache.put(resource, texRef);
    this.refTextureHandle.put(texRef, texture.glHandle());
    return texture;
  }

  /**
   * Loads a texture from a resource and caches it. If the texture has already been loaded, the
   * cached texture is returned.
   *
   * @param resource the Resource that contains the texture data
   * @param cached an array to store whether the texture was cached
   * @return the loaded texture
   */
  public Texture load(Resource resource, boolean[] cached) {
    cached[0] = this.resourceCache.containsKey(resource);
    return this.load(resource);
  }

  private static Texture loadTexture(ByteBuffer buffer) {
    GLUtils.checkBuffer(buffer);
    int[] width = new int[1];
    int[] height = new int[1];
    int[] channels = new int[1];
    ByteBuffer pixels = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4);
    if (pixels == null) {
      throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
    }
    Texture ret = new Texture(width[0], height[0], pixels);
    STBImage.stbi_image_free(pixels);
    return ret;
  }

  /**
   * Removes the texture from the cache.
   * @param texture the texture to remove
   */
  protected void removeFromCache(Texture texture) {
    this.resourceCache.values().removeIf(t -> t != null && t.get() == texture);
  }

  public void collectGarbage() {
    if(!ThreadUtils.isMainThread()) return;
    Reference<? extends Texture> texRef;
    boolean collected = false;
    while((texRef = this.refQueue.poll()) != null) {
      if(this.refTextureHandle.containsKey(texRef)) {
        int handle = this.refTextureHandle.get(texRef);
        GL33.glDeleteTextures(handle);
        this.refTextureHandle.remove(texRef);
      }
      Texture texture = texRef.get();
      if(texture != null) {
        texture.dispose();
      }
      collected = true;
    }

    if(collected) {
      this.resourceCache.entrySet().removeIf(e -> {
        if(e.getValue().get() == null) {
          LOGGER.trace("Removing Texture from cache: {}", e.getKey());
          return true;
        }
        return false;
      });
    }

  }

}
