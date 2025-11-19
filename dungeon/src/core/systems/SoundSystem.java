package core.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.SoundComponent;
import core.sound.SoundSpec;
import core.sound.player.ISoundPlayer;
import core.sound.player.PlayHandle;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.util.*;

/**
 * Client-side system for handling positional audio. For each entity with a SoundComponent, plays
 * new instances and updates existing ones with locally computed pan/volume.
 *
 * <p>This system maintains the invariant that SoundComponent only contains sounds that are
 * currently playing. When sounds finish playing naturally, they are removed from both the internal
 * tracking map and the SoundComponent. When an entity is removed, all its sounds are stopped
 * (looping disabled) and the component is cleared.
 */
public class SoundSystem extends System {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SoundSystem.class);
  private static final float PAN_NORMALIZATION_DISTANCE = 10f;
  private static final float PAN_ATTENUATION_FACTOR = 0.1f;

  /**
   * Maps each entity ID to its currently playing sounds.
   *
   * <p>Structure: entityId -> (soundInstanceId -> playbackHandle)
   *
   * <p>This tracks all active audio instances for each entity. When a sound finishes or is removed,
   * it's deleted from this map. When an entity is removed, its entire inner map is removed.
   */
  private final Map<Integer, Map<Long, PlayHandle>> activePlaybackHandlesByEntity = new HashMap<>();

  /** The sound player used to play and update audio instances. */
  private final ISoundPlayer soundPlayer;

  /** Create a new SoundSystem using the default sound player. */
  public SoundSystem() {
    this(Game.soundPlayer());
  }

  SoundSystem(ISoundPlayer soundPlayer) {
    super(SoundComponent.class);
    this.soundPlayer = soundPlayer;
    this.onEntityRemove = this::onEntityRemoved;
  }

  @Override
  public void execute() {
    Optional<Point> listenerPos = getListenerPosition();
    if (listenerPos.isEmpty()) {
      LOGGER.trace("No listener position available, skipping sound update");
      return;
    }

    filteredEntityStream().forEach(entity -> processEntitySounds(entity, listenerPos.get()));
  }

  /**
   * Processes all sounds for a single entity: starts new sounds, updates active ones, and cleans up
   * finished sounds.
   *
   * @param entity the entity with sounds to process
   * @param listenerPosition the position of the audio listener (usually the player)
   */
  private void processEntitySounds(Entity entity, Point listenerPosition) {
    SoundComponent soundComponent = entity.fetch(SoundComponent.class).orElseThrow();

    // Get or create the map of active sounds for this entity
    // Key: soundInstanceId, Value: playback handle
    Map<Long, PlayHandle> entityActiveSounds =
        activePlaybackHandlesByEntity.computeIfAbsent(entity.id(), k -> new HashMap<>());

    // Step 1: Start new sounds and update existing ones
    startAndUpdateSounds(entity, soundComponent, entityActiveSounds, listenerPosition);

    // Step 2: Clean up finished sounds from both the tracking map and the component
    cleanupFinishedSounds(soundComponent, entityActiveSounds);
  }

  /**
   * Starts new sounds and updates the position/volume of existing sounds.
   *
   * @param entity the entity emitting sounds
   * @param soundComponent the component containing sound specifications
   * @param entityActiveSounds map of currently active sound instances for this entity
   * @param listenerPosition the audio listener's position
   */
  private void startAndUpdateSounds(
      Entity entity,
      SoundComponent soundComponent,
      Map<Long, PlayHandle> entityActiveSounds,
      Point listenerPosition) {

    for (SoundSpec soundSpec : soundComponent.sounds()) {
      long soundInstanceId = soundSpec.instanceId();
      PlayHandle playbackHandle = entityActiveSounds.get(soundInstanceId);

      // If this sound isn't playing yet, start it
      if (playbackHandle == null) {
        playbackHandle = startNewSound(entity, soundSpec, entityActiveSounds);
      }

      // Update the sound's spatial properties (pan, volume based on distance)
      if (playbackHandle != null) {
        updateSound(
            playbackHandle,
            Game.positionOf(entity).orElse(new Point(0, 0)),
            listenerPosition,
            soundSpec);
      }
    }
  }

  /**
   * Starts a new sound instance and adds it to the active sounds map.
   *
   * @param entity the entity emitting the sound
   * @param soundSpec the specification of the sound to play
   * @param entityActiveSounds map to store the playback handle in
   * @return the playback handle if successful, null otherwise
   */
  private PlayHandle startNewSound(
      Entity entity, SoundSpec soundSpec, Map<Long, PlayHandle> entityActiveSounds) {
    long soundInstanceId = soundSpec.instanceId();

    Optional<PlayHandle> handleOpt =
        soundPlayer.playWithInstance(
            soundInstanceId,
            soundSpec.soundName(),
            soundSpec.baseVolume(),
            soundSpec.looping(),
            soundSpec.pitch(),
            0,
            () -> Game.audio().notifySoundFinished(soundInstanceId));

    handleOpt.ifPresentOrElse(
        handle -> entityActiveSounds.put(soundInstanceId, handle),
        () ->
            LOGGER.warn(
                "Failed to play sound '{}' for entity {}", soundSpec.soundName(), entity.id()));

    return entityActiveSounds.get(soundInstanceId);
  }

  /**
   * Removes finished sounds from both the tracking map and the SoundComponent.
   *
   * <p>This ensures that SoundComponent only contains sounds that are currently playing.
   *
   * @param soundComponent the component to clean up
   * @param entityActiveSounds the map of active sounds to clean up
   */
  private void cleanupFinishedSounds(
      SoundComponent soundComponent, Map<Long, PlayHandle> entityActiveSounds) {

    // Find all sound instances that have finished playing
    List<Long> finishedSoundIds = new ArrayList<>();
    for (Map.Entry<Long, PlayHandle> entry : entityActiveSounds.entrySet()) {
      long soundInstanceId = entry.getKey();
      PlayHandle playbackHandle = entry.getValue();

      if (!playbackHandle.isPlaying()) {
        finishedSoundIds.add(soundInstanceId);
      }
    }

    // Remove finished sounds from both the map and the component
    for (long finishedSoundId : finishedSoundIds) {
      entityActiveSounds.remove(finishedSoundId);
      soundComponent.removeByInstance(finishedSoundId);
    }
  }

  /**
   * Called when an entity is removed from the game.
   *
   * <p>Stops all looping sounds for this entity (allows non-looping sounds to finish naturally) and
   * clears the SoundComponent.
   *
   * @param entity the entity being removed
   */
  private void onEntityRemoved(Entity entity) {
    Map<Long, PlayHandle> entityActiveSounds = activePlaybackHandlesByEntity.remove(entity.id());

    if (entityActiveSounds != null) {
      // Turn off looping so sounds finish naturally rather than looping forever
      entityActiveSounds.values().forEach(playbackHandle -> playbackHandle.looping(false));
    }

    entity.fetch(SoundComponent.class).ifPresent(SoundComponent::clear);
  }

  /**
   * Update volume and stereo pan for a playing sound instance based on positions and the spec.
   *
   * <p>Behavior:
   *
   * <ul>
   *   <li>Calculate distance between entity and listener.
   *   <li>If distance > maxDistance -> mute sound.
   *   <li>If maxDistance < 0 -> global sound, no attenuation.
   *   <li>Otherwise apply distance attenuation and pan (pan reduced for distant sounds).
   * </ul>
   *
   * @param playbackHandle handle for the playing sound instance
   * @param entityPosition position of the entity emitting the sound
   * @param listenerPosition position of the audio listener
   * @param soundSpec sound specification containing baseVolume, maxDistance, attenuationFactor,
   *     etc.
   */
  private void updateSound(
      PlayHandle playbackHandle,
      Point entityPosition,
      Point listenerPosition,
      SoundSpec soundSpec) {

    float distance = Point.calculateDistance(entityPosition, listenerPosition);

    // If beyond max distance -> mute
    if (soundSpec.maxDistance() > 0f && distance > soundSpec.maxDistance()) {
      var soundUpdate = ISoundPlayer.SoundUpdate.builder().volume(0f);
      soundPlayer.updateSound(playbackHandle.instanceId(), soundUpdate.build());
      return;
    }

    // Global sounds (no distance attenuation)
    if (soundSpec.maxDistance() <= 0f) {
      var soundUpdate = ISoundPlayer.SoundUpdate.builder().build();
      soundPlayer.updateSound(playbackHandle.instanceId(), soundUpdate);
      return;
    }

    // Distance attenuation
    float attenuationRatio = (distance / soundSpec.maxDistance()) * soundSpec.attenuationFactor();
    float newVolume =
        Math.clamp(soundSpec.maxDistance() * (1 - attenuationRatio), 0f, soundSpec.baseVolume());
    // Pan based on horizontal offset, normalized and clamped to [-1, 1]
    float offsetX = entityPosition.x() - listenerPosition.x();
    float pan = Math.clamp(offsetX / PAN_NORMALIZATION_DISTANCE, -1f, 1f);

    // Reduce pan effect for distant sounds (far sounds are more centered)
    float panDistanceFactor = Math.min(1f, distance / PAN_NORMALIZATION_DISTANCE);
    float panAttenuation = 1f - panDistanceFactor * PAN_ATTENUATION_FACTOR;
    pan *= panAttenuation;

    var soundUpdate = ISoundPlayer.SoundUpdate.builder().pan(pan, newVolume);
    soundPlayer.updateSound(playbackHandle.instanceId(), soundUpdate.build());
  }

  /**
   * Gets the current position of the audio listener (the player).
   *
   * @return the player's position if available, empty otherwise
   */
  private Optional<Point> getListenerPosition() {
    return Game.player().flatMap(Game::positionOf);
  }

  /** {@link SoundSystem} can't be paused. */
  @Override
  public void stop() {
    run = true;
  }
}
