package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;

/** Builds the small amount of shared room-level scenery used by System Recovery. */
public final class SystemRecoveryRoomFactory {

  private SystemRecoveryRoomFactory() {}

  /**
   * Creates an interactable room label.
   *
   * @param point label position
   * @param text popup text or translation key
   * @param title popup title or translation key
   * @return configured room label
   */
  public static Entity roomLabel(Point point, String text, String title) {
    return roomLabel(point, text, title, 0f);
  }

  /**
   * Creates an interactable room label with a visual rotation.
   *
   * <p>The rotation belongs to the draw entity's position component, so it is included in the
   * normal multiplayer entity snapshot. The interaction and collision components remain anchored at
   * the original world position.
   *
   * @param point label position
   * @param text popup text or translation key
   * @param title popup title or translation key
   * @param rotation visual rotation in degrees
   * @return configured room label
   */
  public static Entity roomLabel(Point point, String text, String title, float rotation) {
    Entity entity = new Entity();
    PositionComponent position = new PositionComponent(point);
    position.rotation(rotation);
    entity.add(position);
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Computer_1.png")));
    entity.add(
        new InteractionComponent(
            new Interaction((_, _) -> DialogUtils.showTextPopup(text, title))));
    return entity;
  }
}
