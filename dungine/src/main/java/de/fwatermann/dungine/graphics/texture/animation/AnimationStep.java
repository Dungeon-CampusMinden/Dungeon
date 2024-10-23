package de.fwatermann.dungine.graphics.texture.animation;

/**
 * The `AnimationStep` class represents a step in an animation sequence. It contains the current
 * frame, the next frame, and a blend factor to transition between frames.
 */
public class AnimationStep {

  private AnimationFrame currentFrame;
  private AnimationFrame nextFrame;
  private float blendFactor;

  /**
   * Constructs an `AnimationStep` with the specified current frame, next frame, and blend factor.
   *
   * @param currentFrame the current frame of the animation step.
   * @param nextFrame the next frame of the animation step.
   * @param blendFactor the blend factor for transitioning between frames.
   */
  public AnimationStep(AnimationFrame currentFrame, AnimationFrame nextFrame, float blendFactor) {
    this.currentFrame = currentFrame;
    this.nextFrame = nextFrame;
    this.blendFactor = blendFactor;
  }

  /**
   * Gets the current frame of the animation step.
   *
   * @return the current frame of the animation step.
   */
  public AnimationFrame currentFrame() {
    return this.currentFrame;
  }

  /**
   * Gets the next frame of the animation step.
   *
   * @return the next frame of the animation step.
   */
  public AnimationFrame nextFrame() {
    return this.nextFrame;
  }

  /**
   * Gets the blend factor for transitioning between frames.
   *
   * @return the blend factor for transitioning between frames.
   */
  public float blendFactor() {
    return this.blendFactor;
  }

  /**
   * Sets the current frame of the animation step.
   *
   * @param currentFrame the current frame to set.
   * @return the updated `AnimationStep` instance.
   */
  public AnimationStep currentFrame(AnimationFrame currentFrame) {
    this.currentFrame = currentFrame;
    return this;
  }

  /**
   * Sets the next frame of the animation step.
   *
   * @param nextFrame the next frame to set.
   * @return the updated `AnimationStep` instance.
   */
  public AnimationStep nextFrame(AnimationFrame nextFrame) {
    this.nextFrame = nextFrame;
    return this;
  }

  /**
   * Sets the blend factor for transitioning between frames.
   *
   * @param blendFactor the blend factor to set.
   * @return the updated `AnimationStep` instance.
   */
  public AnimationStep blendFactor(float blendFactor) {
    this.blendFactor = blendFactor;
    return this;
  }
}
