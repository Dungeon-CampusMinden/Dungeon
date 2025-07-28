package core;

import core.utils.Direction;
import core.utils.Point;

public class PlaceHolderNetwork {
  // This is a placeholder for the network functionality.
  // It is not implemented yet, but will be in the future.
  // This is needed to avoid compilation errors when the network functionality is not implemented.
  // It will be removed once the network functionality is implemented.

  boolean isConnected() {
    return false; // Placeholder implementation
  }

  void connect(String host, int port) {
    // Placeholder implementation
    throw new UnsupportedOperationException("Network functionality is not implemented yet.");
  }

  void disconnect() {
    // Placeholder implementation
    throw new UnsupportedOperationException("Network functionality is not implemented yet.");
  }

  private void send(Object message) {
    // Placeholder implementation
    java.lang.System.out.println("Sending message: " + message);
  }

  public void sendHeroMovement(Direction direction) {
    send(new HeroMovementMessage(direction));
  }

  public void sendHeroSkillExecution(Point target) {
    send(new HeroSkillExecutionMessage(target));
  }

  public void sendHeroInteraction(Entity interactedEntity) {
    send(new HeroInteractionMessage(interactedEntity));
  }

  // Placeholder message records
  private record HeroMovementMessage(Direction direction) {}
  private record HeroSkillExecutionMessage(Point target) {}
  private record HeroInteractionMessage(Entity interactedEntity) {}
}
