package contrib.modules.emote;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;

/** Factory class for creating emote entities. */
public class EmoteFactory {

  /**
   * Displays an emote at the given position for the specified duration.
   *
   * @param position The position where the emote should be displayed
   * @param emote The type of emote to display
   * @param duration The duration (in milliseconds) for which the emote should be displayed
   * @return An Entity representing the emote, which can be added to the game world
   */
  public static Entity createEmote(Point position, Emote emote, int duration) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(position));
    entity.add(new EmoteComponent(emote, duration));
    DrawComponent dc = new DrawComponent(new SimpleIPath(emote.getPath()));
    dc.depth(DepthLayer.AbovePlayer.depth());
    entity.add(dc);
    return entity;
  }
}
