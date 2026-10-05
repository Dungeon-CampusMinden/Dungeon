package rooms.lasthour.level;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.network.messages.s2c.ItemState;
import engine.utils.Point;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import rooms.lasthour.save.LastHourSave;

/** Processes player lifecycle events and restores cached player state on each server ECS tick. */
public final class LastHourPlayerStateSystem extends engine.System {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(LastHourPlayerStateSystem.class);
  private final Map<String, PlayerState> players = new LinkedHashMap<>();
  private final Queue<PlayerEvent> events = new ConcurrentLinkedQueue<>();
  private RuntimeException updateFailure;

  /** Creates the server-side observer for player joins and disconnects. */
  public LastHourPlayerStateSystem() {
    super(PlayerComponent.class);
    onEntityAdd = entity -> enqueue(entity, true);
    onEntityRemove =
        entity -> {
          // System/filter removal is not a disconnect. Detached entities retain their components.
          if (Game.findEntityById(entity.id()).isEmpty()) {
            enqueue(entity, false);
          }
        };
  }

  void restore(List<LastHourSave.PlayerData> players) {
    players.forEach(
        player -> {
          PlayerState state = stateFor(player.name());
          state.snapshot = player;
          state.restorePending = true;
        });
  }

  @Override
  public void execute() {
    // Network callbacks only enqueue entities; snapshot/restore happens on the server tick.
    try {
      processEvents();
      restorePendingPlayers();
      updateFailure = null;
    } catch (IllegalArgumentException | IllegalStateException exception) {
      if (updateFailure == null) {
        LOGGER.warn("Could not update The Last Hour player state; will retry", exception);
      }
      updateFailure = exception;
    }
  }

  private void processEvents() {
    PlayerEvent event;
    while ((event = events.peek()) != null) {
      PlayerState state = stateFor(event.name());
      if (event.joined()) onJoin(state, event.entity());
      else onDisconnect(state, event.entity());
      // Keep a failed event at the head so the next ECS tick can retry it.
      events.poll();
    }
  }

  private void restorePendingPlayers() {
    players.values().stream()
        .filter(state -> state.activeEntity != null && state.restorePending)
        .filter(
            state ->
                Game.findEntityById(state.activeEntity.id()).orElse(null) == state.activeEntity)
        .forEach(
            state -> {
              if (restorePlayer(state.activeEntity, state.snapshot)) state.restorePending = false;
            });
  }

  private void enqueue(Entity player, boolean joined) {
    player
        .fetch(PlayerComponent.class)
        .ifPresent(identity -> events.add(new PlayerEvent(identity.playerName(), player, joined)));
  }

  private PlayerState stateFor(String name) {
    return players.computeIfAbsent(name, ignored -> new PlayerState());
  }

  private void onJoin(PlayerState state, Entity player) {
    // The engine reuses this entity for a session reconnect; its live inventory is intact.
    if (state.disconnectedEntity == player) state.restorePending = false;
    state.disconnectedEntity = null;
    state.activeEntity = player;
  }

  private void onDisconnect(PlayerState state, Entity player) {
    if (state.activeEntity != player) return;
    if (!state.restorePending) {
      state.snapshot =
          snapshot(player)
              .orElseThrow(
                  () -> new IllegalStateException("Disconnected player is not ready: " + player));
      state.disconnectedEntity = player;
      state.restorePending = true;
    }
    state.activeEntity = null;
  }

  List<LastHourSave.PlayerData> capture() {
    requireUpdatedState();
    players.values().stream()
        .filter(state -> state.activeEntity != null && !state.restorePending)
        .forEach(
            state -> {
              // A disconnect can remove the entity from the ECS before its callback is queued.
              snapshot(state.activeEntity).ifPresent(saved -> state.snapshot = saved);
            });
    requireUpdatedState();
    return players.values().stream()
        .map(state -> state.snapshot)
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(LastHourSave.PlayerData::name))
        .toList();
  }

  private void requireUpdatedState() {
    if (!isUpdated()) {
      throw new IllegalStateException("Player state update is incomplete", updateFailure);
    }
  }

  boolean isUpdated() {
    return events.isEmpty() && updateFailure == null;
  }

  boolean isReadyForIntro(Entity player) {
    if (!isUpdated()) return false;
    PlayerState state = players.get(player.fetch(PlayerComponent.class).orElseThrow().playerName());
    return state != null && state.activeEntity == player && !state.restorePending;
  }

  private boolean restorePlayer(Entity player, LastHourSave.PlayerData saved) {
    PositionComponent position = player.fetch(PositionComponent.class).orElse(null);
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElse(null);
    if (position == null || inventory == null) return false;
    int capacity = inventory.items().length;
    if (saved.items().stream().anyMatch(item -> item.slot() >= capacity)) return false;
    List<Item> restoredItems = saved.items().stream().map(item -> item.state().toItem()).toList();
    position.position(new Point(saved.x(), saved.y()));
    for (int slot = 0; slot < capacity; slot++) inventory.set(slot, null);
    for (int index = 0; index < saved.items().size(); index++) {
      inventory.set(saved.items().get(index).slot(), restoredItems.get(index));
    }
    if (saved.introShown()) LastHourLevel.INTRO_SHOWN_TO.add(player.id());
    return true;
  }

  private Optional<LastHourSave.PlayerData> snapshot(Entity player) {
    PositionComponent position = player.fetch(PositionComponent.class).orElse(null);
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElse(null);
    if (position == null
        || PositionComponent.ILLEGAL_POSITION.equals(position.position())
        || inventory == null) {
      return Optional.empty();
    }
    List<LastHourSave.ItemData> items = new ArrayList<>();
    Item[] slots = inventory.items();
    for (int slot = 0; slot < slots.length; slot++) {
      if (slots[slot] != null) {
        items.add(new LastHourSave.ItemData(slot, ItemState.fromItem(slots[slot])));
      }
    }
    String name = player.fetch(PlayerComponent.class).orElseThrow().playerName();
    return Optional.of(
        new LastHourSave.PlayerData(
            name,
            position.position().x(),
            position.position().y(),
            LastHourLevel.INTRO_SHOWN_TO.contains(player.id()),
            items));
  }

  private static final class PlayerState {
    private LastHourSave.PlayerData snapshot;
    private Entity activeEntity;
    private Entity disconnectedEntity;
    private boolean restorePending;
  }

  private record PlayerEvent(String name, Entity entity, boolean joined) {}
}
