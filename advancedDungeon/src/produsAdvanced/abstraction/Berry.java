package produsAdvanced.abstraction;

import contrib.components.ItemComponent;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Eine Beere, die als Item im Spiel verwendet werden kann.
 *
 * <p>Je nach Konstruktorparameter kann die Beere giftig sein. Die Beere besitzt außerdem eine
 * Textur und kann im Spiel visuell angepasst werden (z. B. durch Änderung der Farbe oder des
 * Bildes).
 */
public class Berry extends Item {

  /** Name der Beere. */
  public static final String NAME = "BERRY";

  /** Pfad zur Textur eines Donuts (optional verwendbar). */
  public static String DONUT_TEXTURE = "items/resource/donut.png";

  /** Pfad zur Standardtextur der Beere. */
  public static String BERRY_TEXTURE = "items/resource/berry.png";

  /** Standard-Texturpfad, der bei Initialisierung verwendet wird. */
  private static final IPath DEFAULT_TEXTURE = new SimpleIPath(BERRY_TEXTURE);

  /** Gibt an, ob die Beere giftig ist. */
  private final boolean toxic;

  // Registrierung dieser Item-Klasse im Spielsystem
  static {
    Item.registerItem(Berry.class);
  }

  /**
   * Erzeugt eine neue Beere mit einer optionalen Giftigkeit.
   *
   * @param toxic true, wenn die Beere giftig sein soll; false sonst
   */
  public Berry(boolean toxic) {
    super("Eine Beere.", " Könnte sie giftig sein?", new Animation(DEFAULT_TEXTURE));
    this.toxic = toxic;
  }

  /**
   * Verändert die Tönung (Farbe) der Beeren-Textur im Spiel.
   *
   * @param color Farbwert als Integer (ARGB), z. B. 0xffffffcc für gelblich
   */
  public void tintColor(int color) {
    Entity berry = getEntity();
    DrawComponent dc = berry.fetch(DrawComponent.class).get();
    dc.tintColor(color);
  }

  /**
   * Ändert die aktuell verwendete Textur der Beere sowohl im Inventar als auch in der Spielwelt.
   *
   * @param texture Pfad zur neuen Texturdatei
   */
  public void changeTexture(String texture) {
    this.worldAnimation(new Animation(new SimpleIPath(texture)));
    this.inventoryAnimation(new Animation(new SimpleIPath(texture)));
    Entity b = getEntity();
    b.remove(DrawComponent.class);
    DrawComponent dc = new DrawComponent(new Animation(new SimpleIPath(texture)));
    b.add(dc);
  }

  /**
   * Definiert die Wirkung der Beere beim Gebrauch.
   *
   * <p>Aktuell nicht implementiert.
   *
   * @param user Die Entity, die die Beere verwendet
   */
  @Override
  public void use(final Entity user) {}

  /**
   * Gibt an, ob diese Beere giftig ist.
   *
   * @return true, wenn die Beere giftig ist; false sonst
   */
  public boolean isToxic() {
    return toxic;
  }

  /**
   * Ermittelt die zu dieser Item-Instanz gehörende Entity im Spiel.
   *
   * @return Die zugehörige {@link Entity}, oder null, wenn keine gefunden wurde
   */
  private Entity getEntity() {
    Predicate<Entity> isThisBerry =
        e ->
            e.fetch(ItemComponent.class)
                .map(ItemComponent::item)
                .filter(i -> i instanceof Berry)
                .filter(i -> i.equals(this))
                .isPresent();

    return Game.levelEntities(Set.of(ItemComponent.class))
        .filter(isThisBerry::test)
        .findFirst()
        .orElse(null);
  }

  /** Entfernt diese Beere aus dem Spiel. */
  public void destroy() {
    Game.remove(getEntity());
  }
}
