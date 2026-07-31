package foundation.multiplayer.bootstrap;

import core.network.messages.s2c.EntitySpawnEvent;
import java.util.Map;

/** Exact data-only spawn marker that carries the Foundation room-input identity. */
public final class BootstrapMarker {
  /** Reserved marker metadata key. */
  public static final String METADATA_KEY = "foundation.bootstrap";

  private BootstrapMarker() {}

  /**
   * Creates one data-only marker event for the supplied room-input identity.
   *
   * @param entityId server entity identifier reserved for this marker
   * @param roomInputSha256 canonical complete DEER-project identity
   * @return exact data-only marker event
   */
  public static EntitySpawnEvent event(final int entityId, final String roomInputSha256) {
    requireRoomInputSha256(roomInputSha256);
    return EntitySpawnEvent.builder()
        .entityId(entityId)
        .metadata(Map.of(METADATA_KEY, roomInputSha256))
        .build();
  }

  /**
   * Returns whether an event advertises the reserved Foundation bootstrap metadata key.
   *
   * @param event candidate spawn event
   * @return true when the reserved key is present
   */
  public static boolean isMarker(final EntitySpawnEvent event) {
    return event != null && event.metadata().containsKey(METADATA_KEY);
  }

  /**
   * Strictly decodes one correctly shaped Foundation bootstrap marker.
   *
   * @param event exact marker event
   * @return validated canonical complete DEER-project identity
   */
  public static String decode(final EntitySpawnEvent event) {
    if (event == null
        || event.positionComponent() != null
        || event.drawInfo() != null
        || event.playerComponent() != null
        || event.characterClassId() != 0
        || event.metadata().size() != 1
        || !event.metadata().containsKey(METADATA_KEY)) {
      throw new IllegalArgumentException("invalid Foundation bootstrap marker shape");
    }
    return requireRoomInputSha256(event.metadata().get(METADATA_KEY));
  }

  private static String requireRoomInputSha256(final String roomInputSha256) {
    if (roomInputSha256 == null || !roomInputSha256.matches("[0-9a-f]{64}")) {
      throw new IllegalArgumentException(
          "Foundation room input identity must be lowercase SHA-256");
    }
    return roomInputSha256;
  }
}
