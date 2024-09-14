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

  private int currentFrame = 0;
  private long lastFrameTime = System.currentTimeMillis();

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
    ArrayAnimation animation = new ArrayAnimation();
    animation.loadFromMultipleResources(magFilter, minFilter, wrapMode, resources);
    return animation;
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

  private ArrayAnimation() {}

  /**
   * Loads textures from multiple resources and applies the specified texture filters and wrap mode.
   *
   * @param magFilter the magnification filter to be applied to the textures
   * @param minFilter the minification filter to be applied to the textures
   * @param wrapMode the wrap mode to be applied to the textures
   * @param resources the resources to load the textures from
   * @throws IllegalArgumentException if no resources or no valid resources are provided
   */
  private void loadFromMultipleResources(
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

    this.frames = new Texture[numFrames];
    for (int i = 0; i < resources.length; i++) {
      Resource resource = resources[i];
      if (resource == null) continue;
      Texture texture = TextureManager.load(resource);
      texture.minFilter(minFilter).magFilter(magFilter).wrapS(wrapMode).wrapT(wrapMode);
      this.frames[i] = texture;
    }
  }

  /**
   * Returns the current animation step, updating it if necessary.
   *
   * @return the current `AnimationStep`
   */
  @Override
  protected AnimationStep currentAnimationStep() {
    if (this.currentAnimationStep == null) {
      this.updateAnimationStep();
      return this.currentAnimationStep;
    }
    if (this.paused()) return this.currentAnimationStep;

    long diff = System.currentTimeMillis() - this.lastFrameTime;

    if (diff > this.frameDuration()) {
      int newFrame =
          (this.currentFrame + (int) diff / (int) this.frameDuration()) % this.frames.length;
      if (newFrame < this.currentFrame && !this.loop()) {
        if (this.onAnimationFinish != null) this.onAnimationFinish.run(this);
        return this.currentAnimationStep;
      }
      this.currentFrame = newFrame;
      this.lastFrameTime = System.currentTimeMillis();
      this.updateAnimationStep();
    }
    if (this.blend()) {
      if (!this.loop() && this.currentFrame == this.frames.length - 1) {
        this.currentAnimationStep.blendFactor(0.0f);
      } else {
        this.currentAnimationStep.blendFactor(diff / (float) this.frameDuration());
      }
    } else {
      this.currentAnimationStep.blendFactor(0.0f);
    }

    return this.currentAnimationStep;
  }

  /**
   * Creates an `AnimationFrame` for the specified frame index.
   *
   * @param pIndex the index of the frame
   * @return the created `AnimationFrame`
   */
  private AnimationFrame makeAnimationFrame(int pIndex) {
    int index = pIndex % this.frames.length;
    Texture texture = this.frames[index];
    return new AnimationFrame(
        texture, new Vector2i(texture.width(), texture.height()), new Vector2i(0));
  }

  /** Updates the current animation step based on the current frame. */
  private void updateAnimationStep() {
    this.currentAnimationStep =
        new AnimationStep(
            this.makeAnimationFrame(this.currentFrame),
            this.makeAnimationFrame(this.currentFrame + 1),
            0.0f);
  }

  /**
   * Returns the index of the current frame.
   *
   * @return the current frame index
   */
  public int currentFrame() {
    return this.currentFrame;
  }

  /**
   * Sets the index of the current frame and updates the animation step.
   *
   * @param currentFrame the current frame index
   * @return the `ArrayAnimation` instance
   */
  public ArrayAnimation currentFrame(int currentFrame) {
    this.currentFrame = currentFrame;
    this.updateAnimationStep();
    return this;
  }
}
