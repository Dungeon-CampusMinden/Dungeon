package rooms.programming.modules.variables;

/** Properties filled into the golem's soul crystal. */
public enum GolemProperty {
  NAME(JavaValueType.STRING),
  LIFE_ENERGY(JavaValueType.INT),
  MANA(JavaValueType.DOUBLE),
  ACTIVATED(JavaValueType.BOOLEAN),
  VIEW_DIRECTION(JavaValueType.CHAR),
  STEPS(JavaValueType.INT);

  private final JavaValueType valueType;

  GolemProperty(JavaValueType valueType) {
    this.valueType = valueType;
  }

  /** Returns the Java value category required by this property. */
  public JavaValueType valueType() {
    return valueType;
  }
}
