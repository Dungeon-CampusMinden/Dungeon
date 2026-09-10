package feature.shader;

import engine.Game;
import engine.System;
import engine.game.PreRunConfiguration;
import engine.network.NetworkUtils;
import engine.network.codec.ShaderComponentCodec;
import engine.network.messages.s2c.ShaderTargetStateMessage;
import engine.network.messages.s2c.ShaderTargetStateMessage.Target;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Manages authoritative scene, level, and depth-layer shader assignments.
 *
 * <p>In multiplayer, assignments are sent to all clients or to the clients controlling the supplied
 * target entities. Clients apply the received state to local holder entities through {@link
 * ShaderSyncSystem}.
 */
public final class ShaderSystem extends System {
  private static ShaderSystem INSTANCE;

  private final Map<TargetKey, ShaderComponent> globalShaders = new HashMap<>();
  private final Map<Short, Map<TargetKey, ShaderComponent>> clientShaders = new HashMap<>();

  /**
   * Gets the singleton instance of the ShaderSystem, creating it if necessary.
   *
   * @return the ShaderSystem singleton
   */
  public static ShaderSystem getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ShaderSystem();
    }
    return INSTANCE;
  }

  /** Creates the server-side shader assignment system. */
  private ShaderSystem() {
    super(AuthoritativeSide.SERVER);
    INSTANCE = this;
  }

  @Override
  public void execute() {
    // Shader assignments are updated through the public API and resynchronized on connect.
  }

  /**
   * Adds or replaces a scene shader for all clients.
   *
   * @param identifier shader identifier
   * @param order render order
   * @param shader shader instance
   */
  public void addSceneShader(String identifier, int order, AbstractShader shader) {
    addSceneShader(identifier, order, shader, new int[0]);
  }

  /**
   * Adds or replaces a scene shader for clients controlling the target entities. An empty target
   * list targets all clients.
   *
   * @param identifier shader identifier
   * @param order render order
   * @param shader shader instance
   * @param targetEntityIds target player/entity IDs, or empty for all clients
   */
  public void addSceneShader(
      String identifier, int order, AbstractShader shader, int... targetEntityIds) {
    addShader(new TargetKey(Target.SCENE, 0), identifier, order, shader, targetEntityIds);
  }

  /**
   * Adds or replaces a level shader for all clients.
   *
   * @param identifier shader identifier
   * @param order render order
   * @param shader shader instance
   */
  public void addLevelShader(String identifier, int order, AbstractShader shader) {
    addLevelShader(identifier, order, shader, new int[0]);
  }

  /**
   * Adds or replaces a level shader for clients controlling the target entities. An empty target
   * list targets all clients.
   *
   * @param identifier shader identifier
   * @param order render order
   * @param shader shader instance
   * @param targetEntityIds target player/entity IDs, or empty for all clients
   */
  public void addLevelShader(
      String identifier, int order, AbstractShader shader, int... targetEntityIds) {
    addShader(new TargetKey(Target.LEVEL, 0), identifier, order, shader, targetEntityIds);
  }

  /**
   * Adds or replaces a depth-layer shader for all clients.
   *
   * @param depth the DrawSystem depth layer
   * @param identifier shader identifier
   * @param order render order
   * @param shader shader instance
   */
  public void addDepthLayerShader(int depth, String identifier, int order, AbstractShader shader) {
    addDepthLayerShader(depth, identifier, order, shader, new int[0]);
  }

  /**
   * Adds or replaces a depth-layer shader for clients controlling the target entities. An empty
   * target list targets all clients.
   *
   * @param depth the DrawSystem depth layer
   * @param identifier shader identifier
   * @param order render order
   * @param shader shader instance
   * @param targetEntityIds target player/entity IDs, or empty for all clients
   */
  public void addDepthLayerShader(
      int depth, String identifier, int order, AbstractShader shader, int... targetEntityIds) {
    addShader(new TargetKey(Target.DEPTH_LAYER, depth), identifier, order, shader, targetEntityIds);
  }

  /**
   * Removes a scene shader for all clients.
   *
   * @param identifier shader identifier
   * @return true when a shader was removed
   */
  public boolean removeSceneShader(String identifier) {
    return removeSceneShader(identifier, new int[0]);
  }

  /**
   * Removes a scene shader for clients controlling the target entities. An empty target list
   * targets all clients.
   *
   * @param identifier shader identifier
   * @param targetEntityIds target player/entity IDs, or empty for all clients
   * @return true when a shader was removed
   */
  public boolean removeSceneShader(String identifier, int... targetEntityIds) {
    return removeShader(new TargetKey(Target.SCENE, 0), identifier, targetEntityIds);
  }

  /**
   * Removes a level shader for all clients.
   *
   * @param identifier shader identifier
   * @return true when a shader was removed
   */
  public boolean removeLevelShader(String identifier) {
    return removeLevelShader(identifier, new int[0]);
  }

  /**
   * Removes a level shader for clients controlling the target entities. An empty target list
   * targets all clients.
   *
   * @param identifier shader identifier
   * @param targetEntityIds target player/entity IDs, or empty for all clients
   * @return true when a shader was removed
   */
  public boolean removeLevelShader(String identifier, int... targetEntityIds) {
    return removeShader(new TargetKey(Target.LEVEL, 0), identifier, targetEntityIds);
  }

  /**
   * Removes a depth-layer shader for all clients.
   *
   * @param depth the DrawSystem depth layer
   * @param identifier shader identifier
   * @return true when a shader was removed
   */
  public boolean removeDepthLayerShader(int depth, String identifier) {
    return removeDepthLayerShader(depth, identifier, new int[0]);
  }

  /**
   * Removes a depth-layer shader for clients controlling the target entities. An empty target list
   * targets all clients.
   *
   * @param depth the DrawSystem depth layer
   * @param identifier shader identifier
   * @param targetEntityIds target player/entity IDs, or empty for all clients
   * @return true when a shader was removed
   */
  public boolean removeDepthLayerShader(int depth, String identifier, int... targetEntityIds) {
    return removeShader(new TargetKey(Target.DEPTH_LAYER, depth), identifier, targetEntityIds);
  }

  /**
   * Returns the currently assigned scene shaders.
   *
   * @return the global scene shader component
   */
  public ShaderComponent sceneShaders() {
    return globalShaders.getOrDefault(new TargetKey(Target.SCENE, 0), new ShaderComponent());
  }

  /**
   * Returns the currently assigned level shaders.
   *
   * @return the global level shader component
   */
  public ShaderComponent levelShaders() {
    return globalShaders.getOrDefault(new TargetKey(Target.LEVEL, 0), new ShaderComponent());
  }

  /**
   * Returns the currently assigned depth-layer shaders.
   *
   * @param depth the DrawSystem depth layer
   * @return the global depth-layer shader component
   */
  public ShaderComponent depthLayerShaders(int depth) {
    return globalShaders.getOrDefault(
        new TargetKey(Target.DEPTH_LAYER, depth), new ShaderComponent());
  }

  /**
   * Resends all shader assignments that apply to a client.
   *
   * @param clientId the client to resynchronize
   */
  public void resyncToClient(short clientId) {
    Map<TargetKey, ShaderComponent> assignments = new HashMap<>(globalShaders);
    assignments.putAll(clientShaders.getOrDefault(clientId, Map.of()));
    assignments.forEach((target, component) -> send(clientId, target, component));
  }

  /** Clears all authoritative shader assignments and client-side projections in singleplayer. */
  public void clear() {
    Set<TargetKey> targets = new HashSet<>(globalShaders.keySet());
    clientShaders.values().forEach(assignments -> targets.addAll(assignments.keySet()));

    if (isNetworkServer()) {
      targets.forEach(
          target ->
              NetworkUtils.getAllConnectedClientIds()
                  .forEach(clientId -> send(clientId, target, new ShaderComponent())));
    } else {
      Game.system(ShaderSyncSystem.class, ShaderSyncSystem::clearTargetShaders);
    }

    globalShaders.clear();
    clientShaders.clear();
  }

  private void addShader(
      TargetKey target,
      String identifier,
      int order,
      AbstractShader shader,
      int[] targetEntityIds) {
    if (targetEntityIds.length == 0) {
      ShaderComponent updated =
          globalShaders
              .getOrDefault(target, new ShaderComponent())
              .withShader(identifier, order, shader);
      globalShaders.put(target, updated);
      if (isNetworkServer()) {
        NetworkUtils.getAllConnectedClientIds()
            .forEach(
                clientId -> {
                  clientShaders
                      .computeIfAbsent(clientId, ignored -> new HashMap<>())
                      .put(target, updated);
                  send(clientId, target, updated);
                });
      } else {
        applyLocally(target, updated);
      }
      return;
    }

    for (short clientId : NetworkUtils.entityIdsToClientIds(targetEntityIds)) {
      ShaderComponent updated =
          clientShaders
              .computeIfAbsent(clientId, ignored -> new HashMap<>())
              .getOrDefault(target, globalShaders.getOrDefault(target, new ShaderComponent()))
              .withShader(identifier, order, shader);
      clientShaders.get(clientId).put(target, updated);
      send(clientId, target, updated);
    }

    if (!PreRunConfiguration.multiplayerEnabled() && containsCurrentEntity(targetEntityIds)) {
      ShaderComponent updated =
          globalShaders
              .getOrDefault(target, new ShaderComponent())
              .withShader(identifier, order, shader);
      globalShaders.put(target, updated);
      applyLocally(target, updated);
    }
  }

  private boolean removeShader(TargetKey target, String identifier, int[] targetEntityIds) {
    if (targetEntityIds.length == 0) {
      ShaderComponent current = globalShaders.get(target);
      if (current == null
          || current.shaders().stream().noneMatch(entry -> entry.identifier().equals(identifier))) {
        return false;
      }
      ShaderComponent updated = current.withoutShader(identifier);
      globalShaders.put(target, updated);
      if (isNetworkServer()) {
        NetworkUtils.getAllConnectedClientIds()
            .forEach(
                clientId -> {
                  clientShaders
                      .computeIfAbsent(clientId, ignored -> new HashMap<>())
                      .put(target, updated);
                  send(clientId, target, updated);
                });
      } else {
        applyLocally(target, updated);
      }
      return true;
    }

    boolean removed = false;
    for (short clientId : NetworkUtils.entityIdsToClientIds(targetEntityIds)) {
      Map<TargetKey, ShaderComponent> assignments =
          clientShaders.computeIfAbsent(clientId, ignored -> new HashMap<>());
      ShaderComponent current =
          assignments.getOrDefault(
              target, globalShaders.getOrDefault(target, new ShaderComponent()));
      if (current.shaders().stream().anyMatch(entry -> entry.identifier().equals(identifier))) {
        ShaderComponent updated = current.withoutShader(identifier);
        assignments.put(target, updated);
        send(clientId, target, updated);
        removed = true;
      }
    }

    if (!PreRunConfiguration.multiplayerEnabled() && containsCurrentEntity(targetEntityIds)) {
      ShaderComponent current = globalShaders.get(target);
      if (current != null
          && current.shaders().stream().anyMatch(entry -> entry.identifier().equals(identifier))) {
        ShaderComponent updated = current.withoutShader(identifier);
        globalShaders.put(target, updated);
        applyLocally(target, updated);
        removed = true;
      }
    }
    return removed;
  }

  private void applyLocally(TargetKey target, ShaderComponent component) {
    Game.system(
        ShaderSyncSystem.class,
        sync ->
            sync.applyTargetState(
                new ShaderTargetStateMessage(
                    target.target(), target.depth(), ShaderComponentCodec.toState(component))));
  }

  private void send(short clientId, TargetKey target, ShaderComponent component) {
    if (!isNetworkServer()) {
      return;
    }
    Game.network()
        .send(
            clientId,
            new ShaderTargetStateMessage(
                target.target(), target.depth(), ShaderComponentCodec.toState(component)),
            true);
  }

  private boolean isNetworkServer() {
    return PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer();
  }

  private boolean containsCurrentEntity(int[] targetEntityIds) {
    for (int targetEntityId : targetEntityIds) {
      if (Game.findEntityById(targetEntityId).isPresent()) {
        return true;
      }
    }
    return false;
  }

  private record TargetKey(Target target, int depth) {}
}
