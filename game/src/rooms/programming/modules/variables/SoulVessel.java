package rooms.programming.modules.variables;

/** Fantasy vessels representing Java data types. */
public enum SoulVessel {
  IRON_CHEST(JavaValueType.INT),
  CRYSTAL_BOTTLE(JavaValueType.DOUBLE),
  PARCHMENT(JavaValueType.STRING),
  RUNE_STONE(JavaValueType.CHAR),
  LIGHT_ORB(JavaValueType.BOOLEAN);

  private final JavaValueType valueType;

  SoulVessel(JavaValueType valueType) {
    this.valueType = valueType;
  }

  /** Returns the Java value category accepted by the vessel. */
  public JavaValueType valueType() {
    return valueType;
  }
}
