package dgir.core.ir;

import java.util.HashMap;

/**
 * Utility class for uniquing type instances. This is used to ensure that identical types are
 * represented by the same instance, which can save memory and enable fast equality checks.
 *
 * <p>If you use parametric types, make sure to run them through {@link #uniqueInstance(Type)}
 * before storing them or using them in IR construction, so that they are properly uniqued.
 */
public class TypeUniquer {
  private static final HashMap<String, Type> uniqueTypes = new HashMap<>();

  private TypeUniquer() {}

  /**
   * Returns a unique instance of the given type. If an instance with the same hash code already
   * exists, it returns the existing instance instead of the new one. This is used to save memory by
   * reusing identical type instances. It also enables fast equality checks between types, as
   * identical types will be the same instance.
   *
   * @param instance the type instance to uniquify
   * @return a unique instance of the given type
   */
  @SuppressWarnings("unchecked")
  public static <T extends Type> T uniqueInstance(T instance) {
    T existing = (T) uniqueTypes.get(instance.getParameterizedIdent());
    if (existing != null) {
      return existing;
    }
    uniqueTypes.put(instance.getParameterizedIdent(), instance);
    return instance;
  }
}
