package core.platform.gdx.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * libGDX-only blending helpers.
 *
 * <p>All direct OpenGL calls must live in the GDX render package so other hosts
 * (e.g., LITIENGINE) can run without loading libGDX GL classes.
 */
public final class GdxBlendUtils {

  private GdxBlendUtils() {}

  /** Default blending used in this project (PMA Blending). */
  public static void setBlending() {
    setPMABlending();
  }

  /** Default blending used in this project (PMA Blending). */
  public static void setBlending(Object batch) {
    setPMABlending(batch);
  }

  /** Pre-Multiplied Alpha (PMA) blending via raw OpenGL state. */
  public static void setPMABlending() {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  /** Pre-Multiplied Alpha (PMA) blending on a SpriteBatch or via raw OpenGL if null. */
  public static void setPMABlending(Object batch) {
    if (batch == null) {
      setPMABlending();
      return;
    }
    if (!(batch instanceof SpriteBatch b)) {
      throw new IllegalArgumentException(
        "Expected SpriteBatch for blending, but got: " + batch.getClass());
    }
    b.enableBlending();
    b.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  /** Straight alpha blending via raw OpenGL state. */
  public static void setStraightAlphaBlending() {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  /** Straight alpha blending on a SpriteBatch or via raw OpenGL if null. */
  public static void setStraightAlphaBlending(Object batch) {
    if (batch == null) {
      setStraightAlphaBlending();
      return;
    }
    if (!(batch instanceof SpriteBatch b)) {
      throw new IllegalArgumentException(
        "Expected SpriteBatch for blending, but got: " + batch.getClass());
    }
    b.enableBlending();
    b.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }
}
