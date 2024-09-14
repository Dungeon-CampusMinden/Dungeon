package de.fwatermann.dungine.graphics.texture.animation;

public class AnimationStep {

  private AnimationFrame currentFrame;
  private AnimationFrame nextFrame;
  private float blendFactor;

  public AnimationStep(AnimationFrame currentFrame, AnimationFrame nextFrame, float blendFactor) {
    this.currentFrame = currentFrame;
    this.nextFrame = nextFrame;
    this.blendFactor = blendFactor;
  }

  public AnimationFrame currentFrame() {
    return this.currentFrame;
  }

  public AnimationFrame nextFrame() {
    return this.nextFrame;
  }

  public float blendFactor() {
    return this.blendFactor;
  }

  public AnimationStep currentFrame(AnimationFrame currentFrame) {
    this.currentFrame = currentFrame;
    return this;
  }

  public AnimationStep nextFrame(AnimationFrame nextFrame) {
    this.nextFrame = nextFrame;
    return this;
  }

  public AnimationStep blendFactor(float blendFactor) {
    this.blendFactor = blendFactor;
    return this;
  }
}
