package core.sound;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.game.PreRunConfiguration;
import core.network.server.SoundTracker;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Centralized API for managing entity-backed audio.
 *
 * <p><b>Access:</b> Via {@link Game#audio()}.
 *
 * <p><b>Features:</b> Entity-backed positional audio, global audio via SoundHub, instance ID
 * tracking, onFinished callbacks.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Positional sound on entity
 * Game.audio().playOnEntity(projectile,
 *     SoundSpec.builder("fireball")
 *         .volume(0.8f)
 *         .maxDistance(20f)
 *         .attenuation(0.5f));
 *
 * // Global sound (heard everywhere)
 * Game.audio().playGlobal(
 *     SoundSpec.builder("level_complete")
 *         .volume(0.5f)
 *         .maxDistance(-1));  // -1 = infinite
 * }</pre>
 *
 * @see SoundSpec
 * @see core.components.SoundComponent
 * @see core.systems.SoundSystem
 */
public final class AudioApi {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(AudioApi.class);
  private long instanceIdGenerator = 1L;
  private final ConcurrentHashMap<Long, List<Runnable>> onFinishedCallbacks =
      new ConcurrentHashMap<>();
  private static Integer currentSoundHubId = null;

  /**
   * Generates a globally unique sound instance ID.
   *
   * <p>Thread-safe via AtomicLong. IDs are sequential starting from 1.
   *
   * @return unique instance ID (never 0)
   */
  public long newInstanceId() {
    return instanceIdGenerator++;
  }

  /**
   * Plays a sound on a specific entity.
   *
   * <p>Adds a SoundSpec to the entity's SoundComponent (creates component if needed). The sound
   * will be sent to clients via SoundPlayMessage and played by client SoundSystem.
   *
   * <p>To implement a `onFinished` callback, use the returned instance ID to register the callback
   * on * the {@link AudioApi#registerOnFinished(long, Runnable)}.
   *
   * @param entity the entity to attach the sound to (must not be null)
   * @param builder the SoundSpec builder (instanceId auto-generated if 0)
   * @return the sound instance ID
   * @throws IllegalArgumentException if entity is null
   */
  public long playOnEntity(Entity entity, SoundSpec.Builder builder) {
    if (entity == null) {
      throw new IllegalArgumentException("Entity must not be null");
    }

    if (builder.instanceId == 0) {
      builder.instanceId(newInstanceId());
    }

    SoundSpec spec = builder.build();

    if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
      SoundTracker.instance().registerAndSend(spec, entity.id());
    }

