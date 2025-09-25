package contrib.components;

import contrib.utils.components.health.DamageType;
import core.Component;
import core.Entity;

public class BombElementComponent implements Component {

  public enum BombElement {
    FIRE("explosion_red", DamageType.FIRE, "sounds/bomb_explosion_fire.wav"),
    POISON("explosion_green", DamageType.POISON, "sounds/bomb_explosion_poison.wav"),
    ICE("explosion_blue", DamageType.ICE, "sounds/bomb_explosion_ice.wav");

    private final String spriteName;
    private final DamageType dmgType;
    private final String sfxPath;

    BombElement(String spriteName, DamageType dmgType, String sfxPath) {
      this.spriteName = spriteName;
      this.dmgType = dmgType;
      this.sfxPath = sfxPath;
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

    public String sfxPath() {
      return sfxPath;
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
