package rooms.soulweaver.modules.variables;

import java.util.Locale;

/** Fantasy vessels representing Java data types. */
public enum SoulVessel {
  IRON_CHEST("int"),
  CRYSTAL_BOTTLE("double"),
  PARCHMENT("String"),
  RUNE_STONE("char"),
  LIGHT_ORB("boolean");

  private final String javaType;

  SoulVessel(String javaType) {
    this.javaType = javaType;
  }

  /**
   * @return language key of the workshop name, without revealing a programming type
   */
  public String labelKey() {
    return "vessel." + name().toLowerCase(Locale.ROOT) + ".label";
  }

  /**
   * @return language key of the kinds of values this vessel can hold
   */
  public String capacityKey() {
    return "vessel." + name().toLowerCase(Locale.ROOT) + ".capacity";
  }

  /**
   * @return the programming type revealed after assembly
   */
  public String javaType() {
    return javaType;
  }
}
