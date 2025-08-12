package contrib.utils.components.health;

/** Type of damage to include resistances and vulnerabilities in the damage calculation. */
public enum DamageType {
  /** Damage dealt by physical attacks, such as swords or arrows. */
  PHYSICAL,
  /** Damage dealt by magical sources, such as spells. */
  MAGIC,
  /** Damage caused by fire. */
  FIRE,
  /** Represents healing, which is the opposite of damage. */
  HEAL,
  /** Damage that is dealt over time. */
  POISON,
  /** Damage caused by falling from a height. */
  FALL
}
