package rooms.soulweaver.modules.variables;

import java.util.Locale;

/** Properties filled into the golem's soul crystal. */
public enum GolemProperty {
  NAME("name"),
  LIFE_ENERGY("lebensenergie"),
  MANA("mana"),
  ACTIVATED("aktiviert"),
  VIEW_DIRECTION("blickrichtung"),
  STEPS("schritte");

  private final String identifier;

  GolemProperty(String identifier) {
    this.identifier = identifier;
  }

  /**
   * @return language key of the inscription on the property rune
   */
  public String labelKey() {
    return "property." + name().toLowerCase(Locale.ROOT);
  }

  /**
   * @return the variable name in the final source reveal
   */
  public String identifier() {
    return identifier;
  }
}
