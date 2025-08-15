package contrib.item.concreteItem;

import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * A Egg that spawns a monster on usage.
 *
 * <p>Can be used for crafting.
 */
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
