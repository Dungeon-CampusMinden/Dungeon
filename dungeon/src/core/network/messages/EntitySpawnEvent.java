package core.network.messages;

import core.utils.Direction;
import core.utils.Point;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

public record EntitySpawnEvent(String entityName,
                               Point position,
                               Direction viewDirection,
                               String texturePathString,
                               String currentAnimation,
                               int tintColor) implements NetworkMessage {

  public IPath texturePath() {
    return new SimpleIPath(texturePathString);
  }
}
