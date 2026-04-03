package contrib.hud.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.platform.gdx.hud.GdxGuiInteractionContext;
import core.utils.Vector2;
import java.util.Objects;
import java.util.Optional;

/**
 * A GUI element that can be combined with other GUI elements using {@link GUICombination}.
 *
 * <p>GUI elements that should be displayed together should extend this class.
 *
 * <p>This class is used to create GUI elements that can be combined in one {@link GUICombination}
 * to be displayed together. The {@link GUICombination} will call the methods of this class to draw
 * the element and to calculate the preferred size.
 *
 * <p>Interaction is exposed through a backend-neutral {@link GuiInteractionContext}. Concrete
 * backends may unwrap specialized interaction helpers from that context.
 */
public abstract class CombinableGUI {

  // Position of the GUI-Element and its size
  private int x, y, width, height;

  private GuiInteractionContext interactionContext = new GuiInteractionContext() {};
  // Still needed for existing GDX button/input handling.
  private Actor actor;

  /**
   * Sets the interaction context for this GUI element.
   *
   * <p>This is called by the parent {@link GUICombination} during initialization.
   *
   * @param interactionContext backend-neutral interaction context
   */
  public final void interactionContext(final GuiInteractionContext interactionContext) {
    this.interactionContext =
      interactionContext == null ? new GuiInteractionContext() {} : interactionContext;
    this.initInteraction(this.interactionContext);
  }

  /**
   * Returns the current interaction context.
   *
   * @return current interaction context
   */
  protected final GuiInteractionContext interactionContext() {
    return this.interactionContext;
  }

  /**
   * Tries to unwrap the current interaction context to a backend-specific type.
   *
   * @param type requested type
   * @param <T> target type
   * @return matching backend-specific interaction helper if available
   */
  protected final <T> Optional<T> interactionContext(Class<T> type) {
    Objects.requireNonNull(type, "type");
    return this.interactionContext.unwrap(type);
  }

  /**
   * Temporary compatibility bridge for legacy libGDX HUD code that still injects a shared
   * DragAndDrop instance directly.
   *
   * <p>The new primary API is {@link #interactionContext(GuiInteractionContext)}.
   *
   * @param dragAndDrop shared libGDX DragAndDrop context
   * @deprecated migrate callers to {@link #interactionContext(GuiInteractionContext)}
   */
  @Deprecated
  public final void dragAndDrop(final DragAndDrop dragAndDrop) {
    this.interactionContext(new GdxGuiInteractionContext(dragAndDrop));
  }

  /**
   * Initializes backend-specific interaction hooks.
   *
   * <p>Default implementation does nothing.
   *
   * @param interactionContext backend-neutral interaction context
   */
  protected void initInteraction(final GuiInteractionContext interactionContext) {}

  /**
   * Draw the element.
   *
   * <p>This method should be used for drawing the main part of the element.
   *
   * @param batch the batch to draw to
   */
  protected abstract void draw(final Batch batch);

  /**
   * Draw the top layer of the element.
   *
   * <p>This method should be used for things like hover information that need to be on top of
   * everything else.
   *
   * @param batch the batch to draw to
   */
  protected void drawTopLayer(final Batch batch) {}

  /**
   * Draw debug information for the element.
   *
   * <p>The default implementation does nothing.
   */
  protected void drawDebug() {}

  /**
   * Calculate the preferred size of the gui element.
   *
   * @param availableSpace the available space for the element to be drawn in
   * @return the preferred size of the element
   */
  protected abstract Vector2 preferredSize(final GUICombination.AvailableSpace availableSpace);

  /** Called when the bounds of the element change. */
  protected void boundsUpdate() {}

  public int x() {
    return this.x;
  }

  public void x(int x) {
    this.x = x;
    this.syncActorBounds();
  }


  public int y() {
    return this.y;
  }

  public void y(int y) {
    this.y = y;
    this.syncActorBounds();
  }

  /**
   * Get the width of the element.
   *
   * @return the width.
   */
  public final int width() {
    return this.width;
  }

  /**
   * Set the width of the element.
   *
   * @param width the width.
   */
  public void width(int width) {
    this.width = width;
    this.syncActorBounds();
  }

  /**
   * Get the height of the element.
   *
   * @return the height.
   */
  public final int height() {
    return this.height;
  }

  /**
   * Set the height of the element.
   *
   * @param height the height.
   */
  public void height(int height) {
    this.height = height;
    this.syncActorBounds();
  }

  /**
   * Returns the backend anchor actor for legacy libGDX HUD integration.
   *
   * <p>This actor is intentionally kept as a narrow compatibility seam for drag-and-drop,
   * keyboard focus and remaining Scene2D listeners. It is no longer the primary interaction API
   * of {@link CombinableGUI}.
   *
   * @return libGDX anchor actor
   */
  protected final Actor actor() {
    if (this.actor == null) {
      this.actor = new Actor();
      this.syncActorBounds();
    }
    return this.actor;
  }

  private void syncActorBounds() {
    if (this.actor != null) {
      this.actor.setBounds(this.x, this.y, this.width, this.height);
    }
  }
}
