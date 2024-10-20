package de.fwatermann.dungine.graphics.texture.animation;

import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.shader.ShaderProgramConfiguration;
import de.fwatermann.dungine.utils.annotations.Nullable;
import de.fwatermann.dungine.utils.functions.IVoidFunction1P;
import org.lwjgl.opengl.GL33;

/** Abstract class representing an animation. */
public abstract class Animation {

  /** The default texture unit to use if none is specified. */
  public static int DEFAULT_TEXTURE_UNIT = GL33.GL_TEXTURE10;

  /** the default duration of each frame in milliseconds. */
  public static final long DEFAULT_FRAME_DURATION = 1000;

  private final int frameCount;
  private boolean loop = true;
  private boolean paused = false;
  private boolean blend = false;
  private long frameDuration = DEFAULT_FRAME_DURATION;
  private LoopMode loopMode = LoopMode.REPEAT;

  private AnimationStep currentStep;
  private long lastFrameTime = System.currentTimeMillis();
  private int currentFrame = 0;
  private int nextFrame = 0;
  private int step = 1;

  /** Function to be called when the animation finishes. */
  @Nullable protected IVoidFunction1P<Animation> onAnimationFinish;

  /**
   * Creates a new Animation with the specified number of frames.
   *
   * @param frameCount the number of frames in the animation.
   */
  protected Animation(int frameCount) {
    this.frameCount = frameCount;
  }

  /**
   * Create AnimationFrame based on index
   * @param index index of the frame
   * @return AnimationFrame
   */
  protected abstract AnimationFrame makeFrame(int index);

  private void makeCurrentStep() {
    this.currentStep = new AnimationStep(
      this.makeFrame(this.currentFrame),
      this.makeFrame(this.nextFrame),
      0.0f
    );
  }

  /**
   * Advances the current Frame if needed based on the time since last frame update.
   */
  private void update() {
    if(this.currentStep == null) {
      this.makeCurrentStep();
      return;
    }
    long diff = System.currentTimeMillis() - this.lastFrameTime;
    if(diff > this.frameDuration) {
      int frames = (int) (diff / this.frameDuration) * this.step;
      boolean[] requireReverse = new boolean[] {false};
      int newFrame = this.clampFrameIndexRespectingLoopMode(this.currentFrame + frames, requireReverse);
      if(newFrame != this.currentFrame) {
        if(!this.loop && newFrame == this.frameCount - 1 && this.onAnimationFinish != null) {
          this.onAnimationFinish.run(this);
        }
        this.currentFrame = newFrame;
        if(requireReverse[0]) this.step *= -1;
        this.nextFrame = this.clampFrameIndexRespectingLoopMode(this.currentFrame + this.step, null);
        this.makeCurrentStep();
      }
      this.lastFrameTime = System.currentTimeMillis();
    }

    if(this.blend) {
      if(!this.loop && this.currentFrame == this.frameCount - 1) {
        this.currentStep.blendFactor(0.0f);
      } else {
        this.currentStep.blendFactor(diff / (float) this.frameDuration);
      }
    } else {
      this.currentStep.blendFactor(0.0f);
    }
  }

  private int clampFrameIndexRespectingLoopMode(int index, boolean[] requireReverse) {
    int frame = index;
    int clamped = Math.min(Math.max(0, frame), this.frameCount - 1);
    int overshoot = Math.abs(clamped - frame);
    if(this.loop) {
      if(this.loopMode == LoopMode.REPEAT) {
        if(frame < 0) frame = this.frameCount - (overshoot % this.frameCount);
        else if(frame > this.frameCount - 1) frame = overshoot % this.frameCount;
      } else if(this.loopMode == LoopMode.PING_PONG) {
        if(frame < 0) frame = overshoot % this.frameCount;
        else if(frame > this.frameCount - 1) frame = this.frameCount - 1 - (overshoot % this.frameCount);
      }
    }
    if(requireReverse != null) requireReverse[0] = overshoot != 0;
    return Math.min(Math.max(0, frame), this.frameCount - 1);
  }

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
    this.update();
    AnimationStep step = this.currentStep;
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

  /**
   * Gets the loop mode of the animation.
   *
   * @return the loop mode of the animation.
   */
  public LoopMode loopMode() {
    return this.loopMode;
  }

  /**
   * Sets the loop mode of the animation.
   *
   * @param loopMode the loop mode to set.
   * @return the current Animation instance.
   */
  public Animation loopMode(LoopMode loopMode) {
    this.loopMode = loopMode;
    return this;
  }

  /**
   * Get the current frame index.
   * @return the current frame index.
   */
  public int currentFrame() {
    return this.currentFrame;
  }

  /**
   * Sets the current frame index.
   * @param index the index of the frame to set.
   * @return the current Animation instance.
   */
  public Animation currentFrame(int index) {
    this.currentFrame = index % this.frameCount;
    this.makeCurrentStep();
    return this;
  }

  /** Enum representing the loop mode of an animation. */
  public enum LoopMode {
    /** The animation will repeat from the beginning when it reaches the end. */
    REPEAT,

    /** The animation will play in reverse when it reaches the end. */
    PING_PONG
  }


/**
 * Enum representing the animation slot to use when binding an animation to a shader.
 * Each slot corresponds to a specific animation that can be bound to a shader.
 */
public enum AnimationSlot {
  /** Animation slot 0. */
  ANIMATION_0,

  /** Animation slot 1. */
  ANIMATION_1,

  /** Animation slot 2. */
  ANIMATION_2,

  /** Animation slot 3. */
  ANIMATION_3,

  /** Animation slot 4. */
  ANIMATION_4,

  /** Animation slot 5. */
  ANIMATION_5,

  /** Animation slot 6. */
  ANIMATION_6,

  /** Animation slot 7. */
  ANIMATION_7,

  /** Animation slot 8. */
  ANIMATION_8,

  /** Animation slot 9. */
  ANIMATION_9,

  /** Animation slot 10. */
  ANIMATION_10,

  /** Animation slot 11. */
  ANIMATION_11,

  /** Animation slot 12. */
  ANIMATION_12,

  /** Animation slot 13. */
  ANIMATION_13,

  /** Animation slot 14. */
  ANIMATION_14,

  /** Animation slot 15. */
  ANIMATION_15;

  /**
   * Returns the AnimationSlot corresponding to the given index.
   *
   * @param index the index of the animation slot
   * @return the AnimationSlot corresponding to the given index
   */
  public static AnimationSlot fromIndex(int index) {
    return AnimationSlot.values()[index];
  }
}
}
