package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.sound.player.IPlayHandle;
import core.sound.player.ISoundPlayer;
import core.utils.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * System for handling positional sounds in the game world. Manages sound playback, volume
 * attenuation, and stereo panning based on entity positions relative to the hero (listener). Sounds
 * are played when entities with SoundComponent are added, updated each frame for positional
 * effects, and stopped when entities are removed.
 */
public class SoundSystem extends System {
  /**
   * The distance over which pan is fully effective before attenuation begins. At this distance,
   * entities directly left/right produce full pan (-1 or 1). For example, an entity 10 units to the
   * right of the listener pans fully to the right.
   */
  private static final float PAN_NORMALIZATION_DISTANCE = 10;

  /**
   * Controls how quickly pan attenuates with distance. Higher values (e.g., 1.0) make pan fade to
   * center faster as distance increases. At 1.0, pan reaches center at PAN_NORMALIZATION_DISTANCE;
   * at 0.0, pan never fades.
   */
  private static final float PAN_ATTENUATION_FACTOR = 0.1f;

  private final Map<Entity, IPlayHandle> playingSounds = new HashMap<>();
  private final ISoundPlayer soundPlayer = Game.soundPlayer();

  /**
   * Creates a new SoundSystem that filters entities with PositionComponent and SoundComponent. Uses
   * the global sound player from Game.soundPlayer().
   */
  public SoundSystem() {
    super(PositionComponent.class, SoundComponent.class);
    this.onEntityAdd = this::onEntityAdded;
    this.onEntityRemove = this::onEntityRemoved;
  }

  /**
   * Updates sound positions and effects each frame. Calculates volume and pan for active sounds
   * based on current entity and listener positions.
   */
  @Override
  public void execute() {
    Optional<Point> listenerPos = getListenerPosition();
    if (listenerPos.isEmpty()) return;

    filteredEntityStream()
        .forEach(
            entity -> {
              Optional<PositionComponent> posComp = entity.fetch(PositionComponent.class);
              Optional<SoundComponent> soundComp = entity.fetch(SoundComponent.class);
              if (posComp.isPresent() && soundComp.isPresent()) {
                IPlayHandle handle = playingSounds.get(entity);
                if (handle != null) {
                  updateSound(handle, posComp.get().position(), listenerPos.get(), soundComp.get());
                }
              }
            });
  }

  private void onEntityAdded(Entity entity) {
    if (getListenerPosition().isEmpty()) return; // No listener, skip playing sound
    Optional<SoundComponent> soundComp = entity.fetch(SoundComponent.class);
    if (soundComp.isPresent()) {
      SoundComponent comp = soundComp.get();
      Optional<IPlayHandle> handleOpt =
          soundPlayer.play(comp.soundId(), comp.baseVolume(), comp.looping());
      handleOpt.ifPresent(
          handle -> {
            playingSounds.put(entity, handle);
          });
    }
  }

  private void onEntityRemoved(Entity entity) {
    IPlayHandle handle = playingSounds.remove(entity);
    if (handle != null) {
      handle.stop();
    }
  }

  private void updateSound(
      IPlayHandle handle, Point entityPos, Point listenerPos, SoundComponent comp) {
    float distance = Point.calculateDistance(entityPos, listenerPos);
    // mute if out of range
    if (comp.maxDistance() > 0 && distance > comp.maxDistance()) {
      handle.volume(0);
      return;
    }

    float volume = comp.baseVolume();
    // If maxDistance is set, apply distance-based attenuation
    if (comp.maxDistance() > 0) {
      float attenuation = (distance / comp.maxDistance()) * comp.attenuationFactor();
      // Reduce volume proportionally (closer = louder)
      volume *= (1 - attenuation);
      volume = Math.max(0, Math.min(1, volume));
    }

    // Calculate horizontal offset for stereo panning
    float dx = entityPos.x() - listenerPos.x();
    // Normalize pan value: positive dx (right) -> positive pan, clamped to [-1, 1]
    float pan = Math.max(-1, Math.min(1, dx / PAN_NORMALIZATION_DISTANCE));

    // Attenuate pan with distance to avoid extreme panning at far distances
    float panAttenuation =
        1 - Math.min(1, distance / PAN_NORMALIZATION_DISTANCE) * PAN_ATTENUATION_FACTOR;
    pan *= panAttenuation;

    handle.volume(volume);
    handle.pan(pan, volume);
    handle.onFinished(comp.onFinish());
  }

  private Optional<Point> getListenerPosition() {
    return Game.hero()
        .flatMap(hero -> hero.fetch(PositionComponent.class))
        .map(PositionComponent::position);
  }
}
