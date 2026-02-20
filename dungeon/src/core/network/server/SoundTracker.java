package core.network.server;

import core.Game;
import core.game.PreRunConfiguration;
import core.network.NetworkUtils;
import core.network.messages.s2c.SoundPlayMessage;
import core.network.messages.s2c.SoundStopMessage;
import core.sound.SoundSpec;
import core.utils.logging.DungeonLogger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-side tracker for active networked sounds.
 *
 * <p>Maintains a registry of sounds sent to clients, storing callbacks and tracking which clients
 * are authorized to report completion.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Register sounds with their callbacks and target clients
 *   <li>Validate that SoundFinishedMessage senders were authorized
 *   <li>Execute onFinished callbacks
 *   <li>Support sound resync on client reconnect (looping sounds only)
 * </ul>
 *
 * @see SoundPlayMessage
 * @see core.network.messages.c2s.SoundFinishedMessage
 */
public final class SoundTracker {
  private static final SoundTracker INSTANCE = new SoundTracker();
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SoundTracker.class);

  private record SoundInfo(
      SoundSpec spec,
      int entityId,
      Set<Short> authorizedClientIds,
      List<Runnable> onFinishedCallbacks) {}

  private final Map<Long, SoundInfo> sounds = new ConcurrentHashMap<>();
  private final Map<Short, Set<Long>> clientSounds = new ConcurrentHashMap<>();

  private SoundTracker() {}

  /**
   * Returns the singleton instance of the SoundTracker.
   *
   * @return the SoundTracker instance
   */
  public static SoundTracker instance() {
    return INSTANCE;
  }

  /**
   * Registers a sound and sends SoundPlayMessage to target clients.
   *
   * @param spec the sound specification
   * @param entityId the entity emitting the sound
   */
  public void registerAndSend(SoundSpec spec, int entityId) {
    int[] targets = spec.targetEntityIds();
    Set<Short> clientIds =
        (targets.length == 0)
            ? NetworkUtils.getAllConnectedClientIds()
            : NetworkUtils.entityIdsToClientIds(targets);

    if (clientIds.isEmpty()) {
      LOGGER.debug("No clients to send sound {} to", spec.soundName());
      return;
    }

    Set<Short> authorizedClientIds = Set.copyOf(clientIds);
    SoundInfo info =
        new SoundInfo(spec, entityId, authorizedClientIds, new CopyOnWriteArrayList<>());
    sounds.put(spec.instanceId(), info);

    // Track sounds per client for resync
    for (short clientId : authorizedClientIds) {
      clientSounds
          .computeIfAbsent(clientId, k -> ConcurrentHashMap.newKeySet())
          .add(spec.instanceId());
    }

    // Send to all target clients
    SoundPlayMessage msg =
        new SoundPlayMessage(entityId, spec);

    for (short clientId : authorizedClientIds) {
      Game.network().send(clientId, msg, true);
    }

    LOGGER.debug(
        "Registered sound '{}' (instance={}) for {} clients",
        spec.soundName(),
        spec.instanceId(),
        authorizedClientIds.size());
  }

  /**
   * Registers an onFinished callback for a sound instance.
   *
   * @param instanceId the sound instance id
   * @param callback the callback to execute when finished
   */
  public void registerOnFinished(long instanceId, Runnable callback) {
    SoundInfo info = sounds.get(instanceId);
    if (info != null && callback != null) {
      info.onFinishedCallbacks().add(callback);
    }
  }

  /**
   * Checks if a client is authorized to report sound completion.
   *
   * @param clientId the client attempting to report completion
   * @param soundInstanceId the sound instance id
   * @return true if the client can report completion
   */
  public boolean canReport(short clientId, long soundInstanceId) {
    SoundInfo info = sounds.get(soundInstanceId);
    if (info == null) {
      return false;
    }
    return info.authorizedClientIds().isEmpty() || info.authorizedClientIds().contains(clientId);
  }

  boolean isTracked(long soundInstanceId) {
    return sounds.containsKey(soundInstanceId);
  }

  /**
   * Called when a client reports a sound finished.
   *
   * <p>Executes callbacks and removes the sound from tracking.
   *
   * @param instanceId the sound instance id
   */
  public void notifySoundFinished(long instanceId) {
    SoundInfo info = sounds.remove(instanceId);
    if (info == null) {
      return;
    }

    // Remove from client tracking
    for (short clientId : info.authorizedClientIds()) {
      Set<Long> clientSoundSet = clientSounds.get(clientId);
      if (clientSoundSet != null) {
        clientSoundSet.remove(instanceId);
      }
    }

    // Execute callbacks
    if (!info.onFinishedCallbacks().isEmpty()) {
      LOGGER.debug(
          "Sound {} finished, executing {} callbacks",
          instanceId,
          info.onFinishedCallbacks().size());
      info.onFinishedCallbacks().forEach(Runnable::run);
    }
  }

  /**
   * Stops a sound and notifies clients.
   *
   * @param instanceId the sound instance id
   */
  public void stopSound(long instanceId) {
    SoundInfo info = sounds.remove(instanceId);
    if (info == null) {
      return;
    }

    if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
      SoundStopMessage msg = new SoundStopMessage(instanceId);
      for (short clientId : info.authorizedClientIds()) {
        Game.network().send(clientId, msg, true);
      }
    }

    // Remove from client tracking
    for (short clientId : info.authorizedClientIds()) {
      Set<Long> clientSoundSet = clientSounds.get(clientId);
      if (clientSoundSet != null) {
        clientSoundSet.remove(instanceId);
      }
    }

    LOGGER.debug("Stopped sound {}", instanceId);
  }

  /**
   * Resynchronizes active looping sounds to a reconnecting client.
   *
   * @param clientId the reconnecting client id
   */
  public void resyncSoundsToClient(short clientId) {
    Set<Long> soundIds = clientSounds.get(clientId);
    if (soundIds == null || soundIds.isEmpty()) {
      return;
    }

    LOGGER.debug("Resyncing {} sounds to client {}", soundIds.size(), clientId);

    for (long soundId : Set.copyOf(soundIds)) {
      SoundInfo info = sounds.get(soundId);
      if (info == null) {
        soundIds.remove(soundId);
        continue;
      }

      // Only resync looping sounds (non-looping have likely finished)
      if (!info.spec().looping()) {
        continue;
      }

      SoundPlayMessage msg =
          new SoundPlayMessage(info.entityId(), info.spec());

      Game.network().send(clientId, msg, true);
    }
  }

  /**
   * Clears all tracked sounds.
   *
   * <p>Primarily used during level resets.
   */
  public void clear() {
    sounds.clear();
    clientSounds.clear();
  }
}
