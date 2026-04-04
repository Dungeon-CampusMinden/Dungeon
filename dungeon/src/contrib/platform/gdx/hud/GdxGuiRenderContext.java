package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import contrib.hud.elements.CombinableGUI;
import contrib.hud.elements.GuiRenderContext;
import java.util.Objects;
import java.util.Optional;

/**
 * libGDX-specific render context for {@link CombinableGUI}.
 *
 * <p>This wraps the Scene2D/libGDX {@link Batch} used by the current HUD rendering path.
 */
public record GdxGuiRenderContext(Batch batch) implements GuiRenderContext {

  /**
   * Creates a new libGDX render context.
   *
   * @param batch active libGDX batch
   */
  public GdxGuiRenderContext(Batch batch) {
    this.batch = Objects.requireNonNull(batch, "batch");
  }

  /**
   * Returns the wrapped libGDX batch.
   *
   * @return active batch
   */
  @Override
  public Batch batch() {
    return this.batch;
  }

  @Override
  public void renderLegacyContent(CombinableGUI gui) {
    Objects.requireNonNull(gui, "gui");
    gui.renderLegacyBatchContent(this.batch);
  }

  @Override
  public void renderLegacyTopLayer(CombinableGUI gui) {
    Objects.requireNonNull(gui, "gui");
    gui.renderLegacyBatchTopLayer(this.batch);
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    Objects.requireNonNull(type, "type");
    return type.isInstance(this) ? Optional.of(type.cast(this)) : Optional.empty();
  }
}
