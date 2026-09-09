package feature.shader;

import engine.Entity;
import engine.System;
import engine.components.DrawComponent;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Synchronizes {@link ShaderComponent} declarations into runtime draw shader lists.
 *
 * <p>The component is the source of truth for identifiers and ordering. Runtime shader instances
 * remain in {@link DrawComponent} because that is the collection consumed by {@code DrawSystem}.
 */
public final class ShaderSystem extends System {
  private final Map<Integer, Map<String, AppliedShader>> appliedShaders = new HashMap<>();

  /** Creates the client-side shader synchronization system. */
  public ShaderSystem() {
    super(AuthoritativeSide.CLIENT, DrawComponent.class, ShaderComponent.class);
    onEntityAdd = this::synchronize;
    onEntityRemove = this::removeSynchronizedShaders;
  }

  @Override
  public void execute() {
    filteredEntityStream().forEach(this::synchronize);
  }

  private void synchronize(Entity entity) {
    DrawComponent drawComponent = entity.fetch(DrawComponent.class).orElse(null);
    ShaderComponent shaderComponent = entity.fetch(ShaderComponent.class).orElse(null);
    if (drawComponent == null || shaderComponent == null) {
      return;
    }

    Map<String, AppliedShader> previous = appliedShaders.getOrDefault(entity.id(), Map.of());
    Map<String, AppliedShader> current = new HashMap<>();
    Set<String> desiredIdentifiers = new HashSet<>();

    for (ShaderComponent.ShaderEntry entry : shaderComponent.shaders()) {
      desiredIdentifiers.add(entry.identifier());
      AppliedShader previousEntry = previous.get(entry.identifier());
      AbstractShader runtimeShader = drawComponent.shaders().get(entry.identifier());

      if (runtimeShader == null) {
        drawComponent.shaders().add(entry.identifier(), entry.shader(), entry.order());
      } else if (previousEntry == null
          || runtimeShader != previousEntry.shader()
          || previousEntry.shader() != entry.shader()
          || previousEntry.order() != entry.order()) {
        drawComponent.shaders().remove(entry.identifier());
        drawComponent.shaders().add(entry.identifier(), entry.shader(), entry.order());
      }

      current.put(entry.identifier(), new AppliedShader(entry.shader(), entry.order()));
    }

    for (String identifier : previous.keySet()) {
      if (!desiredIdentifiers.contains(identifier)) {
        drawComponent.shaders().remove(identifier);
      }
    }

    appliedShaders.put(entity.id(), current);
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

  private record AppliedShader(AbstractShader shader, int order) {}
}
