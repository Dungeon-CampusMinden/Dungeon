package rooms.programming.level;

import engine.Entity;
import engine.components.CameraComponent;
import engine.systems.CameraSystem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/** Temporarily follows one room entity and restores the preceding local cameras and zoom. */
final class ProgrammingCamera {
  private final Map<Entity, CameraComponent> previous = new LinkedHashMap<>();
  private Entity target;
  private float zoom;

  Optional<Entity> target() {
    return Optional.ofNullable(target);
  }

  void follow(Entity entity, Stream<Entity> cameras) {
    if (target != null) return;
    cameras
        .filter(candidate -> candidate.isPresent(CameraComponent.class))
        .toList()
        .forEach(
            candidate -> {
              previous.put(candidate, candidate.fetch(CameraComponent.class).orElseThrow());
              candidate.remove(CameraComponent.class);
            });
    target = entity;
    zoom = CameraSystem.camera().zoom;
    CameraSystem.camera().zoom = zoom * 1.2f;
    target.add(new CameraComponent());
  }

  void restore() {
    if (target == null) return;
    target.remove(CameraComponent.class);
    previous.forEach(Entity::add);
    previous.clear();
    CameraSystem.camera().zoom = zoom;
    target = null;
  }
}
