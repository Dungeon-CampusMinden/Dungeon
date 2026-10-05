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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import rooms.lasthour.save.LastHourSave;

/** Observes player lifecycle events; the room tick advances the state before saving. */
final class LastHourPlayerStateSystem extends engine.System {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(LastHourPlayerStateSystem.class);
  private final Map<String, LastHourSave.PlayerData> snapshots = new LinkedHashMap<>();
  private final Map<String, Integer> playerEntities = new HashMap<>();
  private final Map<String, Entity> disconnectedPlayers = new HashMap<>();
  private final Set<String> pendingRestores = new HashSet<>();
  private final Queue<PlayerEvent> events = new ConcurrentLinkedQueue<>();
  private RuntimeException updateFailure;

  LastHourPlayerStateSystem() {
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
          snapshots.put(player.name(), player);
          pendingRestores.add(player.name());
        });
  }

  @Override
  public void execute() {
    // Lifecycle-only observer; LastHourLevel calls update() before saving.
  }

  void update() {
    // Network callbacks only enqueue entities; snapshot/restore happens on the server tick.
    try {
      PlayerEvent event;
      while ((event = events.peek()) != null) {
        process(event);
        // Keep a failed event at the head so the next room tick can retry it.
        events.poll();
      }
      Game.allPlayers()
          .forEach(
              player -> {
                String name = player.fetch(PlayerComponent.class).orElseThrow().playerName();
                if (!Integer.valueOf(player.id()).equals(playerEntities.get(name))) return;
                if (pendingRestores.contains(name) && restorePlayer(player, snapshots.get(name))) {
                  pendingRestores.remove(name);
                }
              });
      updateFailure = null;
    } catch (IllegalArgumentException | IllegalStateException exception) {
      if (updateFailure == null) {
        LOGGER.warn("Could not update The Last Hour player state; will retry", exception);
      }
      updateFailure = exception;
    }
  }

  private void enqueue(Entity player, boolean joined) {
    player
        .fetch(PlayerComponent.class)
        .ifPresent(identity -> events.add(new PlayerEvent(identity.playerName(), player, joined)));
  }

  private void process(PlayerEvent event) {
    Entity player = event.entity();
    String name = event.name();
    if (event.joined()) {
      // The engine reuses this entity for a session reconnect; its live inventory is intact.
      if (disconnectedPlayers.remove(name) == player) pendingRestores.remove(name);
      playerEntities.put(name, player.id());
    } else if (Integer.valueOf(player.id()).equals(playerEntities.get(name))
        && !pendingRestores.contains(name)) {
      LastHourSave.PlayerData saved =
          snapshot(player)
              .orElseThrow(
                  () -> new IllegalStateException("Disconnected player is not ready: " + name));
      snapshots.put(name, saved);
      disconnectedPlayers.put(name, player);
      pendingRestores.add(name);
    }
  }

  List<LastHourSave.PlayerData> capture() {
    requireUpdatedState();
    Game.allPlayers()
        .forEach(
            player -> {
              String name = player.fetch(PlayerComponent.class).orElseThrow().playerName();
              if (!Integer.valueOf(player.id()).equals(playerEntities.get(name))
                  || pendingRestores.contains(name)) return;
              snapshot(player).ifPresent(saved -> snapshots.put(name, saved));
            });
    requireUpdatedState();
    return snapshots.values().stream()
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

  private boolean restorePlayer(Entity player, LastHourSave.PlayerData saved) {
    PositionComponent position = player.fetch(PositionComponent.class).orElse(null);
    InventoryComponent inventory = player.fetch(InventoryComponent.class).orElse(null);
    if (position == null
        || inventory == null
        || saved.items().stream().anyMatch(item -> item.slot() >= inventory.items().length)) {
      return false;
    }
    List<Item> restoredItems = saved.items().stream().map(item -> item.state().toItem()).toList();
    position.position(new Point(saved.x(), saved.y()));
    for (int slot = 0; slot < inventory.items().length; slot++) inventory.set(slot, null);
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

  private record PlayerEvent(String name, Entity entity, boolean joined) {}
}
