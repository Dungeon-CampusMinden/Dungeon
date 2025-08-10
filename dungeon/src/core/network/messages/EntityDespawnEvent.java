package core.network.messages;

public record EntityDespawnEvent(
  int entityName,
  String reason
) implements NetworkMessage {}
