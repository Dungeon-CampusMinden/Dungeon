package blockly.vm.dgir.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores all the dialects and their operations/types.
 */
public class DialectRegistry {
  // ------- Dialects --------
  // Map the dialect class to the dialect instance
  private static final Map<Class<? extends IDialect>, IDialect> DIALECTS = new HashMap<>();
  // Keep a reference from the namespace to the dialect class
  private static final Map<String, Class<? extends IDialect>> DIALECT_TYPES = new HashMap<>();
  // Reference from dialect name to instance
  private static final Map<String, IDialect> DIALECT_INSTANCES = new HashMap<>();

  // ------- OPS --------
  // Map namespace to operations and operation name to operation
  private static final Map<String, Operation> OPS = new HashMap<>();
  // Keep a flat lookup by namespace+name for resolver-friendly class lookup
  private static final Map<String, Class<? extends Operation>> OP_TYPES = new HashMap<>();
  // Reference from op id to its dialect
  private static final Map<String, IDialect> OP_DIALECT = new HashMap<>();

  // ------- Types --------
  // Map full type name to type instance
  private static final Map<String, Type> TYPES = new HashMap<>();
  // Keep a flat lookup by namespace+name for resolver-friendly class lookup
  private static final Map<String, Class<? extends Type>> TYPE_TYPES = new HashMap<>();
  // Reference from type id to its dialect
  private static final Map<String, IDialect> TYPE_DIALECT = new HashMap<>();

  // ------- Attributes --------
  // Reference from attribute id to its attribute instance
  private static final Map<String, Attribute> ATTRIBUTES = new HashMap<>();
  // Reference from attribute id to its attribute type
  private static final Map<String, Class<? extends Attribute>> ATTRIBUTE_TYPES = new HashMap<>();
  // Reference from attribute id to its dialect
  private static final Map<String, IDialect> ATTRIBUTE_DIALECT = new HashMap<>();


