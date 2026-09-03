package feature.collision;

import engine.Entity;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.logging.DungeonLogger;
import feature.components.CollideComponent;
import feature.systems.PositionSync;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Synchronizes collider geometry and solidity through string metadata. */
public final class CollideSync {
  private static final String TYPE_HITBOX = "hitbox";
  private static final String TYPE_HITCIRCLE = "hitcircle";
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CollideSync.class);

  private final String colliderTypeKey;
  private final String colliderWidthKey;
  private final String colliderHeightKey;
  private final String colliderOffsetXKey;
  private final String colliderOffsetYKey;
  private final String colliderPositionXKey;
  private final String colliderPositionYKey;
  private final String colliderScaleXKey;
  private final String colliderScaleYKey;
  private final String colliderSolidKey;

  private CollideSync(String metadataPrefix) {
    String prefix = Objects.requireNonNull(metadataPrefix, "metadataPrefix").strip();
    if (prefix.isEmpty()) {
      throw new IllegalArgumentException("metadataPrefix must not be blank");
    }
    if (prefix.endsWith(".")) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }

    colliderTypeKey = prefix + ".type";
    colliderWidthKey = prefix + ".width";
    colliderHeightKey = prefix + ".height";
    colliderOffsetXKey = prefix + ".offset.x";
    colliderOffsetYKey = prefix + ".offset.y";
    colliderPositionXKey = prefix + ".position.x";
    colliderPositionYKey = prefix + ".position.y";
    colliderScaleXKey = prefix + ".scale.x";
    colliderScaleYKey = prefix + ".scale.y";
    colliderSolidKey = prefix + ".isSolid";
  }

  /**
   * Creates a collider metadata sync using keys such as {@code <metadataPrefix>.type}.
   *
   * @param metadataPrefix prefix for all collider metadata keys
   * @return a sync instance for the given metadata prefix
   */
  public static CollideSync withPrefix(String metadataPrefix) {
    return new CollideSync(metadataPrefix);
  }

  /**
   * Adds collider metadata for the entity when a {@link CollideComponent} is present.
   *
   * @param entity the source entity
   * @param metadata metadata target map
   */
  public void appendMetadata(Entity entity, Map<String, String> metadata) {
    entity
        .fetch(CollideComponent.class)
        .ifPresent(collideComponent -> metadata.putAll(metadataOf(collideComponent)));
  }

  /**
   * Serializes collider geometry and solidity.
   *
   * <p>Collision callbacks are runtime behavior and are intentionally not serialized.
   *
   * @param collideComponent the component to serialize
   * @return metadata entries describing the collider, or an empty map for unsupported colliders
   */
  public Map<String, String> metadataOf(CollideComponent collideComponent) {
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
    metadata.put(colliderTypeKey, colliderType);
    metadata.put(colliderWidthKey, String.valueOf(collider.width()));
    metadata.put(colliderHeightKey, String.valueOf(collider.height()));
    metadata.put(colliderOffsetXKey, String.valueOf(collider.offset().x()));
    metadata.put(colliderOffsetYKey, String.valueOf(collider.offset().y()));
    metadata.put(colliderPositionXKey, String.valueOf(collider.position().x()));
    metadata.put(colliderPositionYKey, String.valueOf(collider.position().y()));
    metadata.put(colliderScaleXKey, String.valueOf(collider.scale().x()));
    metadata.put(colliderScaleYKey, String.valueOf(collider.scale().y()));
    metadata.put(colliderSolidKey, String.valueOf(collideComponent.isSolid()));
    return metadata;
  }

  /**
   * Rebuilds a {@link CollideComponent} from metadata.
   *
   * @param metadata source metadata
   * @return the reconstructed collider component, if metadata is present and valid
   */
  public Optional<CollideComponent> fromMetadata(Map<String, String> metadata) {
    String colliderType = metadata.get(colliderTypeKey);
    if (colliderType == null || colliderType.isBlank()) {
      return Optional.empty();
    }

    Optional<Float> width = parseFloat(metadata, colliderWidthKey);
    Optional<Float> height = parseFloat(metadata, colliderHeightKey);
    Optional<Float> offsetX = parseFloat(metadata, colliderOffsetXKey);
    Optional<Float> offsetY = parseFloat(metadata, colliderOffsetYKey);
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
                  "Hitcircle metadata width '{}' and height '{}' differ. Using width as diameter.",
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

    vectorFromMetadata(metadata, colliderScaleXKey, colliderScaleYKey).ifPresent(collider::scale);
    pointFromMetadata(metadata, colliderPositionXKey, colliderPositionYKey)
        .ifPresent(collider::position);

    CollideComponent collideComponent = new CollideComponent();
    collideComponent.collider(collider);
    collideComponent.isSolid(Boolean.parseBoolean(metadata.getOrDefault(colliderSolidKey, "true")));
    return Optional.of(collideComponent);
  }

  /**
   * Applies serialized collider state to an entity without changing collision callbacks.
   *
   * @param entity the target entity
   * @param collideComponent the decoded collider state
   */
  public void apply(Entity entity, CollideComponent collideComponent) {
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

  private Optional<Point> pointFromMetadata(Map<String, String> metadata, String xKey, String yKey) {
    Optional<Float> x = parseFloat(metadata, xKey);
    Optional<Float> y = parseFloat(metadata, yKey);
    if (x.isEmpty() || y.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new Point(x.orElseThrow(), y.orElseThrow()));
  }

  private Optional<Vector2> vectorFromMetadata(
      Map<String, String> metadata, String xKey, String yKey) {
    Optional<Float> x = parseFloat(metadata, xKey);
    Optional<Float> y = parseFloat(metadata, yKey);
    if (x.isEmpty() || y.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(Vector2.of(x.orElseThrow(), y.orElseThrow()));
  }

  private Optional<Float> parseFloat(Map<String, String> metadata, String key) {
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

  private String colliderType(Collider collider) {
    if (collider instanceof Hitbox) {
      return TYPE_HITBOX;
    }
    if (collider instanceof Hitcircle) {
      return TYPE_HITCIRCLE;
    }
    return null;
  }
}
