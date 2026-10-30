package rooms.systemRecovery.network;

import engine.Entity;
import engine.components.DrawComponent;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.collision.CollideSync;
import feature.components.CollideComponent;
import feature.interaction.InteractionComponent;
import java.util.Map;
import java.util.Optional;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.modules.display.DoorLabelComponent;

/**
 * Synchronizes generic System Recovery components shared by snapshots and initial entity spawns.
 */
public final class SystemRecoveryComponentSync {

  private static final CollideSync COLLIDE_SYNC =
      CollideSync.withPrefix(SystemRecoveryEntitySpawnStrategy.METADATA_COLLIDER_PREFIX);

  private SystemRecoveryComponentSync() {}

  /**
   * Applies all generic System Recovery metadata to one client-side entity.
   *
   * @param entity client-side entity receiving the metadata
   * @param metadata synchronized metadata
   */
  public static void applyEntityMetadata(Entity entity, Map<String, String> metadata) {
    applyInteractableMetadata(entity, metadata);
    applyDisplayMetadata(entity, metadata);
    SystemRecoveryTimerSync.apply(entity, metadata);
    collideComponentFromMetadata(metadata)
        .ifPresent(collideState -> COLLIDE_SYNC.apply(entity, collideState));
  }

  /**
   * Applies a synchronized display key on the client that renders it.
   *
   * @param entity client-side display entity
   * @param metadata synchronized display metadata
   */
  public static void applyDisplayMetadata(Entity entity, Map<String, String> metadata) {
    String text = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_DISPLAY_TEXT);
    if (text == null) return;
    entity
        .fetch(DisplayTextComponent.class)
        .ifPresentOrElse(
            display -> display.text(text), () -> entity.add(new DisplayTextComponent(text)));
  }

  /**
   * Applies interactability, door-label and active-module-socket metadata to an entity.
   *
   * @param entity client-side entity
   * @param metadata synchronized interaction metadata
   */
  public static void applyInteractableMetadata(Entity entity, Map<String, String> metadata) {
    DoorLabelComponent.applyMetadata(entity, metadata);
    String interactable = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_INTERACTABLE);
    if (interactable == null) return;

    if (Boolean.parseBoolean(interactable)) {
      if (!entity.isPresent(InteractionComponent.class)) {
        entity.add(new InteractionComponent());
      }
    } else {
      entity.remove(InteractionComponent.class);
    }

    if (Boolean.parseBoolean(
        metadata.getOrDefault(
            SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SOCKET_ACTIVE, "false"))) {
      entity.add(
          new DrawComponent(new Animation(new SimpleIPath("objects/tech/Screen_info_1.png"))));
    }
  }

  /**
   * Creates a collider from synchronized metadata when the metadata is valid.
   *
   * @param metadata synchronized collider metadata
   * @return reconstructed collider, if the metadata is valid
   */
  public static Optional<CollideComponent> collideComponentFromMetadata(
      Map<String, String> metadata) {
    return COLLIDE_SYNC.fromMetadata(metadata);
  }
}
