package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.Type;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores all the dialects and their operations/types.
 */
public class DialectRegistry {
  // Map the dialect class to the dialect instance
  private static final Map<Class<? extends IDialect>, IDialect> DIALECTS = new HashMap<>();
  // Keep a reference from the namespace to the dialect class
  private static final Map<String, Class<? extends IDialect>> DIALECT_TYPES = new HashMap<>();
  // Map namespace to operations and operation name to operation
  private static final Map<String, Operation> OPS = new HashMap<>();
  // Keep a flat lookup by namespace+name for resolver-friendly class lookup
  private static final Map<String, Class<? extends Operation>> OP_TYPES = new HashMap<>();
  // Reference from op id to its dialect
  private static final Map<String, IDialect> OP_DIALECT = new HashMap<>();
  // Map full type name to type instance
  private static final Map<String, Type> TYPES = new HashMap<>();
  // Keep a flat lookup by namespace+name for resolver-friendly class lookup
  private static final Map<String, Class<? extends Type>> TYPE_TYPES = new HashMap<>();
  // Reference from type id to its dialect
  private static final Map<String, IDialect> TYPE_DIALECT = new HashMap<>();


  public static void registerDialect(Class<? extends IDialect> dialectClass) {
    try {
      IDialect instance = dialectClass.getDeclaredConstructor().newInstance();
      DIALECTS.put(dialectClass, instance);
      DIALECT_TYPES.put(instance.getNamespace(), dialectClass);
      for (Operation op : instance.AllOperations()) {
        addOp(instance, op);
      }

      for (Type type : instance.AllTypes()) {
        addType(instance, type);
      }

    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Add an operation to the registry.
   * @param op The operation instance to add.
   */
  public static void addOp(IDialect dialect, Operation op) {
    OP_DIALECT.put(op.getFullName(), dialect);
    OPS.put(op.getFullName(), op);
    OP_TYPES.put(op.getFullName(), op.getClass());
  }

  /**
   * Add a type to the registry.
   * @param type The type instance to add.
   */
  public static void addType(IDialect dialect, Type type) {
    TYPE_DIALECT.put(type.getIdent(), dialect);
    TYPES.put(type.getIdent(), type);
    TYPE_TYPES.put(type.getIdent(), type.getClass());
  }

  /**
   * Get an operation by namespace and name.
   *
   * @param namespace Namespace of the operation.
   * @param name      Name of the operation.
   * @return Returns a copy of the operation so that it can be initialized later.
   */
  public static Optional<Operation> getOp(String namespace, String name) {
    // Get the operation prototype
    String fullName;
    if (namespace.isEmpty()) {
      fullName = name;
    } else {
      fullName = namespace + "." + name;
    }
    Operation op = OPS.get(fullName);
    // If it exists, create a copy of it which can later be initialized
    if (op != null) {
      return Optional.of(op.clone());
    }
    // Return an empty optional if the operation does not exist
    return Optional.empty();
  }

  /**
   * Get an operation by full name.
   *
   * @param fullName Namespace + OpName
   * @return Returns a copy of the operation so that it can be initialized later.
   */
  public static Optional<Operation> getOp(String fullName) {
    // Get the operation prototype
    Operation op = OPS.get(fullName);
    // If it exists, create a copy of it which can later be initialized
    if (op != null) {
      return Optional.of(op.clone());
    }
    // Return an empty optional if the operation does not exist
    return Optional.empty();
  }


  /**
   * Get a dialect by its class.
   *
   * @param dialectClass The dialect class.
   * @return The dialect instance.
   */
  public static Optional<IDialect> getDialect(Class<? extends IDialect> dialectClass) {
    return Optional.ofNullable(DIALECTS.get(dialectClass));
  }

  /**
   * Get a dialect by its namespace.
   *
   * @param dialectNamespace The dialect namespace.
   * @return The dialect instance.
   */
  public static Optional<IDialect> getDialect(String dialectNamespace) {
    return getDialectType(dialectNamespace).flatMap(DialectRegistry::getDialect);
  }

  /**
    * Get a dialect by its namespace.
    *
    * @param dialectNamespace The dialect namespace.
    * @return The dialect class.
    */
  public static Optional<Class<? extends IDialect>> getDialectType(String dialectNamespace) {
    return Optional.ofNullable(DIALECT_TYPES.get(dialectNamespace));
  }

  public static Optional<IDialect> getTypeDialect(String typeIdent){
    return Optional.ofNullable(TYPE_DIALECT.get(typeIdent));
  }



  /**
   * Get the registered operation class for the given namespace and name.
   */
  public static Optional<Class<? extends Operation>> getOpType(String namespace, String name) {
    return Optional.ofNullable(OP_TYPES.get(namespace + "." + name));
  }

  public static Optional<Class<? extends Operation>> getOpType(String fullName) {
    return Optional.ofNullable(OP_TYPES.get(fullName));
  }


  /**
   * Get the type class for the given id.
   * @param id The id of the type.
   * @return The type class.
   */
  public static Optional<Class<? extends Type>> getType(String id) {
    return Optional.ofNullable(TYPE_TYPES.get(id));
  }

  /**
   * Get the type instance for the given id.
   * @param id The id of the type.
   * @return The type instance.
   */
  public static Optional<Type> getTypeInstance(String id) {
    return Optional.ofNullable(TYPES.get(id));
  }
}
