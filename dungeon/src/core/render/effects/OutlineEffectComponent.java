package core.render.effects;

import core.Component;
import java.awt.Color;
import java.util.Objects;

/**
 * LITIENGINE-only sprite outline effect.
 *
 * <p>This mirrors the old OutlineShader semantics for the AWT/Graphics2D render path.
 */
public final class OutlineEffectComponent implements Component {

  private int width;
  private Color color;
  private float beatSpeed;
  private float beatIntensity;
  private boolean rainbow;

  public OutlineEffectComponent(
    final int width, final Color color, final float beatSpeed, final float beatIntensity) {
    this.width = Math.max(1, width);
    this.color = Objects.requireNonNull(color, "color");
    this.beatSpeed = beatSpeed;
    this.beatIntensity = Math.max(0f, beatIntensity);
    this.rainbow = false;
  }

  public OutlineEffectComponent(final int width, final Color color) {
    this(width, color, 1.0f, 0f);
  }

  public OutlineEffectComponent(final int width) {
    this(width, Color.WHITE);
  }

  public int width() {
    return width;
  }

  public OutlineEffectComponent width(final int width) {
    this.width = Math.max(1, width);
    return this;
  }

  public Color color() {
    return color;
  }

  public OutlineEffectComponent color(final Color color) {
    this.color = Objects.requireNonNull(color, "color");
    return this;
  }

  public float beatSpeed() {
    return beatSpeed;
  }

  public OutlineEffectComponent beatSpeed(final float beatSpeed) {
    this.beatSpeed = beatSpeed;
    return this;
  }

  public float beatIntensity() {
    return beatIntensity;
  }

  public OutlineEffectComponent beatIntensity(final float beatIntensity) {
    this.beatIntensity = Math.max(0f, beatIntensity);
    return this;
  }

  public boolean rainbow() {
    return rainbow;
  }

  public OutlineEffectComponent rainbow(final boolean rainbow) {
    this.rainbow = rainbow;
    return this;
  }
}
