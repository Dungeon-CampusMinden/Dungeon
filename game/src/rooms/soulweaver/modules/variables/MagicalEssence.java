package rooms.soulweaver.modules.variables;

import java.util.function.UnaryOperator;

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
   * @param word renders a key below the {@code soulweaver} namespace, as text for this client or as
   *     a transport key on the server
   * @return the Java-style literal shown to players; the facing letter follows the language
   */
  public String literal(UnaryOperator<String> word) {
    return this == VIEW_DIRECTION_VALUE ? word.apply("essence.view_direction") : literal;
  }
}
