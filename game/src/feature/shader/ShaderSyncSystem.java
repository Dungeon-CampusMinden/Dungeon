package feature.shader;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.DrawComponent;
import engine.network.messages.s2c.ShaderTargetStateMessage;
import engine.network.messages.s2c.ShaderTargetStateMessage.Target;
import engine.network.codec.ShaderComponentCodec;
import engine.systems.DrawSystem;
import engine.utils.components.draw.shader.AbstractShader;
import engine.utils.components.draw.shader.ShaderList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Synchronizes {@link ShaderComponent} declarations into runtime draw shader lists.
 *
 * <p>The component is the source of truth for identifiers and ordering. Runtime shader instances
 * remain in {@link DrawComponent} because that is the collection consumed by {@code DrawSystem}.
 */
public final class ShaderSyncSystem extends System {
  private final Map<Integer, Map<String, AppliedShader>> appliedShaders = new HashMap<>();
  private final Map<String, Map<String, AppliedShader>> appliedTargetShaders = new HashMap<>();
  private final Map<Integer, Entity> depthLayerShaderEntities = new HashMap<>();
  private Entity sceneShaderEntity;
  private Entity levelShaderEntity;

  /** Creates the client-side shader synchronization system. */
  public ShaderSyncSystem() {
    super(AuthoritativeSide.CLIENT, DrawComponent.class, ShaderComponent.class);
    onEntityAdd = this::synchronize;
    onEntityRemove = this::removeSynchronizedShaders;
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(this::synchronize);
    synchronizeTargetEntities();
  }

  /**
   * Applies a complete server assignment to the local holder for a managed shader target.
   *
   * @param message the server assignment
   */
  public void applyTargetState(ShaderTargetStateMessage message) {
    targetEntity(message.target(), message.depth())
        .add(ShaderComponentCodec.fromState(message.shaderComponent()));
  }

  /** Clears all local managed shader target holders. */
  public void clearTargetShaders() {
    if (sceneShaderEntity != null) {
      sceneShaderEntity.remove(ShaderComponent.class);
    }
    if (levelShaderEntity != null) {
      levelShaderEntity.remove(ShaderComponent.class);
    }
    depthLayerShaderEntities.values().forEach(entity -> entity.remove(ShaderComponent.class));
  }

  private void synchronize(Entity entity) {
    DrawComponent drawComponent = entity.fetch(DrawComponent.class).orElse(null);
    ShaderComponent shaderComponent = entity.fetch(ShaderComponent.class).orElse(null);
    if (drawComponent == null || shaderComponent == null) {
      return;
    }

    Map<String, AppliedShader> previous = appliedShaders.getOrDefault(entity.id(), Map.of());
    Map<String, AppliedShader> current =
        synchronizeShaders(drawComponent.shaders(), shaderComponent, previous);
    appliedShaders.put(entity.id(), current);
  }

  /**
   * Returns the lazily created entity holding scene-wide shader declarations.
   *
   * @return the scene shader holder entity
   */
  public Entity sceneShaderEntity() {
    sceneShaderEntity = ensureHolder(sceneShaderEntity, "ShaderSystem scene shaders");
    return sceneShaderEntity;
  }

  /**
   * Returns the lazily created entity holding level shader declarations.
   *
   * @return the level shader holder entity
   */
  public Entity levelShaderEntity() {
    levelShaderEntity = ensureHolder(levelShaderEntity, "ShaderSystem level shaders");
    return levelShaderEntity;
  }

  /**
   * Returns the lazily created entity holding shader declarations for a depth layer.
   *
   * @param depth the DrawSystem depth layer
   * @return the depth-layer shader holder entity
   */
  public Entity depthLayerShaderEntity(int depth) {
    Entity entity = depthLayerShaderEntities.get(depth);
    entity = ensureHolder(entity, "ShaderSystem depth shaders " + depth);
    depthLayerShaderEntities.put(depth, entity);
    return entity;
  }

  private Entity targetEntity(Target target, int depth) {
    return switch (target) {
      case SCENE -> sceneShaderEntity();
      case LEVEL -> levelShaderEntity();
      case DEPTH_LAYER -> depthLayerShaderEntity(depth);
    };
  }

  /**
   * Returns the scene shader component.
   *
   * @return scene shader declarations
   */
  public ShaderComponent sceneShaders() {
    return sceneShaderEntity().fetch(ShaderComponent.class).orElseThrow();
  }

  /**
   * Returns the level shader component.
   *
   * @return level shader declarations
   */
  public ShaderComponent levelShaders() {
    return levelShaderEntity().fetch(ShaderComponent.class).orElseThrow();
  }

  /**
   * Returns the shader component for a depth layer.
   *
   * @param depth the DrawSystem depth layer
   * @return depth-layer shader declarations
   */
  public ShaderComponent depthLayerShaders(int depth) {
    return depthLayerShaderEntity(depth).fetch(ShaderComponent.class).orElseThrow();
  }

  /**
   * Gets a scene shader declaration by identifier.
   *
   * @param identifier shader identifier
   * @return the matching shader, if present
   */
  public Optional<AbstractShader> sceneShader(String identifier) {
    return findShader(sceneShaderEntity(), identifier);
  }

