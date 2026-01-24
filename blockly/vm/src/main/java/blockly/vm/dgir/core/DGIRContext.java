package blockly.vm.dgir.core;

import java.util.HashMap;
import java.util.Map;

public class DGIRContext {

  // Mapping from operation-name to its operation info. These might or might not be registered.
  public static final Map<String, OperationName.Impl> operationsByName = new HashMap<>();

  // All the registered operations.
  public static final Map<Class<? extends Op>, RegisteredOperationName> registeredOperations = new HashMap<>();
  public static final Map<String, RegisteredOperationName> registeredOperationsByName = new HashMap<>();

  // Mapping from attribute-name to its attribute info. These might or might not be registered.
  public static final Map<String, AttributeName.Impl> attributesByName = new HashMap<>();

  // All the registered attributes.
  public static final Map<Class<? extends Attribute>, RegisteredAttributeName> registeredAttributes = new HashMap<>();
  public static final Map<String, RegisteredAttributeName> registeredAttributesByName = new HashMap<>();

  // Mapping from type-name to its type info. These might or might not be registered.
  public static final Map<String, TypeName.Impl> typesByName = new HashMap<>();

  // All the registered types.
  public static final Map<Class<? extends Type>, RegisteredTypeName> registeredTypes = new HashMap<>();
  public static final Map<String, RegisteredTypeName> registeredTypesByName = new HashMap<>();

  // All the registered dialects.
  public static final Map<Class<? extends Dialect>, Dialect> registeredDialects = new HashMap<>();
  public static final Map<String, Dialect> registeredDialectsByName = new HashMap<>();

  /**
   * Tries to get the dialect referenced by the given typename.
   * If there is a '.' in the typename, the part before the last '.' is treated as the dialect namespace.
   * If that namespace can't be found, the builtin dialect "" is returned.
   *
   * @param name The name of the type
   * @return The dialect instance
   */
  public static Dialect getReferencedDialect(String name) {
    var i = name.indexOf('.');
    if (i >= 0) {
      var namespace = name.substring(0, i);
      var dialect = registeredDialectsByName.get(namespace);
      if (dialect != null) {
        return dialect;
      }
    }
    return registeredDialectsByName.get("");
  }
}
