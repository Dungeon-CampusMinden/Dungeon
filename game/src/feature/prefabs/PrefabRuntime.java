package feature.prefabs;

import engine.Game;
import engine.game.PreRunConfiguration;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.network.messages.s2c.PrefabChangeMessage;
import engine.utils.logging.DungeonLogger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Changes the prefab instances of the running level for the current game session.
 *
 * <p>This is the API for level handlers to create, update, reset and remove prefab instances while
 * the game runs. Changes are never written to the level file and are discarded when the level is
 * loaded again. They must be made on the server (or in singleplayer); client-side prefabs are
 * updated on every client by forwarding the change over the network, and clients joining later
 * receive the current state with the level.
 *
 * <p>Usually this is used through the level ({@link DungeonLevel#spawnPrefab}, {@link
 * ILevel#prefab}, {@link ILevel#prefabs(Class)}) and the bound prefab views ({@link Prefab#reset},
 * {@link Prefab#remove}, {@link Prefab#update}).
 */
public final class PrefabRuntime {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PrefabRuntime.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private PrefabRuntime() {}

  /**
   * Spawns a new prefab instance, starting from the defaults of its type.
   *
   * @param level current level
   * @param prefabClass prefab type to spawn
   * @param name level-unique instance name
   * @param configure sets the instance properties, e.g. {@code i -> i.with(WaterPrefab.REGION, r)}
   * @param <P> prefab type
   * @return bound view of the spawned instance
   * @throws IllegalArgumentException if the name is already used by an active instance, or the
   *     configured properties are invalid
   * @throws IllegalStateException if called on a multiplayer client or for a level that is not the
   *     current level
   */
  public static <P extends Prefab> P spawn(
      ILevel level, Class<P> prefabClass, String name, UnaryOperator<PrefabInstance> configure) {
    Objects.requireNonNull(configure, "configure");
    PrefabInstance instance =
        configure.apply(PrefabRegistry.definition(prefabClass).newInstance(name));
    return prefabClass.cast(spawn(level, instance));
  }

  /**
   * Spawns a new prefab instance.
   *
   * @param level current level
   * @param instance instance to spawn
   * @return bound view of the spawned instance
   * @throws IllegalArgumentException if the name is already used by an active instance, or the
   *     instance is invalid
   * @throws IllegalStateException if called on a multiplayer client or for a level that is not the
   *     current level
   */
  public static Prefab spawn(ILevel level, PrefabInstance instance) {
    requireAuthority(level);
    PrefabInstance normalized = PrefabRegistry.require(instance.type()).normalize(instance);
    if (find(level, normalized.name()).isPresent()) {
      throw new IllegalArgumentException("Duplicate prefab instance name: " + normalized.name());
    }
    put(level, normalized);
    return PrefabRegistry.createView(level, normalized);
  }

  /**
   * Replaces the data of an active prefab instance and respawns it.
   *
   * @param level current level
   * @param name name of the instance to update
   * @param change creates the new instance data from the current one, e.g. {@code i ->
   *     i.with(WaterPrefab.COLOR, color)}. Name and type must not change.
   * @throws IllegalArgumentException if no instance with this name is active, or the changed data
   *     is invalid
   * @throws IllegalStateException if called on a multiplayer client or for a level that is not the
   *     current level
   */
  public static void update(ILevel level, String name, UnaryOperator<PrefabInstance> change) {
    requireAuthority(level);
    PrefabInstance current =
        find(level, name)
            .orElseThrow(
                () -> new IllegalArgumentException("No active prefab instance named: " + name));
    PrefabInstance changed = Objects.requireNonNull(change.apply(current), "changed instance");
    if (!changed.name().equals(current.name()) || !changed.type().equals(current.type())) {
      throw new IllegalArgumentException("Prefab updates must not change name or type");
    }
    put(level, PrefabRegistry.require(changed.type()).normalize(changed));
  }

  /**
   * Removes an active prefab instance and its entities for the rest of the game session.
   *
   * <p>Listeners registered for the instance are dropped as well.
   *
   * @param level current level
   * @param name name of the instance to remove
   * @return true if an active instance was removed
   * @throws IllegalStateException if called on a multiplayer client or for a level that is not the
   *     current level
   */
  public static boolean remove(ILevel level, String name) {
    requireAuthority(level);
    Optional<PrefabInstance> current = find(level, name);
    if (current.isEmpty()) return false;
    removeLocally(level, current.get(), PrefabSide.localSides());
    broadcast(new PrefabChangeMessage(PrefabChangeMessage.Action.REMOVE, name, "", ""));
    return true;
  }

  /**
   * Respawns an active prefab instance from its current data, discarding all runtime state of its
   * entities, e.g. moving pushed stones back to their start position.
   *
   * @param level current level
   * @param name name of the instance to reset
   * @return true if an active instance was reset
   * @throws IllegalStateException if called on a multiplayer client or for a level that is not the
   *     current level
   */
  public static boolean reset(ILevel level, String name) {
    requireAuthority(level);
    Optional<PrefabInstance> current = find(level, name);
    if (current.isEmpty()) return false;
    respawnLocally(level, current.get(), PrefabSide.localSides());
    broadcast(new PrefabChangeMessage(PrefabChangeMessage.Action.RESET, name, "", ""));
    return true;
  }

  /**
   * Returns the active instance with the given name.
   *
   * @param level level to search
   * @param name instance name
   * @return the active instance, if any
   */
  public static Optional<PrefabInstance> find(ILevel level, String name) {
    PrefabInstance result = null;
    for (PrefabInstance instance : level.activePrefabs()) {
      if (instance.name().equals(name)) result = instance;
    }
    return Optional.ofNullable(result);
  }

  /**
   * Registers a listener for an event of one prefab instance.
   *
   * <p>The listener runs on the game instance that raises the event, which is the server for
   * server-side prefabs.
   *
   * @param level owning level
   * @param name instance name
   * @param event event to listen to
   * @param listener called with the event payload
   * @param <T> payload type
   */
  public static <T> void listen(
      ILevel level, String name, PrefabEvent<T> event, Consumer<? super T> listener) {
    stateOf(level).addListener(name, event, listener);
  }

  /**
   * Notifies the listeners of an instance about an event. Failing listeners are logged and do not
   * stop the others.
   *
   * @param level owning level
   * @param name instance name
   * @param event raised event
   * @param payload event payload
   * @param <T> payload type
   */
  public static <T> void fire(ILevel level, String name, PrefabEvent<T> event, T payload) {
    if (!(level instanceof DungeonLevel dungeonLevel)) return;
    for (Consumer<? super T> listener : dungeonLevel.prefabRuntimeState().listeners(name, event)) {
      try {
        listener.accept(payload);
      } catch (RuntimeException e) {
        LOGGER.error("Prefab listener for '" + name + "' (" + event + ") failed", e);
      }
    }
  }

  /**
   * Applies a runtime prefab change received from the server to the client-side prefabs of the
   * current level.
   *
   * @param message received change
   */
  public static void apply(PrefabChangeMessage message) {
    ILevel level = Game.currentLevel().orElse(null);
    if (level == null) return;
    PrefabSide[] sides = PrefabSide.localSides();
    switch (message.action()) {
      case PUT -> {
        Map<String, JsonNode> properties = new LinkedHashMap<>();
        if (!message.propertiesJson().isEmpty()) {
          MAPPER
              .readTree(message.propertiesJson())
              .properties()
              .forEach(entry -> properties.put(entry.getKey(), entry.getValue()));
        }
        PrefabInstance instance =
            PrefabRegistry.require(message.type())
                .normalize(new PrefabInstance(message.name(), message.type(), properties));
        putLocally(level, instance, sides);
      }
      case REMOVE -> find(level, message.name()).ifPresent(i -> removeLocally(level, i, sides));
      case RESET -> find(level, message.name()).ifPresent(i -> respawnLocally(level, i, sides));
    }
  }

  private static void put(ILevel level, PrefabInstance instance) {
    putLocally(level, instance, PrefabSide.localSides());
    ObjectNode properties = MAPPER.createObjectNode();
    instance.properties().forEach(properties::set);
    broadcast(
        new PrefabChangeMessage(
            PrefabChangeMessage.Action.PUT,
            instance.name(),
            instance.type(),
            MAPPER.writeValueAsString(properties)));
  }

  private static void putLocally(ILevel level, PrefabInstance instance, PrefabSide[] sides) {
    PrefabRuntimeState state = stateOf(level);
    PrefabRuntimeState.Entry previous = state.entry(instance.name());
    Optional<PrefabInstance> previousInstance = find(level, instance.name());
    state.put(instance);
    try {
      respawnLocally(level, instance, sides);
    } catch (RuntimeException e) {
      state.restore(instance.name(), previous);
      try {
        previousInstance.ifPresent(old -> respawnLocally(level, old, sides));
      } catch (RuntimeException rollbackFailure) {
        e.addSuppressed(rollbackFailure);
      }
      throw e;
    }
  }

  private static void removeLocally(ILevel level, PrefabInstance instance, PrefabSide[] sides) {
    stateOf(level).remove(instance.name());
    PrefabSpawner.batch(
        () -> {
          for (PrefabSide side : sides) PrefabSpawner.despawnInstance(level, instance, side);
        });
  }

  private static void respawnLocally(ILevel level, PrefabInstance instance, PrefabSide[] sides) {
    PrefabSpawner.batch(
        () -> {
          for (PrefabSide side : sides) PrefabSpawner.spawnInstance(level, instance, side);
        });
  }

  private static void broadcast(PrefabChangeMessage message) {
    if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
      Game.network().broadcast(message, true);
    }
  }

  private static void requireAuthority(ILevel level) {
    Objects.requireNonNull(level, "level");
    if (Game.isMultiplayerClient()) {
      throw new IllegalStateException("Runtime prefab changes can only be made on the server");
    }
    if (Game.currentLevel().filter(current -> current == level).isEmpty()) {
      throw new IllegalStateException("Runtime prefab changes require the level to be loaded");
    }
  }

  private static PrefabRuntimeState stateOf(ILevel level) {
    if (level instanceof DungeonLevel dungeonLevel) return dungeonLevel.prefabRuntimeState();
    throw new IllegalArgumentException("Runtime prefab changes require a DungeonLevel");
  }
}
