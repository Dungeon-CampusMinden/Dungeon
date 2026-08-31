package feature.canvas;

import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Factory and runtime registry for {@link CanvasDefinition}s.
 *
 * <p>Definitions are registered under their canvas id so that the non serializable parts - the
 * default node supplier and the server side event handlers - never have to travel inside a {@link
 * DialogContext}. Only the canvas id and the evaluated default nodes are sent to the client, which
 * mirrors how {@link feature.puzzle.PuzzleMaker} keeps puzzles out of the wire format.
 *
 * <p>Typical usage from a level setup:
 *
 * <pre>{@code
 * CanvasDefinition canvas =
 *     CanvasMaker.builder("assoc-lab")
 *         .title("Association Lab")
 *         .areaSize(900, 600)
 *         .options(o -> o.zoom(0.25f, 4f).grid(32f, true).snapToGrid(true))
 *         // static defaults
 *         .node(new LabelNode("alpha", "Alpha").position(0, 0))
 *         .node(new LabelNode("beta", "Beta").position(200, 0))
 *         // dynamic defaults: re-evaluated on the server every time the canvas is opened,
 *         // so nodes unlocked by game progress simply appear on the next open
 *         .nodes(() -> discoveredItems().stream().map(MyNodes::forItem).toList())
 *         // reaction to an event the client fired via CanvasArea#fireServerEvent
 *         .onEvent("solved", payload -> door.open())
 *         .build();
 *
 * CanvasMaker.show(canvas, hero.id());
 * }</pre>
 */
public final class CanvasMaker {

  private static final Map<String, CanvasDefinition> REGISTRY = new ConcurrentHashMap<>();

  private CanvasMaker() {}

  /**
   * Creates a builder for a canvas with the given id.
   *
   * @param canvasId the unique canvas id; must not be null or blank
   * @return a new builder
   */
  public static Builder builder(String canvasId) {
    return new Builder(canvasId);
  }

  /**
   * Looks up a registered canvas definition.
   *
   * @param canvasId the canvas id to look up
   * @return the definition, if one is registered
   */
  public static Optional<CanvasDefinition> lookup(String canvasId) {
    return canvasId == null ? Optional.empty() : Optional.ofNullable(REGISTRY.get(canvasId));
  }

  /**
   * Registers a definition, replacing any definition previously registered under the same id.
   *
   * @param definition the definition to register; must not be null
   * @return the registered definition
   */
  public static CanvasDefinition register(CanvasDefinition definition) {
    Objects.requireNonNull(definition, "definition");
    REGISTRY.put(definition.id(), definition);
    return definition;
  }

  /**
   * Removes a definition from the registry.
   *
   * @param canvasId the canvas id to remove
   */
  public static void unregister(String canvasId) {
    REGISTRY.remove(canvasId);
  }

  /**
   * Opens the canvas dialog for the given hero.
   *
   * <p>The current default nodes are evaluated here, on the server, and attached to the dialog
   * context so the client can merge them with its local changes.
   *
   * @param definition the canvas to show; must not be null
   * @param heroId the entity id of the hero that opened the canvas
   * @param targetEntityIds entity ids the dialog should be shown for; empty for all
   * @return the {@link UIComponent} holding the dialog
   */
  public static UIComponent show(CanvasDefinition definition, int heroId, int... targetEntityIds) {
    Objects.requireNonNull(definition, "definition");
    register(definition);

    CanvasSnapshot defaults = new CanvasSnapshot(definition.currentDefaultNodes());
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.CANVAS)
            .put(CanvasDialog.KEY_CANVAS_ID, definition.id())
            .put(CanvasDialog.KEY_HERO_ID, heroId)
            .put(CanvasDialog.KEY_DEFAULT_NODES, defaults)
            .put(DialogContextKeys.TITLE, definition.title())
            .build();

