package feature.inventory.items;

import engine.Entity;
import engine.Game;
import engine.components.PositionComponent;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.InventoryComponent;
import feature.entities.EntityFactory;
import feature.inventory.Item;
import java.io.IOException;

/** A Egg that spawns a monster on usage. */
public class ItemResourceEgg extends Item {

  /** Create a new Egg. */
  public ItemResourceEgg() {
    super(
        "Egg",
        "An egg. What was there before? The chicken or the egg?",
        new Animation(new SimpleIPath("items/resource/egg.png")));
  }

  @Override
  public void use(final Entity e) {
    e.fetch(InventoryComponent.class)
        .ifPresent(
            component -> {
              component.removeOne(this);
              try {
                Entity monster = EntityFactory.randomMonster();
                monster
                    .fetch(PositionComponent.class)
                    .orElseThrow()
                    .position(e.fetch(PositionComponent.class).orElseThrow().position());
                Game.add(monster);
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            });
  }
}
