package contrib.components;

import contrib.utils.components.health.DamageType;
import core.Component;
import core.Entity;

public class BombElementComponent implements Component {

  public enum BombElement {
    FIRE,
    POISON,
    ICE;

    public BombElement next() {
      return switch (this) {
        case FIRE -> POISON;
        case POISON -> ICE;
        case ICE -> FIRE;
      };
    }

    public DamageType toDamageType() {
      return switch (this) {
        case FIRE -> DamageType.FIRE;
        case POISON -> DamageType.POISON;
        case ICE -> DamageType.ICE;
      };
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
