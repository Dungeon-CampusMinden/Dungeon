package contrib.components;

import contrib.utils.components.health.DamageType;
import core.Component;
import core.Entity;

/**
 * Component that assigns a bomb element to an entity.
 *
 * <p>The {@link BombElement} determines the visual animation state, the resulting {@link
 * DamageType}, and the element-specific SFX path for explosions created from this entity.
 *
 * <p>If an entity has no {@code BombElementComponent}, {@link BombElement#defaultValue()} is used
 * as a fallback.
 */
public class BombElementComponent implements Component {

  /**
   * Enumeration of available bomb elements.
   *
   * <p>Each element defines the animation state name used to pick explosion sprites, the {@link
   * DamageType} applied by explosions of this element and the audio file path for the
   * element-specific explosion sound.
   */
  public enum BombElement {
    FIRE("explosion_red", DamageType.FIRE, "sounds/bomb_explosion_fire.wav"),
    POISON("explosion_green", DamageType.POISON, "sounds/bomb_explosion_poison.wav"),
    ICE("explosion_blue", DamageType.ICE, "sounds/bomb_explosion_ice.wav");

    private final String spriteName;
    private final DamageType dmgType;
    private final String sfxPath;

    /**
     * Constructs an element descriptor.
     *
     * @param spriteName Animation state used for the explosion sprite.
     * @param dmgType Damage type associated with this element.
     * @param sfxPath File path to the element's explosion sound effect.
     */
    BombElement(String spriteName, DamageType dmgType, String sfxPath) {
      this.spriteName = spriteName;
      this.dmgType = dmgType;
      this.sfxPath = sfxPath;
    }

    /**
     * Returns the next element in the fixed rotation order FIRE, POISON, ICE, FIRE.
     *
     * @return The next {@link BombElement}.
     */
    public BombElement next() {
      return switch (this) {
        case FIRE -> POISON;
        case POISON -> ICE;
        case ICE -> FIRE;
      };
    }

    /**
     * Maps this element to its {@link DamageType}.
     *
     * @return The damage type for this element.
     */
    public DamageType toDamageType() {
      return dmgType;
    }

    /**
     * Returns the sprite state name associated with this element.
     *
     * @return The sprite/animation state name.
     */
    public String spriteName() {
      return spriteName;
    }

    /**
     * Returns the path to the element-specific explosion sound effect.
     *
     * @return The SFX file path.
     */
    public String sfxPath() {
      return sfxPath;
    }

    /**
     * Resolves a {@link BombElement} from a {@link DamageType}.
     *
     * <p>If no element matches the provided damage type, {@link #defaultValue()} is returned.
     *
     * @param dmgType The damage type to convert.
     * @return The corresponding bomb element, or the default element if none matches.
     */
    public static BombElement fromDamageType(DamageType dmgType) {
      for (BombElement e : values()) {
        if (e.dmgType == dmgType) return e;
      }
      return defaultValue();
    }

    /**
     * Returns the default element used when none is specified.
     *
     * @return The default {@link BombElement}.
     */
    public static BombElement defaultValue() {
      return FIRE;
    }
  }

  private BombElement element;

  /** Creates a component with the default element ({@link BombElement#FIRE}). */
  public BombElementComponent() {
    this(BombElement.FIRE);
  }

  /**
   * Creates a component with the given element, defaulting to {@link BombElement#FIRE} if {@code
   * element} is {@code null}.
   *
   * @param element The initial bomb element for this component.
   */
  public BombElementComponent(BombElement element) {
    this.element = (element == null) ? BombElement.FIRE : element;
  }

  /**
   * Returns the current bomb element.
   *
   * @return The configured {@link BombElement}.
   */
  public BombElement element() {
    return element;
  }

  /**
   * Sets the bomb element, ignores {@code null} assignments.
   *
   * @param element The new {@link BombElement} to set.
   */
  public void element(BombElement element) {
    if (element != null) this.element = element;
  }

  /**
   * Returns the entity's {@link BombElement} if present, otherwise the {@link
   * BombElement#defaultValue()}.
   *
   * @param e The entity to inspect.
   * @return The element configured on the entity, or the default element if none is present.
   */
  public static BombElement getElementOrDefault(Entity e) {
    return e.fetch(BombElementComponent.class)
        .map(BombElementComponent::element)
        .orElse(BombElement.defaultValue());
  }
}
