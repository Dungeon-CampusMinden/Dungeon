package blockly.vm.dgir.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Stores all the dialects and their operations/types.
 */
public class DialectRegistry {
  // Map namespace to operations and operation name to operation
  private static final Map<String, IOperation> OPS = new HashMap<>();
  // Keep a flat lookup by namespace+name for resolver-friendly class lookup
  private static final Map<String, Class<? extends IOperation>> OP_TYPES = new HashMap<>();

  // Add an operation to the registry
  public static void add(IOperation op) {
    String fullName = op.getFullName();
    OPS.put(fullName, op);
    OP_TYPES.put(fullName, op.getClass());
  }

  /**
   * Get an operation by namespace and name.
   * @param namespace Namespace of the operation.
   * @param name Name of the operation.
   * @return Returns a copy of the operation so that it can be initialized later.
   */
  public static Optional<IOperation> get(String namespace, String name) {
    // Get the operation prototype
    IOperation op = OPS.get(namespace + "." + name);
    // If it exists, create a copy of it which can later be initialized
    if (op != null) {
      return Optional.of(op.clone());
    }
    // Return an empty optional if the operation does not exist
    return Optional.empty();
  }

  /**
   * Get an operation by full name.
   * @param fullName Namespace + OpName
   * @return Returns a copy of the operation so that it can be initialized later.
   */
  public static Optional<IOperation> get(String fullName) {
    // Get the operation prototype
    IOperation op = OPS.get(fullName);
    // If it exists, create a copy of it which can later be initialized
    if (op != null) {
      return Optional.of(op.clone());
    }
    // Return an empty optional if the operation does not exist
    return Optional.empty();
  }

  /**
   * Get the registered operation class for the given namespace and name.
   */
  public static Optional<Class<? extends IOperation>> getType(String namespace, String name) {
    return Optional.ofNullable(OP_TYPES.get(namespace + "." + name));
  }

  public static Optional<Class<? extends IOperation>> getType(String fullName) {
    return Optional.ofNullable(OP_TYPES.get(fullName));
  }

}
