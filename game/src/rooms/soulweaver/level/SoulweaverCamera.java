package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.components.CameraComponent;
import engine.systems.CameraSystem;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/** Temporarily follows one room entity and restores the preceding local cameras and zoom. */
final class SoulweaverCamera {
  private final Map<Entity, CameraComponent> previous = new LinkedHashMap<>();
  private Entity target;
  private CameraSystem.ZoomRequest zoom;

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
    zoom = CameraSystem.zoomRequest();
    // At least a fifth more around the target than rendered now, which is always a raster step
    // further out; a fifth more than requested can land on the step already rendered.
    CameraSystem.zoom(CameraSystem.zoom() * 1.2f, CameraSystem.ZoomSnap.AT_LEAST);
    target.add(new CameraComponent());
  }

  void restore() {
    if (target == null) return;
    target.remove(CameraComponent.class);
    previous.forEach(Entity::add);
    previous.clear();
    CameraSystem.zoom(zoom.zoom(), zoom.snap());
    // Return at once: panning back from a distant target sweeps across the whole level.
    if (Game.systems().get(CameraSystem.class) instanceof CameraSystem camera) camera.snapToFocus();
    target = null;
  }
}
