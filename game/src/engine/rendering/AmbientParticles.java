package engine.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import engine.level.Tile;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Rectangle;
import java.util.List;

/**
 * Purely visual particles, one art pixel each: dust motes hanging in the air and dust kicked up by
 * walking feet, both lit by the lighting pass, and embers rising from flames, which glow on their
 * own. Kicked-up dust is drawn with the floor, the rest over the scene.
 */
final class AmbientParticles {

  private static final int DUST = 110;
  private static final float EMBERS_PER_FLAME_PER_SECOND = 5f;
  private static final float PUFFS_PER_SECOND = 9f;
  private static final float PIXEL = 1 / 16f;

  private final float[] dustX = new float[DUST];
  private final float[] dustY = new float[DUST];
  private final float[] dustPhase = new float[DUST];
  private boolean dustPlaced = false;

  private final Pool embers = new Pool(320);
  private final Pool puffs = new Pool(240);
  private float emberBudget = 0f;

  /**
   * Advances all particles and spawns embers at the given flames.
   *
   * @param delta seconds since the last frame
   * @param time seconds since start
   * @param view visible world rectangle
   * @param flames flame tips that emit embers
   */
  void update(float delta, float time, Rectangle view, List<Point> flames) {
    delta = Math.min(delta, 0.1f);
    updateDust(delta, time, view);
    embers.update(delta, time, 0.9f);
    puffs.update(delta, time, 0f);

    if (flames.isEmpty()) return;
    emberBudget += flames.size() * EMBERS_PER_FLAME_PER_SECOND * delta;
    for (; emberBudget >= 1f; emberBudget--) {
      Point f = flames.get(MathUtils.random(flames.size() - 1));
      embers.add(
          f.x() + MathUtils.random(-2, 2) * PIXEL,
          f.y(),
          MathUtils.random(-0.25f, 0.25f),
          MathUtils.random(0.5f, 1.2f),
          MathUtils.random(0.5f, 1.4f));
    }
  }

  /**
   * Kicks up dust at the feet of a walking entity, at a steady rate while it moves.
   *
   * @param feet the point between the entity's feet
   * @param delta seconds since the last frame
   */
  void step(Point feet, float delta) {
    if (!MathUtils.randomBoolean(Math.min(1f, delta * PUFFS_PER_SECOND))) return;
    for (int i = 0; i < 2; i++) {
      puffs.add(
          feet.x() + MathUtils.random(-3, 3) * PIXEL,
          feet.y() + MathUtils.random(0, 1) * PIXEL,
          MathUtils.random(-0.3f, 0.3f),
          MathUtils.random(0.15f, 0.4f),
          MathUtils.random(0.3f, 0.6f));
    }
  }

  /**
   * Draws the dust motes hanging over walkable ground. They are pale and meant to be lit by the
   * lighting pass afterwards.
   *
   * @param batch batch with the world projection, already begun
   * @param layout the level layout, may be empty
   * @param time seconds since start
   */
  void drawDust(SpriteBatch batch, Tile[][] layout, float time) {
    Texture px = RenderTextures.pixel();
    for (int i = 0; i < DUST; i++) {
      if (!overGround(layout, dustX[i], dustY[i])) continue;
      float a = 0.35f + 0.3f * MathUtils.sin(time * 1.3f + dustPhase[i] * 7f);
      batch.setColor(a, a * 0.97f, a * 0.9f, a);
      batch.draw(px, snap(dustX[i]), snap(dustY[i]), PIXEL, PIXEL);
    }
  }

  /**
   * Draws the dust kicked up by walking feet. It lies on the floor, so entities drawn afterwards
   * cover it; like the motes it is lit by the lighting pass.
   *
   * @param batch batch with the world projection, already begun
   */
  void drawPuffs(SpriteBatch batch) {
    Texture px = RenderTextures.pixel();
    for (int i = 0; i < puffs.count; i++) {
      float t = puffs.life[i] / puffs.maxLife[i];
      float a = 0.7f * t;
      // Fresh puffs are two art pixels wide and shrink to one as they fade.
      float size = t > 0.5f ? 2 * PIXEL : PIXEL;
      batch.setColor(a * 0.8f, a * 0.74f, a * 0.66f, a);
      batch.draw(px, snap(puffs.x[i]), snap(puffs.y[i]), size, size);
    }
  }

