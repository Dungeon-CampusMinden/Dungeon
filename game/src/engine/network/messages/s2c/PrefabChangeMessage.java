package engine.network.messages.s2c;

import engine.network.messages.NetworkMessage;
import java.util.Objects;

/**
 * Server-to-client message describing a runtime change to a prefab instance of the current level.
 *
 * <p>Sent whenever a level handler spawns, updates, removes or resets a prefab instance through
 * {@link feature.prefabs.PrefabRuntime}, so clients can apply the change to their client-side
 * prefabs.
 *
 * @param action kind of change
 * @param name level-unique instance name
 * @param type prefab registry type, empty unless {@code action} is {@link Action#PUT}
 * @param propertiesJson serialized JSON object of the instance properties, empty unless {@code
 *     action} is {@link Action#PUT}
 */
public record PrefabChangeMessage(Action action, String name, String type, String propertiesJson)
    implements NetworkMessage {

  /** Kind of runtime prefab change. */
  public enum Action {
    /** Spawns a new instance or replaces the data of an existing one. */
    PUT,
    /** Removes the instance for the rest of the game session. */
    REMOVE,
    /** Respawns the instance from its current data. */
    RESET
  }

  /** Validates the message members. */
  public PrefabChangeMessage {
    Objects.requireNonNull(action, "action");
    Objects.requireNonNull(name, "name");
    type = type == null ? "" : type;
    propertiesJson = propertiesJson == null ? "" : propertiesJson;
  }
}
