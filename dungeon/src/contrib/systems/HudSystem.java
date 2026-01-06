package contrib.systems;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.game.PreRunConfiguration;
import core.network.NetworkUtils;
import core.network.messages.s2c.DialogShowMessage;
import core.network.server.DialogTracker;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.*;

/**
 * The basic handling of any UIComponent. Adds them to the Stage, updates the Stage each Frame to
 * allow EventHandling.
 *
 * <p>Entities with the {@link UIComponent} will be processed by this system.
 */
public final class HudSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(HudSystem.class);
  private boolean ipaused = false;

  /**
   * The removeListener only gets the Entity after its Component is removed. Which means no longer
   * any access to the Group. This is why we need the last group an entity had as a mapping.
   */
  private final Map<Entity, Group> entityGroupMap = new HashMap<>();

  private final Map<Entity, UIComponent> entityUIComponentMap = new HashMap<>();

  /** Create a new HudSystem. */
  public HudSystem() {
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
   * Once a UIComponent is removed, its Dialog has to be removed from the Stage.
   *
   * @param entity Entity which no longer has a UIComponent.
   */
  private void removeListener(final Entity entity) {
    Group remove = entityGroupMap.remove(entity);
    if (remove != null) {
      remove.remove();
    }
    UIComponent component = entityUIComponentMap.remove(entity);
    if (component != null) {
      UIUtils.closeDialog(component);
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

    Group dialog = component.dialog();

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

    // increase open dialog count for all target entities
    for (Integer targetId : targetIds) {
      Optional<Entity> target = Game.findEntityById(targetId);
      target
          .flatMap(t -> t.fetch(PlayerComponent.class))
          .ifPresent(PlayerComponent::incrementOpenDialogs);
    }

    Game.stage()
        .ifPresentOrElse(
            stage -> {
              addDialogToStage(dialog, stage);
              addMapping(entity, dialog, component);
              DialogTracker.instance().registerDialog(component);
            },
            () -> {
              // Headless mode,
              if (PreRunConfiguration.multiplayerEnabled()
                  && PreRunConfiguration.isNetworkServer()) {
                sendDialogToClients(entity, component, affectedIds);
                addMapping(entity, dialog, component);
              }
            });
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

    DialogTracker.instance().registerDialog(component);

    // Send dialog to all target clients
    DialogShowMessage msg =
        new DialogShowMessage(component.dialogContext(), component.canBeClosed());
    for (short clientId : clientIds) {
      Game.network().send(clientId, msg, true);
    }
  }

  private void addMapping(final Entity entity, final Group dialog, final UIComponent component) {
    Group previous = entityGroupMap.put(entity, dialog);
    if (previous != null) {
      previous.remove();
    }
    UIComponent previousUiComponent = entityUIComponentMap.put(entity, component);
    if (previousUiComponent != null) {
      UIUtils.closeDialog(previousUiComponent);
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
    if (filteredEntityStream(UIComponent.class).anyMatch(this::pausesGame)) {
      if (!ipaused) pauseGame();
    } else {
      if (ipaused) unpauseGame();
    }

    // clean up any entities that no longer have a UIComponent
    entityGroupMap.keySet().removeIf(entity -> !entity.isPresent(UIComponent.class));
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
