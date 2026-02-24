package dialect.builtin.types;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * UTF-16 string type in the {@code builtin} dialect.
 *
 * <p>Ident: {@code string}. Validated values must be Java {@link String} instances.
 *
 * <p>The single pre-built instance is available as {@link #INSTANCE}.
 */
public class StringT extends BuiltinType {

  // =========================================================================
  // Static Fields
  // =========================================================================

  /** Singleton instance of the string type. */
  public static final StringT INSTANCE = new StringT();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "string";
  }

  @Override
  public Function<Object, Boolean> getValidator() {
    return value -> value instanceof String;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Creates a new {@code StringT} instance. Prefer {@link #INSTANCE} over this constructor. */
  public StringT() {}
}
