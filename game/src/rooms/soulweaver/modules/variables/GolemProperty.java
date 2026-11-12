package rooms.soulweaver.modules.variables;

import java.util.Locale;

/** Properties filled into the golem's soul crystal. */
public enum GolemProperty {
  NAME,
  LIFE_ENERGY,
  MANA,
  ACTIVATED,
  VIEW_DIRECTION,
  STEPS;

  /**
   * @return language key of the inscription on the property rune
   */
  public String labelKey() {
    return "property." + name().toLowerCase(Locale.ROOT);
  }

  /**
   * @return language key of the variable name in the final source reveal
   */
  public String identifierKey() {
    return "property.identifier." + name().toLowerCase(Locale.ROOT);
  }
}