  public static void registerDialect(Class<? extends IDialect> dialectClass) {
    try {
      IDialect instance = dialectClass.getDeclaredConstructor().newInstance();
      DIALECTS.put(dialectClass, instance);
      DIALECT_TYPES.put(instance.getNamespace(), dialectClass);
      DIALECT_INSTANCES.put(instance.getNamespace(), instance);
      for (Operation op : instance.AllOperations()) {
        addOp(instance, op);
      }

      for (Type type : instance.AllTypes()) {
        addType(instance, type);
      }

      for (Attribute attribute : instance.AllAttributes()) {
        addAttribute(instance, attribute);
      }

    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  private static void throwErrorCase(String name) throws IllegalArgumentException {
    throw new IllegalArgumentException("Could not find dialect or dialect element: " + name + "\nMake sure it is registered in the dialect registry!");
  }

  /**
   * Add an operation to the registry.
   *
   * @param dialect The dialect instance.
   * @param op      The operation instance.
   */
  public static void addOp(IDialect dialect, Operation op) {
    OP_DIALECT.put(op.getIdent(), dialect);
    OPS.put(op.getIdent(), op);
    OP_TYPES.put(op.getIdent(), op.getClass());
  }

  /**
   * Add a type to the registry.
   *
   * @param dialect The dialect instance.
   * @param type    The type instance.
   */
  public static void addType(IDialect dialect, Type type) {
    TYPE_DIALECT.put(type.getIdent(), dialect);
    TYPES.put(type.getIdent(), type);
    TYPE_TYPES.put(type.getIdent(), type.getClass());
  }

  /**
   * Add an attribute to the registry.
   *
   * @param dialect   The dialect instance.
   * @param attribute The attribute instance.
   */
  private static void addAttribute(IDialect dialect, Attribute attribute) {
    ATTRIBUTES.put(attribute.getIdent(), attribute);
    ATTRIBUTE_TYPES.put(attribute.getIdent(), attribute.getClass());
    ATTRIBUTE_DIALECT.put(attribute.getIdent(), dialect);
  }


  // ------- Dialects -------------------------------------------------------------------------------

  /**
   * Get a dialect by its class.
   *
   * @param dialectClass The dialect class.
   * @return The dialect instance.
   * @throws IllegalArgumentException If the op or dialect does not exist.
   */
  public static IDialect getDialect(Class<? extends IDialect> dialectClass) throws IllegalArgumentException {
    var dialect = DIALECTS.get(dialectClass);
    if (dialect == null) {
      throwErrorCase(dialectClass.getName());
    }
    return dialect;
  }

  /**
   * Get a dialect by its namespace.
   *
   * @param dialectNamespace The dialect namespace.
   * @return The dialect instance.
   * @throws IllegalArgumentException If the op or dialect does not exist.
   */
  public static IDialect getDialect(String dialectNamespace) throws IllegalArgumentException {
    var dialect = DIALECT_INSTANCES.get(dialectNamespace);
    if (dialect == null) {
      throwErrorCase(dialectNamespace);
    }
    return dialect;
  }

  /**
   * Get a dialect by its namespace.
   *
   * @param dialectNamespace The dialect namespace.
   * @return The dialect class.
   * @throws IllegalArgumentException If the op or dialect does not exist.
   */
  public static Class<? extends IDialect> getDialectType(String dialectNamespace) throws IllegalArgumentException {
    var dialectType = DIALECT_TYPES.get(dialectNamespace);
    if (dialectType == null) {
      throwErrorCase(dialectNamespace);
    }
    return dialectType;
  }

  // ------- Ops -------------------------------------------------------------------------------

  /**
   * Get an operation by full name.
   *
   * @param fullName Namespace + OpName
   * @return Returns a copy of the operation so that it can be initialized later.
   * @throws IllegalArgumentException If the op or dialect does not exist.
   */
  public static Operation getOp(String fullName) throws IllegalArgumentException {
    // Get the operation prototype
    Operation op = OPS.get(fullName);
    // If it exists, create a copy of it which can later be initialized
    if (op == null) {
      throwErrorCase(fullName);
    }
    return op.clone();
  }

  /**
   * Get the registered operation class for the given full name.
   *
   * @param fullName The full name of the operation.
   * @return The operation class.
   * @throws IllegalArgumentException If the op or dialect does not exist.
   */
  public static Class<? extends Operation> getOpType(String fullName) throws IllegalArgumentException {
    var opType = OP_TYPES.get(fullName);
    if (opType == null) {
      throwErrorCase(fullName);
    }
    return opType;
  }

  // ------- Types -------------------------------------------------------------------------------

  /**
   * Get the type class for the given id.
   *
   * @param id The id of the type.
   * @return The type class.
   * @throws IllegalArgumentException If the type or dialect does not exist.
   */
  public static Class<? extends Type> getType(String id) throws IllegalArgumentException {
    var type = TYPE_TYPES.get(id);
    if (type == null) {
      throwErrorCase(id);
    }
    return type;
  }

  /**
   * Get the type instance for the given id.
   *
   * @param id The id of the type.
   * @return The type instance.
   * @throws IllegalArgumentException If the type or dialect does not exist.
   */
  public static Type getTypeInstance(String id) throws IllegalArgumentException {
    var type = TYPES.get(id);
    if (type == null) {
      throwErrorCase(id);
    }
    return type;
  }

  /**
   * Get the dialect instance for the given type.
   *
   * @param typeIdent The type ident.
   * @return The dialect instance.
   * @throws IllegalArgumentException If the type or dialect does not exist.
   */
  public static IDialect getTypeDialect(String typeIdent) throws IllegalArgumentException {
    var dialect = TYPE_DIALECT.get(typeIdent);
    if (dialect == null) {
      throwErrorCase(typeIdent);
    }
    return dialect;
  }

  // ------- Attributes -------------------------------------------------------------------------------

  /**
   * Get the attribute instance for the given id.
   *
   * @param id The id of the attribute.
   * @return The attribute instance.
   * @throws IllegalArgumentException If the attribute or dialect does not exist.
   */
  public static Attribute getAttribute(String id) throws IllegalArgumentException {
    var attribute = ATTRIBUTES.get(id);
    if (attribute == null) {
      throwErrorCase(id);
    }
    return attribute;
  }

  /**
   * Get the attribute type for the given id.
   *
   * @param id The id of the attribute.
   * @return The attribute type.
   * @throws IllegalArgumentException If the attribute or dialect does not exist.
   */
  public static Class<? extends Attribute> getAttributeType(String id) throws IllegalArgumentException {
    var attributeType = ATTRIBUTE_TYPES.get(id);
    if (attributeType == null) {
      throwErrorCase(id);
    }
    return attributeType;
  }

  /**
   * Get the dialect instance for the given attribute.
   *
   * @param id The id of the attribute.
   * @return The dialect instance.
   * @throws IllegalArgumentException If the attribute or dialect does not exist.
   */
  public static IDialect getAttributeDialect(String id) throws IllegalArgumentException {
    var dialect = ATTRIBUTE_DIALECT.get(id);
    if (dialect == null) {
      throwErrorCase(id);
    }
    return dialect;
  }
}
