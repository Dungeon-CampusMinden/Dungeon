package feature.canvas;

import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Reusable definition of a canvas, including its layout, current nodes and event handlers.
 *
 * <p>The node supplier is evaluated on every opening. The server snapshots the current game state;
 * the client evaluates it again only to obtain callback-bearing node prototypes.
 */
public final class CanvasDefinition {

  private final String id;
  private final CanvasLayout layout;
  private final CanvasNodesSupplier nodesSupplier;
  private final Map<String, Consumer<DialogResponseMessage.Payload>> eventHandlers;
  private final boolean pauseGame;
  private final boolean closable;
  private final String providerClass;

  CanvasDefinition(
      String id,
      CanvasLayout layout,
      CanvasNodesSupplier nodesSupplier,
      Map<String, Consumer<DialogResponseMessage.Payload>> eventHandlers,
      boolean pauseGame,
      boolean closable,
      String providerClass) {
    this.id = Objects.requireNonNull(id, "id");
    this.layout = Objects.requireNonNull(layout, "layout");
    this.nodesSupplier = Objects.requireNonNull(nodesSupplier, "nodesSupplier");
    this.eventHandlers = Map.copyOf(eventHandlers);
    this.pauseGame = pauseGame;
    this.closable = closable;
    this.providerClass = Objects.requireNonNull(providerClass, "providerClass");
  }

  /**
   * Returns the unique canvas id.
   *
   * @return the canvas id
   */
  public String id() {
    return id;
  }

  /**
   * Returns the visual canvas configuration.
   *
   * @return the canvas layout
   */
  public CanvasLayout layout() {
    return layout;
  }

  /**
   * Returns the provider class used for client-side auto-registration.
   *
   * @return the fully qualified provider class name
   */
  public String providerClass() {
    return providerClass;
  }

  /**
   * Creates nodes for the supplied runtime context.
   *
   * @param context the opening context
   * @return fresh node instances
   */
  public List<CanvasNode> currentNodes(CanvasContext context) {
    List<CanvasNode> nodes = nodesSupplier.get(context);
    return nodes == null ? List.of() : List.copyOf(nodes);
  }

  /**
   * Opens this canvas for the given hero.
   *
   * @param heroId the entity id of the hero opening the canvas
   * @param targetEntityIds entities that should see the dialog; empty means all
   * @return the component holding the dialog
   */
  public UIComponent open(int heroId, int... targetEntityIds) {
    CanvasContext opening = new CanvasContext(id, heroId, false);
    List<NodeState> defaults =
        currentNodes(opening).stream()
            .map(CanvasNode::toState)
            .map(state -> state.withOrigin(NodeOrigin.DEFAULT))
            .toList();
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.CANVAS)
            .put(CanvasDialog.KEY_CANVAS_ID, id)
            .put(CanvasDialog.KEY_HERO_ID, heroId)
            .put(CanvasDialog.KEY_DEFAULT_NODES, new CanvasSnapshot(defaults))
            .put(CanvasDialog.KEY_LAYOUT, layout)
            .put(CanvasDialog.KEY_PROVIDER_CLASS, providerClass)
            .build();

    UIComponent ui = DialogFactory.show(context, pauseGame, closable, targetEntityIds);
    ui.registerCallback(CanvasUI.EVENT_CLOSE, payload -> UIUtils.closeDialog(ui));
    eventHandlers.forEach(ui::registerCallback);
    return ui;
  }
}