  /**
   * Draws a flickering glow around each flame and the embers, both additively; embers cool from
   * yellow to red as they rise.
   *
   * @param batch batch with the world projection and additive blending, already begun
   * @param flames flame tips of this frame
   * @param time seconds since start
   */
  void drawEmbers(SpriteBatch batch, List<Point> flames, float time) {
    Texture glow = RenderTextures.glow();
    for (Point f : flames) {
      float a = 0.22f + 0.05f * Light.wobble(f, time);
      batch.setColor(a, a * 0.55f, a * 0.2f, 1f);
      batch.draw(glow, f.x() - 0.8f, f.y() - 0.95f, 1.6f, 1.6f);
    }
    Texture px = RenderTextures.pixel();
    for (int i = 0; i < embers.count; i++) {
      float t = embers.life[i] / embers.maxLife[i];
      float fade = Math.min(1f, t * 2.5f);
      batch.setColor(fade, (0.25f + 0.65f * t) * fade, 0.1f * t * fade, 1f);
      batch.draw(px, snap(embers.x[i]), snap(embers.y[i]), PIXEL, PIXEL);
    }
  }

  private void updateDust(float delta, float time, Rectangle view) {
    for (int i = 0; i < DUST; i++) {
      boolean outside =
          dustX[i] < view.x() - 1
              || dustX[i] > view.x() + view.width() + 1
              || dustY[i] < view.y() - 1
              || dustY[i] > view.y() + view.height() + 1;
      if (!dustPlaced || outside) {
        dustX[i] = view.x() + MathUtils.random(view.width());
        dustY[i] = view.y() + MathUtils.random(view.height());
        dustPhase[i] = MathUtils.random(MathUtils.PI2);
      }
      dustX[i] += (0.12f + 0.1f * MathUtils.sin(time * 0.4f + dustPhase[i])) * delta;
      dustY[i] += 0.08f * MathUtils.sin(time * 0.7f + dustPhase[i] * 3f) * delta;
    }
    dustPlaced = true;
  }

  private static boolean overGround(Tile[][] layout, float x, float y) {
    int tx = MathUtils.floor(x);
    int ty = MathUtils.floor(y);
    if (ty < 0 || ty >= layout.length || tx < 0 || tx >= layout[ty].length) return false;
    Tile t = layout[ty][tx];
    return t != null && t.levelElement() != LevelElement.SKIP && t.visible();
  }

  private static float snap(float v) {
    return MathUtils.floor(v * 16f) / 16f;
  }

  // Fixed-size particle storage; particles move linearly and sway sideways while they live.
  private static final class Pool {
    final float[] x;
    final float[] y;
    final float[] vx;
    final float[] vy;
    final float[] life;
    final float[] maxLife;
    int count = 0;

    Pool(int capacity) {
      x = new float[capacity];
      y = new float[capacity];
      vx = new float[capacity];
      vy = new float[capacity];
      life = new float[capacity];
      maxLife = new float[capacity];
    }

    void add(float px, float py, float pvx, float pvy, float lifetime) {
      if (count == x.length) return;
      int i = count++;
      x[i] = px;
      y[i] = py;
      vx[i] = pvx;
      vy[i] = pvy;
      life[i] = maxLife[i] = lifetime;
    }

    void update(float delta, float time, float sway) {
      for (int i = count - 1; i >= 0; i--) {
        life[i] -= delta;
        if (life[i] <= 0) {
          remove(i);
          continue;
        }
        vx[i] += MathUtils.sin(time * 6f + i) * sway * delta;
        x[i] += vx[i] * delta;
        y[i] += vy[i] * delta;
      }
    }

    private void remove(int i) {
      int last = --count;
      x[i] = x[last];
      y[i] = y[last];
      vx[i] = vx[last];
      vy[i] = vy[last];
      life[i] = life[last];
      maxLife[i] = maxLife[last];
    }
  }
}
