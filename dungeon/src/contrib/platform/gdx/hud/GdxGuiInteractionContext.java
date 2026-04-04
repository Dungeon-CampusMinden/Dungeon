package contrib.platform.gdx.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.hud.elements.GuiInteractionContext;
import java.util.Objects;
import java.util.Optional;

/**
 * libGDX-specific interaction context for {@link contrib.hud.elements.CombinableGUI}.
 *
 * <p>This bundles the shared Scene2D drag-and-drop context together with the technical
 * Scene2D anchor actor that backend-specific adapters may still require.
 */
public final class GdxGuiInteractionContext implements GuiInteractionContext {

  private final DragAndDrop dragAndDrop;
  private final Actor actor;

  /**
   * Creates a new libGDX interaction context.
   *
   * @param actor technical Scene2D anchor actor for this GUI
   * @param dragAndDrop shared Scene2D drag-and-drop context, may be {@code null} in headless mode
   */
  public GdxGuiInteractionContext(Actor actor, DragAndDrop dragAndDrop) {
    this.actor = actor;
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

  /**
   * Returns the technical Scene2D anchor actor if available.
   *
   * @return optional anchor actor
   */
  public Optional<Actor> actor() {
    return Optional.ofNullable(actor);
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    Objects.requireNonNull(type, "type");
    return type.isInstance(this) ? Optional.of(type.cast(this)) : Optional.empty();
  }
}
