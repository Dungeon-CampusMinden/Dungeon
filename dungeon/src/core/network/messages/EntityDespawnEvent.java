package core.network.messages;

public record EntityDespawnEvent(
  String entityName,
  String reason
) implements NetworkMessage {}
