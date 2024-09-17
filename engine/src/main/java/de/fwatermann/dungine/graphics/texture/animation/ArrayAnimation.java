package de.fwatermann.dungine.graphics.texture.animation;

import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.TextureMagFilter;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.graphics.texture.TextureMinFilter;
import de.fwatermann.dungine.graphics.texture.TextureWrapMode;
import de.fwatermann.dungine.resource.Resource;
import org.joml.Vector2i;

/**
 * The `ArrayAnimation` class represents an animation consisting of an array of textures. It
 * provides methods to load textures from resources, update the current animation step, and
 * configure animation properties such as frame duration and blending.
 */
public class ArrayAnimation extends Animation {

  private AnimationStep currentAnimationStep;

  private Texture[] frames;

  /**
   * Creates an `ArrayAnimation` instance from multiple resources with specified texture filters and
   * wrap mode.
   *
   * @param magFilter the magnification filter to be applied to the textures
   * @param minFilter the minification filter to be applied to the textures
   * @param wrapMode the wrap mode to be applied to the textures
   * @param resources the resources to load the textures from
   * @return the created `ArrayAnimation` instance
   */
  public static ArrayAnimation of(
      TextureMagFilter magFilter,
      TextureMinFilter minFilter,
      TextureWrapMode wrapMode,
      Resource... resources) {
    return loadFromMultipleResources(magFilter, minFilter, wrapMode, resources);
  }

  /**
   * Creates an `ArrayAnimation` instance from multiple resources with default texture filters and
   * wrap mode.
   *
   * @param resources the resources to load the textures from
   * @return the created `ArrayAnimation` instance
   */
  public static ArrayAnimation of(Resource... resources) {
    return of(
        TextureMagFilter.NEAREST,
        TextureMinFilter.LINEAR,
        TextureWrapMode.CLAMP_TO_EDGE,
        resources);
  }

  /**
   * Creates an `ArrayAnimation` instance from multiple textures.
   * @param textures the textures to create the animation from
   * @return the created `ArrayAnimation` instance
   */
  public static ArrayAnimation of(Texture... textures) {
    ArrayAnimation animation = new ArrayAnimation(textures.length);
    animation.frames = textures;
    return animation;
  }

  private ArrayAnimation(int count) {
    super(count);
  }

  /**
   * Loads textures from multiple resources and applies the specified texture filters and wrap mode.
   *
   * @param magFilter the magnification filter to be applied to the textures
   * @param minFilter the minification filter to be applied to the textures
   * @param wrapMode the wrap mode to be applied to the textures
   * @param resources the resources to load the textures from
   * @throws IllegalArgumentException if no resources or no valid resources are provided
   */
  private static ArrayAnimation loadFromMultipleResources(
      TextureMagFilter magFilter,
      TextureMinFilter minFilter,
      TextureWrapMode wrapMode,
      Resource... resources) {
    if (resources.length == 0) {
      throw new IllegalArgumentException("No resources provided.");
    }

    // Count the number of frames.
    int numFrames = 0;
    for (int i = 0; i < resources.length; i++) {
      Resource resource = resources[i];
      if (resource != null) {
        numFrames++;
      }
    }
    if (numFrames == 0) {
      throw new IllegalArgumentException("No valid resources provided.");
    }

    ArrayAnimation animation = new ArrayAnimation(numFrames);

    animation.frames = new Texture[numFrames];
    for (int i = 0; i < resources.length; i++) {
      Resource resource = resources[i];
      if (resource == null) continue;
      Texture texture = TextureManager.load(resource);
      texture.minFilter(minFilter).magFilter(magFilter).wrapS(wrapMode).wrapT(wrapMode);
      animation.frames[i] = texture;
    }

    return animation;
  }

  /**
   * Creates an `AnimationFrame` for the specified frame index.
   *
   * @param pIndex the index of the frame
   * @return the created `AnimationFrame`
   */
  @Override
  protected AnimationFrame makeFrame(int pIndex) {
    int index = pIndex % this.frames.length;
    Texture texture = this.frames[index];
    return new AnimationFrame(
        texture, new Vector2i(texture.width(), texture.height()), new Vector2i(0));
  }
}
