package rooms.programming.modules.variables;

/** Candidate values used by the variable puzzle. */
public enum MagicalEssence {
  LIFE_ENERGY_VALUE("125"),
  STEPS_VALUE("17"),
  MANA_VALUE("3.5"),
  BOOLEAN_TRUE("true"),
  BOOLEAN_FALSE("false"),
  NAME_VALUE("\"Nox\""),
  VIEW_DIRECTION_VALUE("'O'");

  private final String literal;

  MagicalEssence(String literal) {
    this.literal = literal;
  }

  /**
   * @return the Java-style literal shown to players
   */
  public String literal() {
    return literal;
  }
}
