package rooms.programming.modules.variables;

/** Fantasy vessels representing Java data types. */
public enum SoulVessel {
  IRON_CHEST("Eisenkiste", "Ganze Zahlen", "int"),
  CRYSTAL_BOTTLE("Kristallflasche", "Zahlen, auch Bruchteile", "double"),
  PARCHMENT("Pergament", "Wörter und Texte", "String"),
  RUNE_STONE("Runenstein", "Ein einzelnes Zeichen", "char"),
  LIGHT_ORB("Lichtkugel", "An oder aus", "boolean");

  private final String label;
  private final String capacity;
  private final String javaType;

  SoulVessel(String label, String capacity, String javaType) {
    this.label = label;
    this.capacity = capacity;
    this.javaType = javaType;
  }

  /**
   * @return the workshop name, without revealing a programming type
   */
  public String label() {
    return label;
  }

  /**
   * @return the kinds of values this vessel can hold
   */
  public String capacity() {
    return capacity;
  }

  /**
   * @return the programming type revealed after assembly
   */
  public String javaType() {
    return javaType;
  }
}
