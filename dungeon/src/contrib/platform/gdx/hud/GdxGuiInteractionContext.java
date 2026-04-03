package contrib.platform.gdx.hud;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.hud.elements.GuiInteractionContext;
import java.util.Objects;
import java.util.Optional;

/**
 * libGDX-specific interaction context for {@link contrib.hud.elements.CombinableGUI}.
 *
 * <p>Currently this wraps the shared Scene2D {@link DragAndDrop} instance that is created by
 * {@link contrib.hud.elements.GUICombination}.
 */
public final class GdxGuiInteractionContext implements GuiInteractionContext {

  private final DragAndDrop dragAndDrop;

  /**
   * Creates a new libGDX interaction context.
   *
   * @param dragAndDrop shared Scene2D drag-and-drop context, may be {@code null} in headless mode
   */
  public GdxGuiInteractionContext(DragAndDrop dragAndDrop) {
    this.dragAndDrop = dragAndDrop;
  }

  /**
   * Returns the wrapped Scene2D drag-and-drop context if available.
   *
   * @return optional drag-and-drop context
   */
  public Optional<DragAndDrop> dragAndDrop() {
    return Optional.ofNullable(dragAndDrop);
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    Objects.requireNonNull(type, "type");
    return type.isInstance(this) ? Optional.of(type.cast(this)) : Optional.empty();
  }
}
