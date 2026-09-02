package rooms.programming.modules.variables;

/** Candidate values used by the variable puzzle. */
public enum MagicalEssence {
  LIFE_ENERGY_VALUE("125", JavaValueType.INT),
  STEPS_VALUE("17", JavaValueType.INT),
  MANA_VALUE("3.5", JavaValueType.DOUBLE),
  BOOLEAN_TRUE("true", JavaValueType.BOOLEAN),
  BOOLEAN_FALSE("false", JavaValueType.BOOLEAN),
  NAME_VALUE("\"Nox\"", JavaValueType.STRING),
  VIEW_DIRECTION_VALUE("'O'", JavaValueType.CHAR);

  private final String literal;
  private final JavaValueType valueType;

  MagicalEssence(String literal, JavaValueType valueType) {
    this.literal = literal;
    this.valueType = valueType;
  }

  /** Returns the Java-style literal shown to players. */
  public String literal() {
    return literal;
  }

  /** Returns the Java value category of the essence. */
  public JavaValueType valueType() {
    return valueType;
  }
}
