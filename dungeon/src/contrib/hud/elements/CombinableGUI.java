package contrib.hud.elements;

import com.badlogic.gdx.graphics.g2d.Batch;
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
 *
 * <p>Rendering is exposed through a backend-neutral {@link GuiRenderContext}. The old
 * libGDX-specific {@code Batch}-based draw methods are kept as a temporary compatibility seam so
 * subclasses can be migrated incrementally.
 */
public abstract class CombinableGUI {

  private int x, y, width, height;
  private GuiInteractionContext interactionContext = new GuiInteractionContext() {};

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
   * Initializes backend-specific interaction hooks.
   *
   * <p>Default implementation does nothing.
   *
   * @param interactionContext backend-neutral interaction context
   */
  protected void initInteraction(final GuiInteractionContext interactionContext) {}

  /**
   * Entry point used by the parent container to render the main layer of this widget.
   *
   * <p>The new primary API is {@link #renderContent(GuiRenderContext)}.
   *
   * @param renderContext backend-neutral render context
   */
  final void render(final GuiRenderContext renderContext) {
    this.renderContent(renderContext);
  }

  /**
   * Entry point used by the parent container to render the top layer of this widget.
   *
   * <p>The new primary API is {@link #renderTopLayerContent(GuiRenderContext)}.
   *
   * @param renderContext backend-neutral render context
   */
  final void renderTopLayer(final GuiRenderContext renderContext) {
    this.renderTopLayerContent(renderContext);
  }

  /**
   * Renders the main layer of this widget using a backend-neutral render context.
   *
   * <p>Default implementation delegates the temporary legacy rendering path back to the active
   * backend via {@link GuiRenderContext#renderLegacyContent(CombinableGUI)}.
   *
   * @param renderContext backend-neutral render context
   */
  protected void renderContent(final GuiRenderContext renderContext) {
    renderContext.renderLegacyContent(this);
  }

  /**
   * Renders the top layer of this widget using a backend-neutral render context.
   *
   * <p>Default implementation delegates the temporary legacy rendering path back to the active
   * backend via {@link GuiRenderContext#renderLegacyTopLayer(CombinableGUI)}.
   *
   * @param renderContext backend-neutral render context
   */
  protected void renderTopLayerContent(final GuiRenderContext renderContext) {
    renderContext.renderLegacyTopLayer(this);
  }

  /**
   * Temporary backend-adapter bridge for the old libGDX main-layer render hook.
   *
   * <p>This keeps the legacy {@link Batch}-based subclasses working while the actual dispatch
   * decision is moved out of this class and into backend-specific render contexts.
   *
   * @param batch libGDX batch
   */
  public final void renderLegacyBatchContent(final Batch batch) {
    this.draw(batch);
  }

  /**
   * Temporary backend-adapter bridge for the old libGDX top-layer render hook.
   *
   * <p>This keeps the legacy {@link Batch}-based subclasses working while the actual dispatch
   * decision is moved out of this class and into backend-specific render contexts.
   *
   * @param batch libGDX batch
   */
  public final void renderLegacyBatchTopLayer(final Batch batch) {
    this.drawTopLayer(batch);
  }

  /**
   * Legacy libGDX render hook.
   *
   * <p>This method remains temporarily so subclasses can be migrated incrementally to
   * {@link #renderContent(GuiRenderContext)}.
   *
   * @param batch libGDX batch
   * @deprecated migrate to {@link #renderContent(GuiRenderContext)}
   */
  @Deprecated
  protected void draw(final Batch batch) {}

  /**
   * Legacy libGDX top-layer render hook.
   *
   * <p>This method remains temporarily so subclasses can be migrated incrementally to
   * {@link #renderTopLayerContent(GuiRenderContext)}.
   *
   * @param batch libGDX batch
   * @deprecated migrate to {@link #renderTopLayerContent(GuiRenderContext)}
   */
  @Deprecated
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
  }

  public int y() {
    return this.y;
  }

  public void y(int y) {
    this.y = y;
  }

  public final int width() {
    return this.width;
  }

  public void width(int width) {
    this.width = width;
  }

  public final int height() {
    return this.height;
  }

  public void height(int height) {
    this.height = height;
  }
}
