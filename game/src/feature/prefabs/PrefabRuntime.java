package feature.prefabs;

import engine.Entity;
import engine.Game;
import engine.game.PreRunConfiguration;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.network.messages.s2c.PrefabChangeMessage;
import engine.utils.logging.DungeonLogger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
  private static final int MAX_CHAIN_LENGTH = 32;

  // Actions currently running, innermost last. Used to stop actions from triggering themselves.
  private static Deque<String> actionChain = new ArrayDeque<>();

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
   * Raises an event of an instance: notifies its listeners and runs the actions linked to it.
   *
   * <p>Failing listeners and actions are logged and do not stop the others. Linked actions only run
   * where the game has authority, i.e. on the server or in singleplayer. While prefabs are being
   * spawned, linked actions are delayed until spawning finished, so they can reach instances that
   * are spawned later.
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
    if (Game.isMultiplayerClient()) return;
    Optional<Entity> player = event.player(payload);
    List<PrefabWiring.Connection> targets =
        PrefabWiring.outgoing(level.activePrefabs(), name, event.key()).stream()
            .filter(connection -> connection.input().kind() == PrefabPort.Kind.ACTION)
            .toList();
    for (PrefabWiring.Connection target : targets) {
      dungeonLevel.prefabRuntimeState().recordSignal(signalKey(target));
      runAction(level, target.receiver().name(), target.input().key(), player);
    }
  }

  /**
   * Runs an action of an instance, as if a linked event was raised.
   *
   * <p>Lifecycle actions also run for inactive instances, other actions are ignored while the
   * instance is inactive. An action that is reached again while it is still running, e.g. through
   * two prefabs triggering each other, is not run again and the loop is logged.
   *
   * @param level current level
   * @param name name of the receiving instance
   * @param action key of the action
   * @param player player that caused the action, if any
   * @return true if the instance and action exist and the action was run or scheduled
   */
  public static boolean runAction(
      ILevel level, String name, String action, Optional<Entity> player) {
    if (Game.isMultiplayerClient()) {
      throw new IllegalStateException("Prefab actions can only be run on the server");
    }
    if (find(level, name).isEmpty()) return false;
    if (level instanceof DungeonLevel dungeonLevel) {
      dungeonLevel.prefabRuntimeState().recordSignal(name + "#" + action);
    }
    if (PrefabSpawner.isBatching()) {
      Deque<String> chain = new ArrayDeque<>(actionChain);
      PrefabSpawner.afterChanges(
          new Object(),
          () -> {
            Deque<String> outer = actionChain;
            actionChain = chain;
            try {
              runActionNow(level, name, action, player);
            } finally {
              actionChain = outer;
            }
          });
      return true;
    }
    return runActionNow(level, name, action, player);
  }

  private static boolean runActionNow(
      ILevel level, String name, String actionKey, Optional<Entity> player) {
    Optional<PrefabInstance> current = find(level, name);
    if (current.isEmpty()) return false;
    Prefab prefab = PrefabRegistry.require(current.get().type());
    if (!(prefab.port(actionKey).orElse(null) instanceof PrefabAction action)) return false;
    String step = name + "." + actionKey;
    if (actionChain.contains(step) || actionChain.size() >= MAX_CHAIN_LENGTH) {
      LOGGER.error(
          "Stopped prefab action loop: " + String.join(" -> ", actionChain) + " -> " + step);
      return false;
    }
    actionChain.addLast(step);
    try {
      PrefabInstance instance = prefab.normalize(current.get());
      if (action == Prefab.ACTIVATE) {
        if (!instance.active()) update(level, name, i -> i.withActive(true));
      } else if (action == Prefab.DEACTIVATE) {
        if (instance.active()) update(level, name, i -> i.withActive(false));
      } else if (action == Prefab.RESET) {
        if (instance.active()) reset(level, name);
      } else if (instance.active()) {
        action.run(new ActionContext(level, prefab, instance, player));
      }
      return true;
    } catch (RuntimeException e) {
      LOGGER.error("Prefab action '" + step + "' failed", e);
      return false;
    } finally {
      actionChain.removeLast();
    }
  }

  /**
   * Returns the identity of a link used for {@link PrefabRuntimeState#recordSignal}.
   *
   * @param connection link and its receiver
   * @return signal key
   */
  public static String signalKey(PrefabWiring.Connection connection) {
    return connection.receiver().name()
        + "#"
        + connection.input().key()
        + "<"
        + connection.link().source()
        + "#"
        + connection.link().port();
  }

  /**
   * Publishes the value of an artifact output and notifies the linked consumers.
   *
   * @param level owning level
   * @param name name of the producing instance
   * @param output artifact output of the producer's type
   * @param value produced value
   * @param <T> value type
   */
  public static <T> void publish(ILevel level, String name, ArtifactOutput<T> output, T value) {
    Objects.requireNonNull(value, "value");
    if (!(level instanceof DungeonLevel dungeonLevel)) return;
    dungeonLevel.prefabRuntimeState().putArtifact(name, output.key(), value);
    notifyArtifactConsumers(level, name, output.key());
  }

  /**
   * Returns the value an instance currently publishes on an artifact output.
   *
   * @param level owning level
   * @param name name of the producing instance
   * @param output artifact output of the producer's type
   * @param <T> value type
   * @return the value, if published
   */
  public static <T> Optional<T> artifact(ILevel level, String name, ArtifactOutput<T> output) {
    if (!(level instanceof DungeonLevel dungeonLevel)) return Optional.empty();
    Object value = dungeonLevel.prefabRuntimeState().artifact(name, output.key());
    return output.type().convert(value, output.type());
  }

  /**
   * Resolves the current value of a link into an artifact slot.
   *
   * @param level owning level
   * @param link link stored on the consumer
   * @param slot slot of the consumer
   * @param <T> value type
   * @return the converted value, or empty if it is not available or not accepted
   */
  static <T> Optional<T> artifactValue(ILevel level, PrefabLink link, ArtifactSlot<T> slot) {
    if (!(level instanceof DungeonLevel dungeonLevel)) return Optional.empty();
    Optional<PrefabPort> port = PrefabWiring.outputPort(level.activePrefabs(), link);
    if (port.isEmpty() || !(port.get() instanceof ArtifactOutput<?> output)) {
      return Optional.empty();
    }
    Object raw = dungeonLevel.prefabRuntimeState().artifact(link.source(), link.port());
    Optional<T> value = output.type().convert(raw, slot.type());
    if (value.isPresent() && !slot.accepts(value.get())) {
      LOGGER.debug(
          "Artifact "
              + link
              + " is not accepted by slot '"
              + slot.key()
              + "': "
              + slot.validationMessage().orElse("invalid value"));
      return Optional.empty();
    }
    return value;
  }

  static void listenToArtifacts(ILevel level, String consumer, Runnable listener) {
    if (level instanceof DungeonLevel dungeonLevel) {
      dungeonLevel.prefabRuntimeState().addArtifactListener(consumer, listener);
    }
  }

  /**
   * Drops the published values and artifact listeners of a despawned instance.
   *
   * @param level owning level
   * @param name name of the despawned instance
   */
  static void despawned(ILevel level, String name) {
    if (!(level instanceof DungeonLevel dungeonLevel)) return;
    PrefabRuntimeState state = dungeonLevel.prefabRuntimeState();
    state.clearArtifactListeners(name);
    if (state.clearArtifacts(name)) notifyArtifactConsumers(level, name, null);
  }

  private static void notifyArtifactConsumers(ILevel level, String name, String port) {
    if (!(level instanceof DungeonLevel dungeonLevel)) return;
    PrefabRuntimeState state = dungeonLevel.prefabRuntimeState();
    Set<String> consumers = new LinkedHashSet<>();
    for (PrefabWiring.Connection connection : PrefabWiring.outgoing(level.activePrefabs(), name)) {
      if (connection.input().kind() != PrefabPort.Kind.ARTIFACT_SLOT) continue;
      if (port != null && !connection.link().port().equals(port)) continue;
      consumers.add(connection.receiver().name());
    }
    for (String consumer : consumers) {
      for (Runnable listener : state.artifactListeners(consumer)) {
        try {
          listener.run();
        } catch (RuntimeException e) {
          LOGGER.error("Artifact listener of '" + consumer + "' failed", e);
        }
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
        JsonNode record = MAPPER.readTree(message.instanceJson());
        if (!(record instanceof ObjectNode object)) {
          throw new IllegalArgumentException("Prefab change record must be a JSON object");
        }
        PrefabInstance instance =
            PrefabInstanceCodec.decode(
                object, warning -> LOGGER.warn(message.name() + ": " + warning));
        putLocally(level, instance, sides);
      }
      case REMOVE -> find(level, message.name()).ifPresent(i -> removeLocally(level, i, sides));
      case RESET -> find(level, message.name()).ifPresent(i -> respawnLocally(level, i, sides));
    }
  }

  private static void put(ILevel level, PrefabInstance instance) {
    putLocally(level, instance, PrefabSide.localSides());
    broadcast(
        new PrefabChangeMessage(
            PrefabChangeMessage.Action.PUT,
            instance.name(),
            instance.type(),
            MAPPER.writeValueAsString(PrefabInstanceCodec.encode(MAPPER, instance))));
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
