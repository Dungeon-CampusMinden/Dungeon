package contrib.components;

import contrib.utils.components.health.DamageType;
import core.Component;
import core.Entity;

public class BombElementComponent implements Component {

  public enum BombElement {
    FIRE("explosion_red", DamageType.FIRE),
    POISON("explosion_green", DamageType.POISON),
    ICE("explosion_blue", DamageType.ICE);

    private final String spriteName;
    private final DamageType dmgType;

    BombElement(String spriteName, DamageType dmgType) {
      this.spriteName = spriteName;
      this.dmgType = dmgType;
    }

    public BombElement next() {
      return switch (this) {
        case FIRE -> POISON;
        case POISON -> ICE;
        case ICE -> FIRE;
      };
    }

    public DamageType toDamageType() {
      return dmgType;
    }

    public String spriteName() {
      return spriteName;
    }

    public static BombElement fromDamageType(DamageType dmgType) {
      for (BombElement e : values()) {
        if (e.dmgType == dmgType) return e;
      }
      return defaultValue();
    }

    public static BombElement defaultValue() {
      return FIRE;
    }
  }

  private BombElement element;

  public BombElementComponent() {
    this(BombElement.FIRE);
  }

  public BombElementComponent(BombElement element) {
    this.element = (element == null) ? BombElement.FIRE : element;
  }

  public BombElement element() {
    return element;
  }

  public void element(BombElement element) {
    if (element != null) this.element = element;
  }

  public static BombElement getOrDefault(Entity e) {
    return e.fetch(BombElementComponent.class)
        .map(BombElementComponent::element)
        .orElse(BombElement.defaultValue());
  }
}
