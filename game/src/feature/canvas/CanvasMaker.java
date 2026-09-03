package feature.canvas;

import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.logging.DungeonLogger;
import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Defines canvases and resolves their callback-bearing node prototypes on clients.
 *
 * <p>A room normally needs one static field:
 *
 * <pre>{@code
 * private static final CanvasDefinition CANVAS =
 *     CanvasMaker.define("association-lab", canvas -> canvas
 *         .title("Association Lab")
 *         .nodes(MyCanvas::nodes));
 * }</pre>
 *
 * <p>{@link #define(String, Consumer)} records its caller class. When a multiplayer client receives
 * the dialog, that class is loaded to trigger the same static definition and make node callbacks
 * available locally.
 */
public final class CanvasMaker {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CanvasMaker.class);
  private static final Map<String, CanvasDefinition> REGISTRY = new ConcurrentHashMap<>();

  private CanvasMaker() {}

  /**
   * Defines and registers a canvas.
   *
   * <p>Call this from a static field or static initializer of the class that owns the canvas. That
   * allows clients to load the class automatically when the dialog opens.
   *
   * @param canvasId the unique canvas id
   * @param configurator configures the canvas
   * @return the registered definition
   */
  public static CanvasDefinition define(String canvasId, Consumer<Builder> configurator) {
    return define(canvasId, callerClassName(), configurator);
  }

  /**
   * Defines and registers a canvas with an explicit provider class.
   *
   * <p>Use this overload when the definition is created through a shared helper and automatic
   * caller detection would identify the helper instead of the class whose static initialization
   * registers the canvas.
   *
   * @param canvasId the unique canvas id
   * @param providerClass the class whose static initialization registers this definition
   * @param configurator configures the canvas
   * @return the registered definition
   */
  public static CanvasDefinition define(
      String canvasId, Class<?> providerClass, Consumer<Builder> configurator) {
    Objects.requireNonNull(providerClass, "providerClass");
    return define(canvasId, providerClass.getName(), configurator);
  }

  private static CanvasDefinition define(
      String canvasId, String providerClass, Consumer<Builder> configurator) {
    Objects.requireNonNull(configurator, "configurator");
    Builder builder = new Builder(canvasId);
    configurator.accept(builder);
    return register(builder.build(providerClass));
  }

  private static String callerClassName() {
    return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
        .walk(
            frames ->
                frames
                    .map(StackWalker.StackFrame::getDeclaringClass)
                    .filter(type -> type != CanvasMaker.class)
                    .findFirst()
                    .orElseThrow()
                    .getName());
  }

  /**
   * Looks up a registered canvas definition.
   *
   * @param canvasId the canvas id
   * @return the definition, if registered
   */
  public static Optional<CanvasDefinition> lookup(String canvasId) {
    return canvasId == null ? Optional.empty() : Optional.ofNullable(REGISTRY.get(canvasId));
  }

  /**
   * Resolves a definition, loading its provider class when necessary.
   *
   * @param canvasId the canvas id
   * @param providerClass the class whose static initializer defines the canvas
   * @return the resolved definition, if available
   */
  public static Optional<CanvasDefinition> resolve(String canvasId, String providerClass) {
    Optional<CanvasDefinition> existing = lookup(canvasId);
    if (existing.isPresent()) {
      CanvasDefinition definition = existing.orElseThrow();
      if (providerClass == null || definition.providerClass().equals(providerClass)) {
        return existing;
      }
      LOGGER.warn(
          "Canvas '{}' is registered by '{}' instead of requested provider '{}'",
          canvasId,
          definition.providerClass(),
          providerClass);
      return Optional.empty();
    }
    if (providerClass == null || providerClass.isBlank()) {
      return existing;
    }
    try {
      Class.forName(providerClass);
    } catch (ClassNotFoundException | LinkageError e) {
      LOGGER.warn(
          "Could not load canvas provider '{}' for '{}'; callbacks will be unavailable",
          providerClass,
          canvasId);
    }
    Optional<CanvasDefinition> resolved = lookup(canvasId);
    if (resolved.isEmpty()) {
      LOGGER.warn(
          "Canvas provider '{}' loaded without registering definition '{}'",
          providerClass,
          canvasId);
      return Optional.empty();
    }
    CanvasDefinition definition = resolved.orElseThrow();
    if (!definition.providerClass().equals(providerClass)) {
      LOGGER.warn(
          "Canvas '{}' resolved to provider '{}' instead of '{}'",
          canvasId,
          definition.providerClass(),
          providerClass);
      return Optional.empty();
    }
    return resolved;
  }

  private static CanvasDefinition register(CanvasDefinition definition) {
    REGISTRY.put(definition.id(), definition);
    return definition;
  }

  /** Fluent configuration used by {@link #define(String, Consumer)}. */
  public static final class Builder {

    private final String canvasId;
    private final List<NodeState> staticNodes = new ArrayList<>();
    private final Map<String, Consumer<DialogResponseMessage.Payload>> eventHandlers =
        new LinkedHashMap<>();
    private final CanvasOptions options = new CanvasOptions();
    private CanvasNodesSupplier nodesSupplier = context -> List.of();
    private String title = "";
    private float areaWidth = 900f;
    private float areaHeight = 560f;
    private boolean pauseGame = true;
    private boolean closable = true;
    private boolean showResetViewButton = true;
    private boolean showFitButton = true;

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
      title = value == null ? "" : value;
      return this;
    }

    /**
     * Sets the preferred viewport size.
     *
     * @param width the viewport width
     * @param height the viewport height
     * @return this builder for chaining
     */
    public Builder areaSize(float width, float height) {
      if (width <= 0f || height <= 0f) {
        throw new IllegalArgumentException("area size must be positive");
      }
      areaWidth = width;
      areaHeight = height;
      return this;
    }

    /**
     * Configures canvas interaction and rendering options.
     *
     * @param configurator receives the mutable canvas options
     * @return this builder for chaining
     */
    public Builder options(Consumer<CanvasOptions> configurator) {
      Objects.requireNonNull(configurator, "configurator").accept(options);
      return this;
    }

    /**
     * Adds a state-only static node.
     *
     * <p>Use {@link #nodes(CanvasNodesSupplier)} for nodes with callbacks.
     *
     * @param node the node to snapshot
     * @return this builder for chaining
     */
    public Builder node(CanvasNode node) {
      staticNodes.add(Objects.requireNonNull(node, "node").toState());
      return this;
    }

    /**
     * Sets the source of callback-bearing nodes evaluated for every opening.
     *
     * @param supplier the node supplier
     * @return this builder for chaining
     */
    public Builder nodes(CanvasNodesSupplier supplier) {
      nodesSupplier = Objects.requireNonNull(supplier, "supplier");
      return this;
    }

    /**
     * Registers a server-side canvas event handler.
     *
     * @param key the event key fired by the canvas
     * @param handler the server-side handler
     * @return this builder for chaining
     */
    public Builder onEvent(String key, Consumer<DialogResponseMessage.Payload> handler) {
      eventHandlers.put(
          Objects.requireNonNull(key, "key"), Objects.requireNonNull(handler, "handler"));
      return this;
    }

    /**
     * Sets whether opening the canvas pauses the game.
     *
     * @param value true to pause the game
     * @return this builder for chaining
     */
    public Builder pauseGame(boolean value) {
      pauseGame = value;
      return this;
    }

    /**
     * Sets whether the player may close the canvas.
     *
     * @param value true to allow closing
     * @return this builder for chaining
     */
    public Builder closable(boolean value) {
      closable = value;
      return this;
    }

    /**
     * Sets whether the reset-view button is visible.
     *
     * @param value true to show the button
     * @return this builder for chaining
     */
    public Builder showResetViewButton(boolean value) {
      showResetViewButton = value;
      return this;
    }

    /**
     * Sets whether the fit-to-content button is visible.
     *
     * @param value true to show the button
     * @return this builder for chaining
     */
    public Builder showFitButton(boolean value) {
      showFitButton = value;
      return this;
    }

    private CanvasDefinition build(String providerClass) {
      List<NodeState> fixed = List.copyOf(staticNodes);
      CanvasNodesSupplier combined =
          context -> {
            List<CanvasNode> nodes = new ArrayList<>(fixed.size());
            fixed.forEach(state -> nodes.add(CanvasNodeType.create(state)));
            List<CanvasNode> supplied = nodesSupplier.get(context);
            if (supplied != null) {
              nodes.addAll(supplied);
            }
            return nodes;
          };
      return new CanvasDefinition(
          canvasId,
          new CanvasLayout(
              title, areaWidth, areaHeight, options, showResetViewButton, showFitButton),
          combined,
          eventHandlers,
          pauseGame,
          closable,
          providerClass);
    }
  }
}
