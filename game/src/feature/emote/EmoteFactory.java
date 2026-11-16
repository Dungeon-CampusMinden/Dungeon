package feature.emote;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.path.SimpleIPath;
import feature.components.AttachmentComponent;
import feature.utils.EntityUtils;

/** Factory class for creating emote entities. */
public class EmoteFactory {

  /** Moves the emote's lower left corner so that the emote is centered above the given position. */
  private static final Vector2 SPRITE_OFFSET = Vector2.of(-0.5f, 0.5f);

  /**
   * Displays an emote above an entity for the specified duration. The emote follows the entity and
   * disappears together with it.
   *
   * @param target The entity the emote belongs to
   * @param offset The offset from the target's center where the emote should be displayed
   * @param emote The type of emote to display
   * @param duration The duration (in milliseconds) for which the emote should be displayed
   * @return An Entity representing the emote, which can be added to the game world
   */
  public static Entity createEmote(Entity target, Vector2 offset, Emote emote, int duration) {
    Vector2 emoteOffset = offset.add(SPRITE_OFFSET);
    Entity entity = new Entity();
    entity.add(new PositionComponent(EntityUtils.getPosition(target).translate(emoteOffset)));
    entity.add(new AttachmentComponent(target, emoteOffset));
    entity.add(new EmoteComponent(emote, duration));
    DrawComponent dc = new DrawComponent(new SimpleIPath(emote.getPath()));
    dc.depth(DepthLayer.AbovePlayer.depth());
    entity.add(dc);
    return entity;
  }
}
