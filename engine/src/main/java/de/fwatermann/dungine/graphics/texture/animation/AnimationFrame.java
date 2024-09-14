package de.fwatermann.dungine.graphics.texture.animation;

import de.fwatermann.dungine.graphics.texture.Texture;
import org.joml.Vector2i;

public class AnimationFrame {

  private final Texture texture;
  private final Vector2i size;
  private final Vector2i position;

  public AnimationFrame(Texture texture, Vector2i size, Vector2i position) {
    this.texture = texture;
    this.size = size;
    this.position = position;
  }

  public Texture texture() {
    return this.texture;
  }

  public Vector2i size() {
    return this.size;
  }

  public Vector2i position() {
    return this.position;
  }
}
