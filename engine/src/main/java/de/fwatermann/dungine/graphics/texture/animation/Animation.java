package de.fwatermann.dungine.graphics.texture.animation;

import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.shader.ShaderProgramConfiguration;
import de.fwatermann.dungine.utils.annotations.Nullable;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;
import org.lwjgl.opengl.GL33;

/** Abstract class representing an animation. */
public abstract class Animation {

  // The default texture unit to use if none is specified.
  public static int DEFAULT_TEXTURE_UNIT = GL33.GL_TEXTURE10;

  // the default duration of each frame in milliseconds.
  public static final long DEFAULT_FRAME_DURATION = 1000;

  private boolean loop = true;
  private boolean paused = false;
  private boolean blend = false;
  private long frameDuration = DEFAULT_FRAME_DURATION;

  // Function to be called when the animation finishes.
  @Nullable protected IVoidFunction1P<Animation> onAnimationFinish;

  /**
   * Get the current AnimationStep.
   *
   * @return the current AnimationStep.
   */
  protected abstract AnimationStep currentAnimationStep();

  /**
   * Bind the animation to the given shader using the default animation slot and default texture
   * unit.
   *
   * @param shader the shader program to bind the animation to.
   */
  public void bind(ShaderProgram shader) {
    this.bind(shader, AnimationSlot.ANIMATION_0, DEFAULT_TEXTURE_UNIT);
  }

  /**
   * Bind the animation to the given shader using the default animation slot and the provided
   * texture unit.
   *
   * @param shader the shader program to bind the animation to.
   * @param textureUnit texture unit of
   */
  public void bind(ShaderProgram shader, int textureUnit) {
    this.bind(shader, AnimationSlot.ANIMATION_0, textureUnit);
  }

  /**
   * Binds the animation to the given shader using the default texture unit and the provided
   * animation slot.
   *
   * @param shader the shader program to bind the animation to.
   * @param slot Slot of animation if multiple animations are required for one call.
   */
  public void bind(ShaderProgram shader, AnimationSlot slot) {
    this.bind(shader, slot, DEFAULT_TEXTURE_UNIT);
  }

  /**
   * Binds the animation to the given shader using the specified texture unit.
   *
   * @param shader the shader program to bind the animation to.
   * @param slot Slot of animation if multiple animations are required for one call.
   * @param textureUnit the texture unit to use for binding.
   */
  public void bind(ShaderProgram shader, AnimationSlot slot, int textureUnit) {
    AnimationStep step = this.currentAnimationStep();
    ShaderProgramConfiguration config = shader.configuration();

    boolean sameTexture =
        step.currentFrame().texture().glHandle() == step.nextFrame().texture().glHandle();

    String uniform = String.format(config.uniformAnimation, slot.ordinal());

    // CurrentFrame
    shader.setUniform1i(uniform + ".currentFrame.frameTexture", textureUnit - GL33.GL_TEXTURE0);
    shader.setUniform2i(uniform + ".currentFrame.position", step.currentFrame().position());
    shader.setUniform2i(uniform + ".currentFrame.size", step.currentFrame().size());

    // NextFrame
    shader.setUniform1i(
        uniform + ".nextFrame.frameTexture",
        textureUnit + (sameTexture ? 0 : 1) - GL33.GL_TEXTURE0);
    shader.setUniform2i(uniform + ".nextFrame.position", step.nextFrame().position());
    shader.setUniform2i(uniform + ".nextFrame.size", step.nextFrame().size());

    // Blend factor
    shader.setUniform1f(uniform + ".blendFactor", step.blendFactor());

    // CurrentFrame texture
    GL33.glActiveTexture(textureUnit);
    step.currentFrame().texture().bind(textureUnit);

    if (sameTexture) {
      return;
    }

    // NextFrame texture
    GL33.glActiveTexture(textureUnit + 1);
    step.nextFrame().texture().bind(textureUnit + 1);
  }

  /**
   * Checks if the animation is set to loop.
   *
   * @return true if the animation loops, false otherwise.
   */
  public boolean loop() {
    return this.loop;
  }

  /**
   * Sets whether the animation should loop.
   *
   * @param loop true to loop the animation, false otherwise.
   * @return the current Animation instance.
   */
  public Animation loop(boolean loop) {
    this.loop = loop;
    return this;
  }

  /**
   * Checks if the animation is paused.
   *
   * @return true if the animation is paused, false otherwise.
   */
  public boolean paused() {
    return this.paused;
  }

  /**
   * Sets whether the animation should be paused.
   *
   * @param paused true to pause the animation, false otherwise.
   * @return the current Animation instance.
   */
  public Animation paused(boolean paused) {
    this.paused = paused;
    return this;
  }

  /**
   * Sets the function to be called when the animation finishes.
   *
   * @param onAnimationFinish the function to call when the animation finishes.
   * @return the current Animation instance.
   */
  public Animation onAnimationFinish(IVoidFunction1P<Animation> onAnimationFinish) {
    this.onAnimationFinish = onAnimationFinish;
    return this;
  }

  /**
   * Checks if the current frame and the next frame are blended together.
   *
   * @return true if blending is enabled, false otherwise.
   */
  public boolean blend() {
    return this.blend;
  }

  /**
   * Sets whether the current frame and the next frame should be blended together.
   *
   * @param blend true to enable blending, false to disable.
   * @return the current Animation instance.
   */
  public Animation blend(boolean blend) {
    this.blend = blend;
    return this;
  }

  /**
   * Gets the duration of each frame in milliseconds.
   *
   * @return the duration of each frame in milliseconds.
   */
  public long frameDuration() {
    return this.frameDuration;
  }

  /**
   * Sets the duration of each frame in milliseconds.
   *
   * @param frameDuration the duration of each frame in milliseconds.
   * @return the current Animation instance.
   */
  public Animation frameDuration(long frameDuration) {
    this.frameDuration = frameDuration;
    return this;
  }

  public abstract Animation currentFrame(int frameIndex);

  public abstract int currentFrame();

  public enum AnimationSlot {
    ANIMATION_0,
    ANIMATION_1,
    ANIMATION_2,
    ANIMATION_3,
    ANIMATION_4,
    ANIMATION_5,
    ANIMATION_6,
    ANIMATION_7,
    ANIMATION_8,
    ANIMATION_9,
    ANIMATION_10,
    ANIMATION_11,
    ANIMATION_12,
    ANIMATION_13,
    ANIMATION_14,
    ANIMATION_15;

    public static AnimationSlot fromIndex(int index) {
      return AnimationSlot.values()[index];
    }

  }
}
