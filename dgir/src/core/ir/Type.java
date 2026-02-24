package core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import core.Dialect;
import core.Utils;
import core.detail.TypeDetails;
import core.serialization.TypeDeserializer;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.util.function.Function;

// We have to use the deserializer because we cant use @JsonCreator on static methods and therefore
// can put the logic
// directly in this class.
@JsonDeserialize(using = TypeDeserializer.class)
public abstract class Type {

  // =========================================================================
  // Members
  // =========================================================================

  @JsonIgnore private @NotNull TypeDetails details;

  // =========================================================================
  // Type Info
  // =========================================================================

  /**
   * Get the identifier for this type. This is a unique string that identifies the basic type
   * without any parameters. Example: {@code "i32"} or {@code "func.func"} (instead of {@code
   * func.func<...>}).
   *
   * <p>Syntax:
   *
   * <pre>
   * ident:
   *    namespace '.' name
   * </pre>
   *
   * @return The ident string.
   */
  @Contract(pure = true)
  public abstract @NotNull String getIdent();

  /**
   * Get the parameterized ident for this type. Simple types return just the ident; generic types
   * (e.g. {@link core.ir.Type} parameterized) override this to include parameters.
   *
   * <p>Syntax:
   *
   * <pre>
   * parameterizedType:
   *    ident
   *    | ident '&lt;' typeParameter (',' typeParameter)* '&gt;'
   *    | ident '&lt;' verbatim '&gt;'
   * </pre>
   *
   * @return The parameterized ident string.
   */
  @Contract(pure = true)
  @JsonValue
  public @NotNull String getParameterizedIdent() {
    return getIdent();
  }

  /**
   * Returns the namespace prefix for this type (e.g. {@code ""} for builtin types or
   * {@code "func"} for the func dialect).
   *
   * @return the namespace string, never {@code null}.
   */
  @Contract(pure = true)
  public abstract @NotNull String getNamespace();

  /**
   * Returns the class of the dialect that contributes this type.
   *
   * @return the dialect class, never {@code null}.
   */
  @Contract(pure = true)
  public abstract @NotNull Class<? extends Dialect> getDialect();

  /**
   * Returns a function that checks whether a given value is a valid instance of this type.
   *
   * <p>The validator is stored in {@link core.detail.TypeDetails.Registered} at registration time
   * and used by {@link #validate(Object)} to type-check attribute storage values.
   *
   * @return the validator function, never {@code null}.
   */
  @Contract(pure = true)
  public abstract Function<Object, Boolean> getValidator();

  /**
   * Returns a factory that creates a type from a parameterized identifier. This is used for types
   * that have parameters, such as ptrs or function types. The parameterized identifier is the
   * string representation of the type, including its parameters. For example, for a pointer type,
   * the parameterized identifier could be "ptr<i32>" or "ptr<ptr<f64>>".
   *
   * <p>The factory should parse the parameterized identifier and return the corresponding type
   * instance. For types that do not have parameters, this can simply return a factory that ignores
   * the parameterized identifier and returns the default instance of the type.
   *
   * @return A factory that creates a type from a parameterized identifier.
   */
  @Contract(pure = true)
  public Function<Pair<String, TypeDetails>, Type> getParameterizedStringFactory() {
    return args -> args.getRight().defaultInstance();
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public Type() {
    details = TypeDetails.get(getClass());
  }

  public Type(@NotNull TypeDetails typeDetails) {
    details = typeDetails;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull TypeDetails getDetails() {
    return details;
  }

  /**
   * Replace the details for this type. May only be called from
   * {@link core.detail.TypeDetails.Registered} during dialect registration.
   *
   * @param details the new details instance.
   * @throws AssertionError if called from outside {@link core.detail.TypeDetails.Registered}.
   */
  public void setDetails(@NotNull TypeDetails details) {
    // Only allow TypeDetails.Registered to set details, since they are the registration of the
    // types.
    assert Utils.Caller.getCallingClass().isAssignableFrom(TypeDetails.Registered.class)
        : "Only TypeDetails.Registered can set details for a type.";
    this.details = details;
  }

  public final boolean validate(Object value) {
    return getDetails().validator().apply(value);
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public String toString() {
    return getParameterizedIdent();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj instanceof Type other)
        && this.getParameterizedIdent().equals(other.getParameterizedIdent());
  }

  @Override
  public int hashCode() {
    return getParameterizedIdent().hashCode();
  }
}
