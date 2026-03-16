package dgir.dialect.str;

import dgir.core.Dialect;
import dgir.core.ir.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

public sealed interface StrTypes {
  abstract class StrType extends Type {
    @Override
    public @NotNull String getNamespace() {
      return "str";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return StrDialect.class;
    }
  }

  /**
   * UTF-16 string type in the {@code builtin} dialect.
   *
   * <p>Ident: {@code string}. Validated values must be Java {@link String} instances.
   *
   * <p>The single pre-built instance is available as {@link #INSTANCE}.
   */
  final class StringT extends StrType implements StrTypes {

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

    @Override
    public @NotNull @Unmodifiable List<Type> getDefaultTypeInstances() {
      return List.of(INSTANCE);
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Creates a new {@code StringT} instance. Prefer {@link #INSTANCE} over this constructor. */
    StringT() {}
  }
}
