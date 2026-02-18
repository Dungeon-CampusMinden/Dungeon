package core.detail;

import core.DGIRContext;
import core.Dialect;
import core.ir.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * This object contains all the basic information about a type object.
 */
public class TypeDetails {
  public static TypeDetails get(String ident) {
    return new TypeDetails(ident);
  }

  public static TypeDetails get(Class<? extends Type> clazz) {
    return new TypeDetails(clazz);
  }

  private Impl impl = null;

  @JsonIgnore
  public Impl getImpl() {
    return impl;
  }

  protected TypeDetails(Impl impl) {
    this.impl = impl;
  }

  public String getIdent() {
    return impl.getIdent();
  }

  @JsonIgnore
  public Class<? extends Type> getType() {
    return impl.getType();
  }

  @JsonIgnore
  public Dialect getDialect() {
    return impl.getDialect();
  }

  @JsonIgnore
  public String getParameterizedIdent(Type type) {
    return impl.getParameterizedIdent(type);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TypeDetails other && this.impl == other.impl;
  }

  @Override
  public int hashCode() {
    return impl.hashCode();
  }

  /**
   * This is the fully type erased interface for a type object.
   */
  public abstract static class Impl {
    protected Type defaultInstance;
    protected String ident;
    protected Class<? extends Type> type;
    protected Dialect dialect;
    protected Constructor<? extends Type> constructor;

    public Impl(Type defaultInstance, String ident, Class<? extends Type> type, Dialect dialect) {
      this.defaultInstance = defaultInstance;
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
      // Pre-fetch the default constructor
      try {
        this.constructor = type.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Type class must have a default constructor: " + type.getSimpleName(), e);
      }
    }

    public String getIdent() {
      return ident;
    }

    public Class<? extends Type> getType() {
      return type;
    }

    public Dialect getDialect() {
      return dialect;
    }

    /**
     * Get the parameterized ident for this type given the provided type parameters.
     * This is used for generic or complex types that take type parameters such as pointers or function types.
     * <p>
     * The syntax for parameterized types is:
     * <pre>
     * parameterizedType:
     *    ident '<' typeParameter (',' typeParameter)* '>'
     *    | ident '<' verbatim '>'
     * typeParameter: ident | parameterizedType
     * verbatim: any string that is not parsed or interpreted, just passed through as-is
     * </pre>
     *
     * @param type The type parameters to use for generating the parameterized ident.
     * @return The parameterized ident string.
     */
    public String getParameterizedIdent(Type type) {
      return getIdent();
    }

    /**
     * Create a Type instance from the provided parameterized ident.
     * This is used for generic or complex types that take type parameters such as pointers or function types.
     * Still works for simple types.
     * <p>
     * The syntax for parameterized types is:
     * <pre>
     * parameterizedType:
     *    ident '<' typeParameter (',' typeParameter)* '>'
     *    | ident '<' verbatim '>'
     * typeParameter: ident | parameterizedType
     * verbatim: any string that is not parsed or interpreted, just passed through as-is
     * </pre>
     *
     * @param parameterizedIdent The parameterized ident string.
     */
    public Type fromParameterizedIdent(String parameterizedIdent) {
      try {
        return defaultInstance;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public <T extends Type> T fromParameterizedIdent(String parameterizedIdent, Class<T> clazz) {
      assert clazz.isAssignableFrom(defaultInstance.getClass()) : "Cannot create type of class " + clazz.getSimpleName() + " from the impl of type " + defaultInstance.getClass().getSimpleName();
      Type type = fromParameterizedIdent(parameterizedIdent);
      return clazz.cast(type);
    }

    public <T extends Type> T createInstance(Class<T> clazz) {
      try {
        return clazz.cast(constructor.newInstance());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException("Failed to create instance of type: " + type.getSimpleName(), e);
      }
    }
  }

  protected final static class UnregisteredType extends Impl {
    UnregisteredType(String ident, Class<? extends Type> clazz, Dialect dialect) {
      super(null, ident, clazz, dialect);
    }

  }

  public TypeDetails(String ident) {
    // Try to get the registered Type first
    TypeDetails registeredName = DGIRContext.registeredTypesByIdent.get(ident);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered operation next and if that doesn't work, add a new dummy
    Impl unregisteredName = DGIRContext.typesByIdent.get(ident);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.typesByIdent.put(ident,
      new UnregisteredType(ident, Type.class, DGIRContext.getReferencedDialect(ident)));
    DGIRContext.types.put(Type.class, unregisteredName);

    impl = unregisteredName;
  }

  public TypeDetails(Class<? extends Type> clazz) {
    // Try to get the registered Type first
    TypeDetails registeredName = DGIRContext.registeredTypes.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered type next nad if that doesn't work, add a new dummy
    Impl unregisteredName = DGIRContext.types.get(clazz);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.typesByIdent.put(clazz.getName(),
      new UnregisteredType(clazz.getName(), Type.class, null));
    DGIRContext.types.put(clazz, unregisteredName);

    impl = unregisteredName;
  }

  /**
   * Create a Type instance from the provided parameterized ident.
   * This is used for generic or complex types that take type parameters such as pointers or function types.
   * It also works for simple types.
   * Examples include:
   * <pre>
   * {@literal
   *   int32
   *   float64
   *   func.func<((int, string) -> (bool))>
   *   ptr.ptr<int>
   * }
   * </pre>
   *
   * @param parameterizedIdent The parameterized ident string.
   * @return The created Type instance.
   */
  public static Type fromParameterizedIdent(String parameterizedIdent) {
    // Extract the base ident
    String baseIdent = parameterizedIdent;
    int genericStart = parameterizedIdent.indexOf('<');
    if (genericStart != -1) {
      baseIdent = parameterizedIdent.substring(0, genericStart);
    }

    TypeDetails typeDetails = TypeDetails.get(baseIdent);
    return typeDetails.getImpl().fromParameterizedIdent(parameterizedIdent);
  }

  /**
   * Create a list of Type instances from the provided parameter string.
   * This is used for parsing multiple types from a comma-separated list.
   * It handles nested parameterized types correctly.
   * Examples include:
   * <pre>
   * {@literal
   *   * int32, float64, ptr<int>
   *   * func.func<(int, string, struct<bool, float, ptr<int>>) -> (bool)>, ptr<func.func<(int) -> (int)>>
   * }
   * </pre>
   *
   * @param parameterString The comma-separated parameter string.
   * @return The list of created Type instances.
   */
  public static List<Type> fromParameterString(String parameterString) {
    // Split on commas, but handle nested parameterized types
    List<Type> types = new java.util.ArrayList<>();
    int bracketLevel = 0;
    StringBuilder currentType = new StringBuilder();
    for (int i = 0; i < parameterString.length(); i++) {
      char c = parameterString.charAt(i);
      if (c == '<') bracketLevel++;
      if (c == '>') bracketLevel--;
      if (c == ',' && bracketLevel == 0) {
        types.add(fromParameterizedIdent(currentType.toString().trim()));
        currentType.setLength(0);
      } else {
        currentType.append(c);
      }
    }
    if (!currentType.isEmpty()) {
      types.add(fromParameterizedIdent(currentType.toString().trim()));
    }
    return types;
  }
}