    UIComponent ui =
        DialogFactory.show(context, definition.pauseGame(), definition.closable(), targetEntityIds);
    ui.registerCallback(CanvasUI.EVENT_CLOSE, payload -> UIUtils.closeDialog(ui));
    definition.eventHandlers().forEach(ui::registerCallback);
    return ui;
  }

  /** Fluent builder for {@link CanvasDefinition}s. */
  public static final class Builder {

    private final String canvasId;
    private final List<NodeState> staticNodes = new ArrayList<>();
    private final List<Supplier<List<NodeState>>> dynamicNodes = new ArrayList<>();
    private final Map<String, Consumer<DialogResponseMessage.Payload>> eventHandlers =
        new LinkedHashMap<>();
    private final CanvasOptions options = new CanvasOptions();

    private String title = "";
    private float areaWidth = 900f;
    private float areaHeight = 560f;
    private boolean pauseGame = true;
    private boolean closable = true;

    private Builder(String canvasId) {
      Objects.requireNonNull(canvasId, "canvasId");
      if (canvasId.isBlank()) {
        throw new IllegalArgumentException("canvasId must not be blank");
      }
      this.canvasId = canvasId;
    }

    /**
     * Sets the window title.
     *
     * @param value the title text
     * @return this builder for chaining
     */
    public Builder title(String value) {
      this.title = value == null ? "" : value;
      return this;
    }

    /**
     * Sets the viewport size in pixels.
     *
     * @param width the viewport width
     * @param height the viewport height
     * @return this builder for chaining
     */
    public Builder areaSize(float width, float height) {
      if (width <= 0f || height <= 0f) {
        throw new IllegalArgumentException("area size must be positive");
      }
      this.areaWidth = width;
      this.areaHeight = height;
      return this;
    }

    /**
     * Configures the canvas options.
     *
     * @param configurator receives the mutable options of this canvas
     * @return this builder for chaining
     */
    public Builder options(Consumer<CanvasOptions> configurator) {
      configurator.accept(options);
      return this;
    }

    /**
     * Adds a static default node.
     *
     * @param node the node to snapshot; must not be null
     * @return this builder for chaining
     */
    public Builder node(CanvasNode node) {
      Objects.requireNonNull(node, "node");
      return node(node.toState());
    }

    /**
     * Adds a static default node.
     *
     * @param state the node state; must not be null
     * @return this builder for chaining
     */
    public Builder node(NodeState state) {
      Objects.requireNonNull(state, "state");
      staticNodes.add(state.withOrigin(NodeOrigin.DEFAULT));
      return this;
    }

    /**
     * Adds several static default nodes.
     *
     * @param states the node states
     * @return this builder for chaining
     */
    public Builder nodes(List<NodeState> states) {
      states.forEach(this::node);
      return this;
    }

    /**
     * Adds a dynamic source of default nodes.
     *
     * <p>The supplier is evaluated on the server every time the canvas is opened, which is the
     * mechanism for defaults that only appear once the player made progress.
     *
     * @param supplier produces the current default nodes; must not be null
     * @return this builder for chaining
     */
    public Builder nodes(Supplier<List<NodeState>> supplier) {
      dynamicNodes.add(Objects.requireNonNull(supplier, "supplier"));
      return this;
    }

    /**
     * Registers a server side handler for an event the client can fire.
     *
     * @param key the event key, as passed to {@link CanvasArea#fireServerEvent(String)}
     * @param handler the handler to run on the server
     * @return this builder for chaining
     * @see CanvasArea#fireServerEvent(String, DialogResponseMessage.Payload)
     */
    public Builder onEvent(String key, Consumer<DialogResponseMessage.Payload> handler) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(handler, "handler");
      eventHandlers.put(key, handler);
      return this;
    }

    /**
     * Sets whether opening the canvas pauses the game.
     *
     * @param value true to pause the game while the canvas is open
     * @return this builder for chaining
     */
    public Builder pauseGame(boolean value) {
      this.pauseGame = value;
      return this;
    }

    /**
     * Sets whether the player may close the canvas.
     *
     * @param value true to allow closing
     * @return this builder for chaining
     */
    public Builder closable(boolean value) {
      this.closable = value;
      return this;
    }

    /**
     * Builds the definition and registers it.
     *
     * @return the built and registered definition
     */
    public CanvasDefinition build() {
      List<NodeState> fixed = List.copyOf(staticNodes);
      List<Supplier<List<NodeState>>> suppliers = List.copyOf(dynamicNodes);
      Supplier<List<NodeState>> combined =
          () -> {
            List<NodeState> all = new ArrayList<>(fixed);
            for (Supplier<List<NodeState>> supplier : suppliers) {
              List<NodeState> supplied = supplier.get();
              if (supplied != null) {
                all.addAll(supplied);
              }
            }
            return all;
          };
      return register(
          new CanvasDefinition(
              canvasId,
              title,
              areaWidth,
              areaHeight,
              options,
              combined,
              eventHandlers,
              pauseGame,
              closable));
    }
  }
}
