package contrib.platform.gdx.hud;

import com.badlogic.gdx.graphics.g2d.Batch;
import contrib.hud.elements.GuiRenderContext;
import java.util.Objects;
import java.util.Optional;

/**
 * libGDX-specific render context for {@link contrib.hud.elements.CombinableGUI}.
 *
 * <p>This wraps the Scene2D/libGDX {@link Batch} used by the current HUD rendering path.
 */
public final class GdxGuiRenderContext implements GuiRenderContext {

  private final Batch batch;

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
  public Batch batch() {
    return this.batch;
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    Objects.requireNonNull(type, "type");
    return type.isInstance(this) ? Optional.of(type.cast(this)) : Optional.empty();
  }
}