    if (!PreRunConfiguration.multiplayerEnabled() || !PreRunConfiguration.isNetworkServer()) {
      SoundComponent sc =
          entity
              .fetch(SoundComponent.class)
              .orElseGet(
                  () -> {
                    SoundComponent newSc = new SoundComponent();
                    entity.add(newSc);
                    return newSc;
                  });
      sc.add(spec);
    }
    LOGGER.debug(
        "Added sound '{}' (instance={}) to entity {}",
        spec.soundName(),
        spec.instanceId(),
        entity.id());
    return spec.instanceId();
  }

  /**
   * Plays a global sound via the SoundHub entity.
   *
   * <p>Global audio are heard by all clients regardless of position. Automatically sets maxDistance
   * to -1 (infinite). The SoundHub is created at level start if it doesn't exist.
   *
   * <p>It forces maxDistance to -1 to ensure the sound is global.
   *
   * <p>To implement a `onFinished` callback, use the returned instance ID to register the callback
   * on * the {@link AudioApi#registerOnFinished(long, Runnable)}.
   *
   * @param builder the SoundSpec builder (maxDistance will be overridden to -1)
   * @return the sound instance ID
   */
  public long playGlobal(SoundSpec.Builder builder) {
    builder.maxDistance(-1); // Ensure infinite distance for global audio
    Entity hub = ensureSoundHub();
    return playOnEntity(hub, builder);
  }

  /**
   * Plays a sound at a specific world position via a temporary entity.
   *
   * <p>Useful for sounds that are not tied to an entity but need to be positional.
   *
   * <p>A temporary entity is created at the specified position and added to the {@link Game}, and
   * when the sound finishes, the entity is removed automatically.
   *
   * <p>To implement a `onFinished` callback, use the returned instance ID to register the callback
   * on * the {@link AudioApi#registerOnFinished(long, Runnable)}.
   *
   * @param position the world position to play the sound at
   * @param builder the SoundSpec builder (instanceId auto-generated if 0)
   * @return the sound instance ID
   */
  public long playAtPosition(Point position, SoundSpec.Builder builder) {
    Entity tempEntity = new Entity("TempSoundEntity");
    tempEntity.add(new PositionComponent(position));
    Game.add(tempEntity);
    long instanceId = playOnEntity(tempEntity, builder);
    registerOnFinished(instanceId, () -> Game.remove(tempEntity));
    return instanceId;
  }

  /**
   * Stops a specific sound instance by removing it from its entity.
   *
   * <p>Searches all entities for the instance ID and removes the corresponding SoundSpec. Also
   * removes any registered onFinished callback.
   *
   * @param instanceId the unique sound instance ID to stop
   * @return true if found and removed, false if instance doesn't exist
   */
  public boolean stopInstance(long instanceId) {
    if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
      SoundTracker.instance().stopSound(instanceId);
      return true;
    }

    onFinishedCallbacks.remove(instanceId);
    boolean removed = false;
    for (Entity entity : Game.allEntities().toList()) {
      Optional<SoundComponent> scOpt = entity.fetch(SoundComponent.class);
      if (scOpt.isPresent()) {
        SoundComponent sc = scOpt.get();
        int sizeBefore = sc.sounds().size();
        sc.removeByInstance(instanceId);
        if (sc.sounds().size() < sizeBefore) {
          LOGGER.debug("Stopped sound instance {} on entity {}", instanceId, entity.id());
          removed = true;
        }
      }
    }

    boolean stopped = Game.soundPlayer().stopByInstance(instanceId);
    return removed || stopped;
  }

  /**
   * Stops all audio on a specific entity.
   *
   * <p>Removes all SoundSpecs from the entity's SoundComponent and clears their onFinished
   * callbacks.
   *
   * @param entity the entity to stop all audio on (null-safe, ignored if null)
   */
  public void stopAllOnEntity(Entity entity) {
    if (entity == null) return;
    entity
        .fetch(SoundComponent.class)
        .ifPresent(
            sc -> {
              sc.sounds().forEach(spec -> onFinishedCallbacks.remove(spec.instanceId()));
              sc.clear();
              LOGGER.debug("Stopped all audio on entity {}", entity.id());
            });
  }

  /**
   * Registers an onFinished callback for a sound instance.
   *
   * <p>When the client finishes playing the sound in multiplayer, it sends a SoundFinishedMessage
   * to the server. The server then calls {@link #notifySoundFinished(long)} which triggers this
   * callback. In local play, callbacks are executed directly on the client.
   *
   * @param instanceId the sound instance ID to watch
   * @param callback the callback to execute when finished (null-safe, ignored if null)
   */
  public void registerOnFinished(long instanceId, Runnable callback) {
    if (callback != null) {
      if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
        SoundTracker.instance().registerOnFinished(instanceId, callback);
      } else {
        onFinishedCallbacks
            .computeIfAbsent(instanceId, k -> new CopyOnWriteArrayList<>())
            .add(callback);
      }
      LOGGER.debug("Registered registerOnFinished callback for sound instance {}", instanceId);
    }
  }

  /**
   * Unregisters all onFinished callbacks for a sound instance.
   *
   * <p>Useful when stopping a sound to prevent the callback from executing.
   *
   * <p><b>Note: </b> This also removes the cleanup callback for temporary entities created via
   * {@link #playAtPosition(Point, SoundSpec.Builder)}. You should manually handle entity removal in
   * that case to avoid orphaned entities.
   *
   * @param instanceId the sound instance ID to clear callbacks for
   */
  public void unregisterOnFinished(long instanceId) {
    List<Runnable> callbacks = onFinishedCallbacks.get(instanceId);
    if (callbacks != null) {
      callbacks.clear();
    }
    LOGGER.debug("Unregistered all onFinished callbacks for sound instance {}", instanceId);
  }

  /**
   * Called when a sound finishes playing.
   *
   * <p>Removes the callback from the registry and executes it if present.
   *
   * @param instanceId the sound instance ID that finished playing
   */
  public void notifySoundFinished(long instanceId) {
    if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
      SoundTracker.instance().notifySoundFinished(instanceId);
    } else {
      List<Runnable> callbacks = onFinishedCallbacks.remove(instanceId);
      if (callbacks != null && !callbacks.isEmpty()) {
        LOGGER.debug(
            "Sound instance {} finished, executing {} callback", instanceId, callbacks.size());
        callbacks.forEach(Runnable::run);
      }
    }
  }

  /**
   * Ensures a SoundHub entity exists.
   *
   * <p>If the SoundHub already exists (tracked by {@link #currentSoundHubId}), it is returned.
   * Otherwise, a new SoundHub entity is created, marked persistent, and returned.
   *
   * @return the SoundHub entity
   */
  public Entity ensureSoundHub() {
    if (currentSoundHubId != null) {
      Optional<Entity> hub = Game.findEntityById(currentSoundHubId);
      if (hub.isPresent()) {
        return hub.get();
      }
    }

    Entity hub = new Entity("SoundHub");
    hub.persistent(true);
    hub.add(new SoundComponent());
    Game.add(hub);
    currentSoundHubId = hub.id();
    LOGGER.info("Created SoundHub entity with ID {}", currentSoundHubId);
    return hub;
  }
}
