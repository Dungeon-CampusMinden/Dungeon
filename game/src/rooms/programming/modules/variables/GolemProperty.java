package rooms.programming.modules.variables;

/** Properties filled into the golem's soul crystal. */
public enum GolemProperty {
  NAME("Name", "name"),
  LIFE_ENERGY("Lebensenergie", "lebensenergie"),
  MANA("Mana", "mana"),
  ACTIVATED("Aktiviert", "aktiviert"),
  VIEW_DIRECTION("Blickrichtung", "blickrichtung"),
  STEPS("Schritte", "schritte");

  private final String label;
  private final String identifier;

  GolemProperty(String label, String identifier) {
    this.label = label;
    this.identifier = identifier;
  }

  /**
   * @return the inscription on the property rune
   */
  public String label() {
    return label;
  }

  /**
   * @return the variable name in the final source reveal
   */
  public String identifier() {
    return identifier;
  }
}
