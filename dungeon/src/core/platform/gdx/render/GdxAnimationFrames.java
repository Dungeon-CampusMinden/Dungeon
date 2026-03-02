package core.platform.gdx.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.AnimationFrame;

/** Utility to resolve engine-agnostic {@link AnimationFrame}s into libGDX drawables. */
public final class GdxAnimationFrames {
  private GdxAnimationFrames() {}

  public static TextureRegion toRegion(final AnimationFrame frame) {
    return toSprite(frame);
  }

  public static Sprite toSprite(final AnimationFrame frame) {
    if (frame == null) return new Sprite();

    Object cached = frame.backendHandle();
    if (cached instanceof Sprite s) {
      s.setFlip(frame.flipX(), false);
      return s;
    }

    Texture tex = TextureMap.instance().textureAt(frame.texturePath());
    Sprite out;

    if (tex == null) {
      out = new Sprite();
    } else if (frame.hasRegion()) {
      out =
        new Sprite(
          new TextureRegion(tex, frame.regionX(), frame.regionY(), frame.regionW(), frame.regionH()));
    } else {
      out = new Sprite(tex);
    }

    out.setFlip(frame.flipX(), false);
    frame.backendHandle(out);
    return out;
  }
}