  /**
   * Gets a level shader declaration by identifier.
   *
   * @param identifier shader identifier
   * @return the matching shader, if present
   */
  public Optional<AbstractShader> levelShader(String identifier) {
    return findShader(levelShaderEntity(), identifier);
  }

  /**
   * Gets a depth-layer shader declaration by identifier.
   *
   * @param depth the DrawSystem depth layer
   * @param identifier shader identifier
   * @return the matching shader, if present
   */
  public Optional<AbstractShader> depthLayerShader(int depth, String identifier) {
    return findShader(depthLayerShaderEntity(depth), identifier);
  }

  private void synchronizeTargetEntities() {
    DrawSystem drawSystem = DrawSystem.getInstance();
    sceneShaderEntity =
        synchronizeTargetEntity(
            sceneShaderEntity, "scene", drawSystem.sceneShaders());
    levelShaderEntity =
        synchronizeTargetEntity(
            levelShaderEntity, "level", drawSystem.levelShaders());

    for (Map.Entry<Integer, Entity> entry : new HashMap<>(depthLayerShaderEntities).entrySet()) {
      Entity synchronizedEntity =
          synchronizeTargetEntity(
              entry.getValue(),
              "depth:" + entry.getKey(),
              drawSystem.entityDepthShaders(entry.getKey()));
      if (synchronizedEntity == null) {
        depthLayerShaderEntities.remove(entry.getKey());
      } else {
        depthLayerShaderEntities.put(entry.getKey(), synchronizedEntity);
      }
    }
  }

  private Entity synchronizeTargetEntity(
      Entity entity, String targetKey, ShaderList shaderList) {
    if (entity == null) {
      return null;
    }
    if (Game.findEntityById(entity.id()).filter(existing -> existing == entity).isEmpty()) {
      removeAppliedShaders(shaderList, appliedTargetShaders.remove(targetKey));
      return null;
    }

    ShaderComponent shaderComponent =
        entity
            .fetch(ShaderComponent.class)
            .orElseGet(
                () -> {
                  ShaderComponent empty = new ShaderComponent();
                  entity.add(empty);
                  return empty;
                });
    Map<String, AppliedShader> previous =
        appliedTargetShaders.getOrDefault(targetKey, Map.of());
    appliedTargetShaders.put(
        targetKey, synchronizeShaders(shaderList, shaderComponent, previous));
    return entity;
  }

  private Entity ensureHolder(Entity entity, String name) {
    if (entity != null
        && Game.findEntityById(entity.id()).filter(existing -> existing == entity).isPresent()) {
      return entity;
    }

    Entity holder = Entity.createLocalEntity(name);
    holder.add(new ShaderComponent());
    Game.add(holder);
    return holder;
  }

  private Map<String, AppliedShader> synchronizeShaders(
      ShaderList shaderList,
      ShaderComponent shaderComponent,
      Map<String, AppliedShader> previous) {
    Map<String, AppliedShader> current = new HashMap<>();
    Set<String> desiredIdentifiers = new HashSet<>();

    for (ShaderComponent.ShaderEntry entry : shaderComponent.shaders()) {
      desiredIdentifiers.add(entry.identifier());
      AppliedShader previousEntry = previous.get(entry.identifier());
      AbstractShader runtimeShader = shaderList.get(entry.identifier());

      if (runtimeShader == null) {
        shaderList.add(entry.identifier(), entry.shader(), entry.order());
      } else if (previousEntry == null
          || runtimeShader != previousEntry.shader()
          || previousEntry.shader() != entry.shader()
          || previousEntry.order() != entry.order()) {
        shaderList.remove(entry.identifier());
        shaderList.add(entry.identifier(), entry.shader(), entry.order());
      }

      current.put(entry.identifier(), new AppliedShader(entry.shader(), entry.order()));
    }

    for (String identifier : previous.keySet()) {
      if (!desiredIdentifiers.contains(identifier)) {
        shaderList.remove(identifier);
      }
    }

    return current;
  }

  private void removeSynchronizedShaders(Entity entity) {
    Map<String, AppliedShader> previous = appliedShaders.remove(entity.id());
    if (previous == null) {
      return;
    }

    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            drawComponent ->
                previous
                    .keySet()
                    .forEach(identifier -> drawComponent.shaders().remove(identifier)));
  }

  private void removeAppliedShaders(
      ShaderList shaderList, Map<String, AppliedShader> applied) {
    if (applied == null) {
      return;
    }
    applied.keySet().forEach(shaderList::remove);
  }

  private Optional<AbstractShader> findShader(Entity entity, String identifier) {
    if (entity == null) {
      return Optional.empty();
    }
    return entity
        .fetch(ShaderComponent.class)
        .flatMap(
            component ->
                component.shaders().stream()
                    .filter(entry -> entry.identifier().equals(identifier))
                    .map(ShaderComponent.ShaderEntry::shader)
                    .findFirst());
  }

  private record AppliedShader(AbstractShader shader, int order) {}
}
