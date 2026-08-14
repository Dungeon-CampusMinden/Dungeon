package feature.systems;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import engine.Entity;
import engine.Game;
import engine.System;
import engine.game.PreRunConfiguration;
import engine.network.NetworkUtils;
import engine.network.messages.s2c.DialogShowMessage;
import engine.network.server.DialogTracker;
import engine.utils.Tuple;
import engine.utils.components.MissingComponentException;
import engine.utils.logging.DungeonLogger;
import feature.components.UIComponent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The basic handling of any UIComponent. Adds them to the Stage, updates the Stage each Frame to
 * allow EventHandling.
 *
 * <p>Entities with the {@link UIComponent} will be processed by this system.
 */
public final class HudSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(HudSystem.class);
  private static final HudSystem INSTANCE = new HudSystem();
  private boolean ipaused = false;

  /** Stores the component because it is no longer available on the entity during removal. */
  private final Map<Entity, UIComponent> entityUIComponentMap = new HashMap<>();

  /**
   * Returns the singleton HUD system.
   *
   * @return the HUD system instance
   */
  public static HudSystem getInstance() {
    return INSTANCE;
  }

  /** Create the singleton HudSystem. */
  private HudSystem() {
    super(AuthoritativeSide.BOTH, UIComponent.class);
    onEntityAdd = this::addListener;
    onEntityRemove = this::removeListener;
  }

  /**
   * Returns the topmost closeable UI.
   *
   * @return a Tuple of the Entity and its UIComponent
   */
  public Optional<Tuple<Entity, UIComponent>> topmostCloseableUI() {
    return entityUIComponentMap.entrySet().stream()
        .filter(entry -> entry.getValue().isVisible() && entry.getValue().canBeClosed())
        .max(Comparator.comparingInt(entry -> entry.getValue().dialog().getZIndex()))
        .map(entry -> Tuple.of(entry.getKey(), entry.getValue()));
  }

  /**
   * Returns whether there is any open pausing UI for a given entity.
   *
   * @param entity the entity to check for
   * @return true if there is an open pausing UI for the entity, false otherwise
   */
  public boolean hasOpenPausingUI(Entity entity) {
    return entityUIComponentMap.values().stream()
        .anyMatch(component -> component.willPauseGame() && isVisibleFor(component, entity));
  }

  /**
   * Returns whether the entity is affected by any visible UI.
   *
   * @param entity the entity to check
   * @return true if a visible UI targets the entity or all entities
   */
  public boolean hasOpenUI(Entity entity) {
    return entityUIComponentMap.values().stream()
        .anyMatch(component -> isVisibleFor(component, entity));
  }

  private boolean isVisibleFor(UIComponent component, Entity entity) {
    int[] targets = component.targetEntityIds();
    return component.isVisible()
        && (targets.length == 0 || Arrays.stream(targets).anyMatch(id -> id == entity.id()));
  }

  /**
   * Once a UIComponent is removed, its Dialog has to be removed from the Stage.
   *
   * @param entity Entity which no longer has a UIComponent.
   */
  private void removeListener(final Entity entity) {
    UIComponent removedComponent = entityUIComponentMap.remove(entity);
    if (removedComponent == null) {
      return;
    }
    removedComponent.dialog().remove();
    boolean terminalRemoval =
        !entity.isPresent(UIComponent.class) || Game.systems().get(HudSystem.class) == this;
    if (PreRunConfiguration.isNetworkServer()) {
      DialogTracker.instance()
          .closeDialog(removedComponent.dialogContext().dialogId(), terminalRemoval);
    }

    // Removing a system temporarily detaches its entities too. Only dispose the dialog when the
    // component or its entity was removed; otherwise the same component is re-added with the
    // system.
    if (terminalRemoval && removedComponent.dialog() instanceof Disposable disposable) {
      disposable.dispose();
    }
  }

  /**
   * When an Entity with a UIComponent is added, its dialog has to be added to the Stage for UI
   * Representation.
   *
   * @param entity Entity which now has a UIComponent.
   */
  private void addListener(final Entity entity) {
    UIComponent component =
        entity
            .fetch(UIComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, UIComponent.class));

    // check if we should draw it
    int[] myIds = Game.allPlayers().mapToInt(Entity::id).toArray();
    int[] targetIds = component.targetEntityIds();
    int[] affectedIds =
        Arrays.stream(myIds)
            .filter(id -> Arrays.stream(targetIds).anyMatch(targetId -> targetId == id))
            .toArray();

    if (targetIds.length != 0 && affectedIds.length == 0) {
      // This UI is not for any of the current players
      return;
    }

    Group dialog = component.dialog();

    Game.stage()
        .ifPresentOrElse(
            stage -> addDialogToStage(dialog, stage),
            () -> sendDialogToClients(entity, component, affectedIds));

    entityUIComponentMap.put(entity, component);
    // Multiplayer clients only render dialogs. The server keeps callback ownership and
    // response authorization in DialogTracker.
    if (!Game.isMultiplayerClient()) {
      DialogTracker.instance().registerDialog(component);
    }
  }

  /**
   * Sends the dialog to all connected and relevant clients.
   *
   * <p>A dialog is relevant for a client, if the targetEntityIds of the UIComponent contains the id
   * of an entity controlled by the client or if targetEntityIds is empty (meaning all clients).
   *
   * @param entity the entity which owns the UIComponent
   * @param component the UIComponent to send
   * @param targetIds all clients that are connect and should receive the dialog
   */
  private void sendDialogToClients(
      final Entity entity, final UIComponent component, int[] targetIds) {
    Set<Short> clientIds =
        (targetIds.length == 0)
            ? NetworkUtils.getAllConnectedClientIds()
            : NetworkUtils.entityIdsToClientIds(targetIds);

    if (clientIds.isEmpty()) {
      return; // No clients to send to
    }

    // Send dialog to all target clients
    DialogShowMessage msg =
        new DialogShowMessage(component.dialogContext(), component.canBeClosed());
    for (short clientId : clientIds) {
      Game.network().send(clientId, msg, true);
    }
  }

  private void addDialogToStage(final Group group, final Stage stage) {
    if (!stage.getActors().contains(group, true)) {
      stage.addActor(group);
    } else {
      group.toFront(); // ensure it's on top
    }
  }

  @Override
  public void execute() {
    try {
      if (filteredEntityStream(UIComponent.class).anyMatch(this::pausesGame)) {
        if (!ipaused) pauseGame();
      } else {
        if (ipaused) unpauseGame();
      }
    } catch (Exception ignored) {
    } // only a hotfix for reconnecting clients

    // clean up any entities that no longer have a UIComponent
    entityUIComponentMap.keySet().removeIf(entity -> !entity.isPresent(UIComponent.class));
  }

  private boolean pausesGame(final Entity entity) {
    Optional<UIComponent> uiComponent = entity.fetch(UIComponent.class);
    return uiComponent
        .filter(component -> component.isVisible() && component.willPauseGame())
        .isPresent();
  }

  private void pauseGame() {
    LOGGER.info("Pausing game due to open UI");
    ipaused = true;
    if (PreRunConfiguration.multiplayerEnabled()) return; // don't pause in multiplayer mode
    Game.systems().values().forEach(System::stop);
  }

  private void unpauseGame() {
    LOGGER.info("Unpausing game as no pausing UIs are open");
    if (PreRunConfiguration.multiplayerEnabled()) {
      ipaused = false;
      return; // don't pause in multiplayer mode
    }
    if (ipaused) Game.systems().values().forEach(System::run);
    ipaused = false;
  }

  /** HudSystem can´t be paused. */
  @Override
  public void stop() {}
}
