package core.network.messages;

/** Record representing critical discrete events. */
public record NetworkEvent(Type type, Object data) implements NetworkMessage {

  public enum Type {
    LEVEL_CHANGE,
    ENTITY_SPAWN,
    ENTITY_DESPAWN,
    PLAYER_JOINED,
    PLAYER_LEFT,
    GAME_OVER
  }

  public NetworkEvent(Type type) {
    this(type, null);
  }
}
