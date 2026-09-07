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

public class EntityFactory {

  public static Entity cryoBox(Point point, boolean on) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    if (on) entity.add(new DrawComponent(new SimpleIPath("objects/tech/CryoBox.png")));
    else entity.add(new DrawComponent(new SimpleIPath("objects/tech/CryoBoxOFF.png")));

    return entity;
  }

  public static Entity roomLabel(Point point, String text, String titel) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Computer_1.png")));
    entity.add(
        new InteractionComponent(
            new Interaction((_, _) -> DialogUtils.showTextPopup(text, titel))));
    return entity;
  }
}
