package feature.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import engine.Entity;
import engine.System;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.rendering.WorldOverlay;
import feature.components.BarDisplayable;
import feature.components.HealthComponent;
import feature.components.ManaComponent;
import feature.components.StaminaComponent;
import java.util.Comparator;
import java.util.List;

/**
 * A system that displays attribute bars above entities.
 *
 * <p>Entities with {@link BarDisplayable}, {@link PositionComponent}, and a visible {@link
 * DrawComponent} show one bar per attribute that is below its maximum. The bars are pixel art drawn
 * in the world over the lit scene: they sit on the sprite's art-pixel grid, scale with the zoom and
 * keep their colours in the dark. Visible bars stack upwards from the top of the sprite, ordered by
 * priority (lower priority = closer to entity).
 */
public final class AttributeBarSystem extends System implements WorldOverlay {

  private static final List<Class<? extends BarDisplayable>> ATTRIBUTES =
      List.of(HealthComponent.class, ManaComponent.class, StaminaComponent.class);
  // Bar size in art pixels, outline included; stacked bars share an outline row.
  private static final int WIDTH = 16;
  private static final int HEIGHT = 4;
  private static final Color OUTLINE = new Color(0.07f, 0.05f, 0.09f, 1f);

  private final Color scratch = new Color();

  /** Creates a new {@code AttributeBarSystem}. */
  public AttributeBarSystem() {
    super(AuthoritativeSide.CLIENT, DrawComponent.class, PositionComponent.class);
  }

  @Override
  public void execute() {}

  @Override
  public void drawOverlay(SpriteBatch batch) {
    Texture pixel = WorldOverlay.pixel();
    filteredEntityStream().forEach(entity -> drawBars(batch, pixel, entity));
  }

  private void drawBars(SpriteBatch batch, Texture pixel, Entity entity) {
    DrawComponent dc = entity.fetch(DrawComponent.class).orElseThrow();
    if (!dc.isVisible()) return;
    List<BarDisplayable> bars =
        ATTRIBUTES.stream()
            .flatMap(type -> entity.fetch(type).stream())
            .map(BarDisplayable.class::cast)
            .filter(bar -> bar.current() < bar.max())
            .sorted(Comparator.comparingInt(BarDisplayable::barPriority))
            .toList();
    if (bars.isEmpty()) return;

    // Offsets in whole art pixels from the sprite's corner keep the bars on its pixel grid.
    PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
    int spriteWidth = Math.round(dc.getWidth() * Math.abs(pc.scale().x()) / PIXEL);
    int spriteHeight = Math.round(dc.getHeight() * Math.abs(pc.scale().y()) / PIXEL);
    float left = pc.position().x() + Math.round((spriteWidth - WIDTH) / 2f) * PIXEL;
    float bottom = pc.position().y() + (spriteHeight + 1) * PIXEL;

    for (int i = 0; i < bars.size(); i++) {
      BarDisplayable bar = bars.get(i);
      float y = bottom + i * (HEIGHT - 1) * PIXEL;
      int inner = WIDTH - 2;
      float fraction = Math.max(0f, bar.current() / bar.max());
      int filled = bar.current() > 0 ? Math.max(1, Math.round(fraction * inner)) : 0;
      float x = left + PIXEL;
      Color color = bar.barColor();
      rect(batch, pixel, OUTLINE, left, y, WIDTH, HEIGHT);
      // Dark empty track, then the fill with a lighter top row.
      rect(batch, pixel, scratch.set(color).mul(0.3f, 0.3f, 0.3f, 1f), x, y + PIXEL, inner, 2);
      rect(batch, pixel, color, x, y + PIXEL, filled, 1);
      rect(batch, pixel, scratch.set(color).lerp(Color.WHITE, 0.35f), x, y + 2 * PIXEL, filled, 1);
    }
  }

  // Draws a rectangle of whole art pixels.
  private static void rect(
      SpriteBatch batch, Texture pixel, Color color, float x, float y, int width, int height) {
    if (width <= 0) return;
    batch.setColor(color);
    batch.draw(pixel, x, y, width * PIXEL, height * PIXEL);
  }
}
