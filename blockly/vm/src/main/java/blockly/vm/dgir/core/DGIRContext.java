package blockly.vm.dgir.core;

import blockly.vm.dgir.core.detail.*;
import blockly.vm.dgir.core.ir.Attribute;
import blockly.vm.dgir.core.ir.Op;
import blockly.vm.dgir.core.ir.Type;

import java.util.HashMap;
import java.util.Map;

public class DGIRContext {

  // Mapping from op-ident and op class to its op details. In case the op aren't registered the class will be Op.class
  // and the op-ident will be the full class name. These are not reliable in any way.
  public static final Map<Class<? extends Op>, OperationDetails.Impl> operations = new HashMap<>();
  public static final Map<String, OperationDetails.Impl> operationsByIdent = new HashMap<>();

  // All the registered operations.
  public static final Map<Class<? extends Op>, RegisteredOperationDetails> registeredOperations = new HashMap<>();
  public static final Map<String, RegisteredOperationDetails> registeredOperationsByIdent = new HashMap<>();

  // Mapping from attribute-ident and attribute class to its attribute details. In case the attributes aren't registered the class will be Attribute.class
  // and the attribute-ident will be the full class name. These are not reliable in any way.
  public static final Map<Class<? extends Attribute>, AttributeDetails.Impl> attributes = new HashMap<>();
  public static final Map<String, AttributeDetails.Impl> attributesByIdent = new HashMap<>();

  // All the registered attributes.
  public static final Map<Class<? extends Attribute>, RegisteredAttributeDetails> registeredAttributes = new HashMap<>();
  public static final Map<String, RegisteredAttributeDetails> registeredAttributesByIdent = new HashMap<>();

  // Mapping from type-ident and type class to its type details. In case the types aren't registered the class will be Type.class
  // and the type-ident will be the full class name. These are not reliable in any way.
  public static final Map<Class<? extends Type>, TypeDetails.Impl> types = new HashMap<>();
  public static final Map<String, TypeDetails.Impl> typesByIdent = new HashMap<>();

  // All the registered types.
  public static final Map<Class<? extends Type>, RegisteredTypeDetails> registeredTypes = new HashMap<>();
  public static final Map<String, RegisteredTypeDetails> registeredTypesByIdent = new HashMap<>();

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
