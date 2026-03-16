package network;

import contrib.components.CollideComponent;
import contrib.systems.PositionSync;
import contrib.utils.components.collide.Collider;
import contrib.utils.components.collide.Hitbox;
import contrib.utils.components.collide.Hitcircle;
import core.Entity;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class LastHourCollideSync {

  static final String METADATA_COLLIDER_TYPE = "collider.type";
  static final String METADATA_COLLIDER_WIDTH = "collider.width";
  static final String METADATA_COLLIDER_HEIGHT = "collider.height";
  static final String METADATA_COLLIDER_OFFSET_X = "collider.offset.x";
  static final String METADATA_COLLIDER_OFFSET_Y = "collider.offset.y";
  static final String METADATA_COLLIDER_POSITION_X = "collider.position.x";
  static final String METADATA_COLLIDER_POSITION_Y = "collider.position.y";
  static final String METADATA_COLLIDER_SCALE_X = "collider.scale.x";
  static final String METADATA_COLLIDER_SCALE_Y = "collider.scale.y";
  static final String METADATA_COLLIDER_SOLID = "collider.isSolid";

  private static final String TYPE_HITBOX = "hitbox";
  private static final String TYPE_HITCIRCLE = "hitcircle";
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LastHourCollideSync.class);

  private LastHourCollideSync() {}

  static void appendMetadata(Entity entity, Map<String, String> metadata) {
    entity
        .fetch(CollideComponent.class)
        .ifPresent(collideComponent -> metadata.putAll(metadataOf(collideComponent)));
  }

  static Map<String, String> metadataOf(CollideComponent collideComponent) {
    Collider collider = collideComponent.collider();
    if (collider == null) {
      return Map.of();
    }

    String colliderType = colliderType(collider);
    if (colliderType == null) {
      LOGGER.warn("Skipping unsupported collider type '{}'.", collider.getClass().getSimpleName());
      return Map.of();
    }

    Map<String, String> metadata = new HashMap<>();
    metadata.put(METADATA_COLLIDER_TYPE, colliderType);
    metadata.put(METADATA_COLLIDER_WIDTH, String.valueOf(collider.width()));
    metadata.put(METADATA_COLLIDER_HEIGHT, String.valueOf(collider.height()));
    metadata.put(METADATA_COLLIDER_OFFSET_X, String.valueOf(collider.offset().x()));
    metadata.put(METADATA_COLLIDER_OFFSET_Y, String.valueOf(collider.offset().y()));
    metadata.put(METADATA_COLLIDER_POSITION_X, String.valueOf(collider.position().x()));
    metadata.put(METADATA_COLLIDER_POSITION_Y, String.valueOf(collider.position().y()));
    metadata.put(METADATA_COLLIDER_SCALE_X, String.valueOf(collider.scale().x()));
    metadata.put(METADATA_COLLIDER_SCALE_Y, String.valueOf(collider.scale().y()));
    metadata.put(METADATA_COLLIDER_SOLID, String.valueOf(collideComponent.isSolid()));
    return metadata;
  }

  static Optional<CollideComponent> fromMetadata(Map<String, String> metadata) {
    String colliderType = metadata.get(METADATA_COLLIDER_TYPE);
    if (colliderType == null || colliderType.isBlank()) {
      return Optional.empty();
    }

    Optional<Float> width = parseFloat(metadata, METADATA_COLLIDER_WIDTH);
    Optional<Float> height = parseFloat(metadata, METADATA_COLLIDER_HEIGHT);
    Optional<Float> offsetX = parseFloat(metadata, METADATA_COLLIDER_OFFSET_X);
    Optional<Float> offsetY = parseFloat(metadata, METADATA_COLLIDER_OFFSET_Y);
    if (width.isEmpty() || height.isEmpty() || offsetX.isEmpty() || offsetY.isEmpty()) {
      LOGGER.warn("Incomplete collider metadata for collider type '{}'.", colliderType);
      return Optional.empty();
    }

    Collider collider =
        switch (colliderType) {
          case TYPE_HITBOX ->
              new Hitbox(
                  width.orElseThrow(),
                  height.orElseThrow(),
                  offsetX.orElseThrow(),
                  offsetY.orElseThrow());
          case TYPE_HITCIRCLE -> {
            if (Math.abs(width.orElseThrow() - height.orElseThrow()) > 1e-4f) {
              LOGGER.warn(
                  "Hitcircle metadata width '{}' and height '{}' differ. Using width to rebuild radius.",
                  width.orElseThrow(),
                  height.orElseThrow());
            }
            yield new Hitcircle(
                width.orElseThrow() / 2f, offsetX.orElseThrow(), offsetY.orElseThrow());
          }
          default -> null;
        };
    if (collider == null) {
      LOGGER.warn("Unknown collider metadata type '{}'.", colliderType);
      return Optional.empty();
    }

    vectorFromMetadata(metadata, METADATA_COLLIDER_SCALE_X, METADATA_COLLIDER_SCALE_Y)
        .ifPresent(collider::scale);
    pointFromMetadata(metadata, METADATA_COLLIDER_POSITION_X, METADATA_COLLIDER_POSITION_Y)
        .ifPresent(collider::position);

    CollideComponent collideComponent = new CollideComponent();
    collideComponent.collider(collider);
    collideComponent.isSolid(
        Boolean.parseBoolean(metadata.getOrDefault(METADATA_COLLIDER_SOLID, "true")));
    return Optional.of(collideComponent);
  }

  static void apply(Entity entity, CollideComponent collideComponent) {
    CollideComponent target =
        entity
            .fetch(CollideComponent.class)
            .orElseGet(
                () -> {
                  CollideComponent newComponent = new CollideComponent();
                  entity.add(newComponent);
                  return newComponent;
                });
    target.isSolid(collideComponent.isSolid());
    target.collider(collideComponent.collider());
    PositionSync.syncPosition(entity);
  }

  private static Optional<Point> pointFromMetadata(
      Map<String, String> metadata, String xKey, String yKey) {
    Optional<Float> x = parseFloat(metadata, xKey);
    Optional<Float> y = parseFloat(metadata, yKey);
    if (x.isEmpty() || y.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new Point(x.orElseThrow(), y.orElseThrow()));
  }

  private static Optional<Vector2> vectorFromMetadata(
      Map<String, String> metadata, String xKey, String yKey) {
    Optional<Float> x = parseFloat(metadata, xKey);
    Optional<Float> y = parseFloat(metadata, yKey);
    if (x.isEmpty() || y.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(Vector2.of(x.orElseThrow(), y.orElseThrow()));
  }

  private static Optional<Float> parseFloat(Map<String, String> metadata, String key) {
    String raw = metadata.get(key);
    if (raw == null || raw.isBlank()) {
      return Optional.empty();
    }
    try {
      return Optional.of(Float.parseFloat(raw));
    } catch (NumberFormatException ex) {
      LOGGER.warn("Invalid collider metadata {}='{}'.", key, raw);
      return Optional.empty();
    }
  }

  private static String colliderType(Collider collider) {
    if (collider instanceof Hitbox) {
      return TYPE_HITBOX;
    }
    if (collider instanceof Hitcircle) {
      return TYPE_HITCIRCLE;
    }
    return null;
  }
}
