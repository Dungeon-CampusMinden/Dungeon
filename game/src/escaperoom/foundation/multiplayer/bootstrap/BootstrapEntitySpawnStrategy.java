package escaperoom.foundation.multiplayer.bootstrap;

import engine.Entity;
import engine.network.config.EntitySpawnStrategy;
import engine.network.messages.s2c.EntitySpawnEvent;
import java.util.Objects;
import java.util.Optional;

/** Delegating spawn strategy that recognizes exactly one caller-owned bootstrap marker entity. */
public final class BootstrapEntitySpawnStrategy implements EntitySpawnStrategy {
  private final Entity markerEntity;
  private final EntitySpawnEvent markerEvent;
  private final EntitySpawnStrategy delegate;

  /**
   * Creates a strategy preserving ordinary/player spawn behavior unchanged.
   *
   * @param markerEntity exact caller-owned data-only marker entity
   * @param roomInputSha256 canonical complete DEER-project identity
   * @param delegate ordinary/player entity spawn strategy
   */
  public BootstrapEntitySpawnStrategy(
      final Entity markerEntity, final String roomInputSha256, final EntitySpawnStrategy delegate) {
    this.markerEntity = Objects.requireNonNull(markerEntity, "markerEntity");
    this.delegate = Objects.requireNonNull(delegate, "delegate");
    markerEvent = BootstrapMarker.event(markerEntity.id(), roomInputSha256);
  }

  @Override
  public Optional<EntitySpawnEvent> buildSpawnEvent(final Entity entity) {
    Objects.requireNonNull(entity, "entity");
    if (entity == markerEntity) {
      return Optional.of(markerEvent);
    }
    return Objects.requireNonNull(delegate.buildSpawnEvent(entity), "delegate result");
  }
}
