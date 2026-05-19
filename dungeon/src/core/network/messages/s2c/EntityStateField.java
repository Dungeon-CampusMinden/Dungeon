package core.network.messages.s2c;

/** Fields of {@link EntityState} that can be changed or cleared by a delta snapshot. */
public enum EntityStateField {
  /** Entity display name. */
  ENTITY_NAME,

  /** Position coordinates. */
  POSITION,

  /** View direction. */
  VIEW_DIRECTION,

  /** Rotation. */
  ROTATION,

  /** Scale. */
  SCALE,

  /** Current health points. */
  CURRENT_HEALTH,

  /** Maximum health points. */
  MAX_HEALTH,

  /** Current mana points. */
  CURRENT_MANA,

  /** Maximum mana points. */
  MAX_MANA,

  /** Draw state name. */
  STATE_NAME,

  /** Tint color. */
  TINT_COLOR,

  /** Inventory contents. */
  INVENTORY,

  /** Metadata map. */
  METADATA
}
