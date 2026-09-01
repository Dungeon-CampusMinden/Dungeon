package feature.canvas;

import engine.network.messages.c2s.DialogResponseMessage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Server side description of a canvas.
 *
 * <p>A definition is authored once, typically during level setup, and then reused every time the
 * canvas is opened. It deliberately does <em>not</em> hold any live UI state: the only thing it
 * knows about nodes is how to produce the current set of <em>default</em> nodes.
 *
 * <p>The default nodes are supplied lazily and re-evaluated on every open. That is what makes
 * progression work: a canvas can start with six nodes and, once the player has found the missing
 * items, hand out all ten the next time it is opened, without losing anything the player arranged
 * in the meantime.
 *
 * @see CanvasMaker
 * @see CanvasSnapshot
 */
public final class CanvasDefinition {

  private final String id;
  private final String title;
  private final float areaWidth;
  private final float areaHeight;
  private final CanvasOptions options;
  private final Supplier<List<NodeState>> defaultNodes;
  private final Map<String, Consumer<DialogResponseMessage.Payload>> eventHandlers;
  private final boolean pauseGame;
  private final boolean closable;
  private final boolean showResetViewButton;
  private final boolean showFitButton;

  CanvasDefinition(
      String id,
      String title,
      float areaWidth,
      float areaHeight,
      CanvasOptions options,
      Supplier<List<NodeState>> defaultNodes,
      Map<String, Consumer<DialogResponseMessage.Payload>> eventHandlers,
      boolean pauseGame,
      boolean closable,
      boolean showResetViewButton,
      boolean showFitButton) {
    this.id = Objects.requireNonNull(id, "id");
    this.title = title == null ? "" : title;
    this.areaWidth = areaWidth;
    this.areaHeight = areaHeight;
    this.options = Objects.requireNonNull(options, "options");
    this.defaultNodes = Objects.requireNonNull(defaultNodes, "defaultNodes");
    this.eventHandlers = Map.copyOf(eventHandlers);
    this.pauseGame = pauseGame;
    this.closable = closable;
    this.showResetViewButton = showResetViewButton;
    this.showFitButton = showFitButton;
  }

  /**
   * Returns the unique id of this canvas.
   *
   * @return the canvas id
   */
  public String id() {
    return id;
  }

  /**
   * Returns the window title of this canvas.
   *
   * @return the title, possibly empty
   */
  public String title() {
    return title;
  }

  /**
   * Returns the preferred viewport width in pixels.
   *
   * @return the area width
   */
  public float areaWidth() {
    return areaWidth;
  }

  /**
   * Returns the preferred viewport height in pixels.
   *
   * @return the area height
   */
  public float areaHeight() {
    return areaHeight;
  }

  /**
   * Returns the configuration of this canvas.
   *
   * @return the canvas options
   */
  public CanvasOptions options() {
    return options;
  }

  /**
   * Evaluates the current default node set.
   *
   * <p>Called server side each time the canvas dialog is shown.
   *
   * @return the current default nodes, always tagged as {@link NodeOrigin#DEFAULT}
   */
  public List<NodeState> currentDefaultNodes() {
    List<NodeState> supplied = defaultNodes.get();
    if (supplied == null) {
      return List.of();
    }
    return supplied.stream().map(state -> state.withOrigin(NodeOrigin.DEFAULT)).toList();
  }

  /**
   * Returns the registered server side event handlers.
   *
   * @return an unmodifiable map from event key to handler
   */
  public Map<String, Consumer<DialogResponseMessage.Payload>> eventHandlers() {
    return eventHandlers;
  }

  /**
   * Returns whether opening this canvas pauses the game.
   *
   * @return true if the game is paused while the canvas is open
   */
  public boolean pauseGame() {
    return pauseGame;
  }

  /**
   * Returns whether the player may close this canvas.
   *
   * @return true if the canvas dialog is closable
   */
  public boolean closable() {
    return closable;
  }

  /**
   * Returns whether the canvas shows a reset-view button.
   *
   * @return true if the reset-view button is shown
   */
  public boolean showResetViewButton() {
    return showResetViewButton;
  }

  /**
   * Returns whether the canvas shows a fit-to-content button.
   *
   * @return true if the fit button is shown
   */
  public boolean showFitButton() {
    return showFitButton;
  }
}
