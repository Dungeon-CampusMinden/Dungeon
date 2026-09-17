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
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Computer_1.png")));
    entity.add(
        new InteractionComponent(
            new Interaction((_, _) -> DialogUtils.showTextPopup(text, title))));
    return entity;
  }
}
