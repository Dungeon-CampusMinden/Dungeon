package blockly.vm.dgir.core;

public class Utility {
  /**
   * Gets the full name of an entity in a dialect.
   *
   * @param dialectClass The class of the dialect.
   * @param baseName     The base name of the entity.
   * @return The full name of the entity.
   * @throws IllegalArgumentException If the dialect does not exist.
   */
  public static String getFullName(Class<? extends IDialect> dialectClass, String baseName) throws IllegalArgumentException {
    var dialect = DialectRegistry.getDialect(dialectClass);
    if (dialect.getNamespace().isEmpty())
      return baseName;
    return dialect.getNamespace() + "." + baseName;
  }
}
