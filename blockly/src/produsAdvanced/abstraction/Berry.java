package produsAdvanced.abstraction;

import contrib.components.ItemComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.utils.components.draw.Animation;
import core.utils.components.draw.CoreAnimations;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Set;
import java.util.stream.Collectors;

public class Berry extends Item {

  public static String DONUT_TEXTURE = "items/resource/donut.png";
  public static String BERRY_TEXTURE = "items/resource/berry.png";

  private static final IPath DEFAULT_TEXTURE = new SimpleIPath(BERRY_TEXTURE);
  private final boolean toxic;

  static {
    Item.registerItem(Berry.class);
  }

  public Berry(boolean toxic) {
    super("Eine Beere.", " Könnte sie giftig sein?", Animation.fromSingleImage(DEFAULT_TEXTURE));
    this.toxic = toxic;
  }

  /**
   * 0xffffffcc
   *
   * @param color
   */
  public void tintColor(int color) {
    Entity berry = getEntity();
    DrawComponent dc = berry.fetch(DrawComponent.class).get();
    dc.tintColor(color);
  }

  public void changeTexture(String texture) {
    this.worldAnimation(Animation.fromSingleImage(new SimpleIPath(texture)));
    this.inventoryAnimation(Animation.fromSingleImage(new SimpleIPath(texture)));
    Entity b = getEntity();
    System.out.println("CHANGE TEXTURE");
    b.remove(DrawComponent.class);
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(new SimpleIPath(texture)));
    dc.currentAnimation(CoreAnimations.IDLE);
    b.add(dc);
    return;
  }

  @Override
  public void use(final Entity user) {}

  public boolean isToxic() {
    return toxic;
  }

  private Entity getEntity() {
    Set<Entity> berrys =
        Game.allEntities()
            .filter(e -> e.fetch(ItemComponent.class).isPresent())
            .filter(e -> e.fetch(ItemComponent.class).get().item() instanceof Berry)
            .collect(Collectors.toSet());

    for (Entity b : berrys)
      if (b.fetch(ItemComponent.class).get().item().equals(this)) {
        return b;
      }
    return null;
  }
}
