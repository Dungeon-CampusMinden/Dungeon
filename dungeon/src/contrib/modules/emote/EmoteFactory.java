package contrib.modules.emote;

import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.DepthLayer;
import core.utils.components.path.SimpleIPath;

public class EmoteFactory {

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
