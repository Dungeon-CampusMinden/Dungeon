package dgir.dialect.func;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.ir.Type;
import dgir.core.ir.TypeDetails;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public sealed interface FuncTypes {
  /**
   * Abstract base class for all types contributed by the {@link FuncDialect}.
   *
   * <p>Subclasses must implement {@link #getIdent()}, {@link #getValidator()}, and, for
   * parameterized types, {@link #getParameterizedIdent()} and {@link
   * #getParameterizedStringFactory()}.
   */
  abstract class FuncBaseType extends Type {

    @Override
    public @NotNull String getNamespace() {
      return "func";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return FuncDialect.class;
    }
  }

  /**
   * Function signature type in the {@code func} dialect.
   *
   * <p>A {@code FuncType} describes a function's parameter types and optional return type:
   *
   * <pre>
   *   func.func&lt;(int32, string) -&gt; (bool)&gt;
   * </pre>
   *
   * <p>The {@link #getParameterizedIdent()} method renders the full signature; simple (void/no-arg)
   * function types can be compared by this string.
   */
  final class FuncType extends FuncBaseType implements FuncTypes {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull String getIdent() {
      return "func.func";
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getParameterizedIdent() {
      return getIdent()
          + "<("
          + String.join(", ", getInputs().stream().map(Type::getParameterizedIdent).toList())
          + ") -> ("
          + (getOutput() == null ? "" : getOutput().getParameterizedIdent())
          + ")>";
    }

    @Override
    public Function<Pair<String, TypeDetails>, Type> getParameterizedStringFactory() {
      return args -> {
        // Extract the single parameter (the full "(inputs) -> (output)" string), then split on "->"
        // at depth 0 so nested func types containing "->" are never split prematurely.
        String param = Utils.getParameterStrings(args.getLeft()).getFirst();
        List<String> arrowParts = Utils.splitAtDepthZero(param, "->");
        String inputsPart = arrowParts.get(0).trim();
        String outputPart = arrowParts.get(1).trim();
        List<Type> inputs;
        {
          inputsPart = inputsPart.substring(1, inputsPart.length() - 1).trim();
          inputs = TypeDetails.fromParameterString(inputsPart);
        }
        Type output;
        {
          outputPart = outputPart.substring(1, outputPart.length() - 1).trim();
          if (outputPart.isEmpty()) {
            output = null;
          } else {
            output = TypeDetails.fromParameterizedIdent(outputPart);
          }
        }
        return new FuncType(inputs, output);
      };
    }

    @Override
    public @NotNull @Unmodifiable List<Type> getDefaultTypeInstances() {
      return List.of(this);
    }

    @Override
    public Function<Object, Boolean> getValidator() {
      // I currently do not know what about a value passed to type should do wrong since its just
      // other types.
      return value -> true;
    }

    // =========================================================================
    // Members
    // =========================================================================

    /** The ordered list of input types (never {@code null}, may be empty). */
    private final List<Type> inputs;

    /** The return type, or {@code null} for void functions. */
    private final @Nullable Type output;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a no-argument void function type. */
    public FuncType() {
      inputs = List.of();
      output = null;
    }

    /**
     * Create a function type with the given input types and return type.
     *
     * @param inputs the ordered list of parameter types; must not be {@code null}.
     * @param output the return type, or {@code null} for a void function.
     */
    public FuncType(@NotNull List<Type> inputs, @Nullable Type output) {
      this.inputs = Collections.unmodifiableList(inputs);
      this.output = output;
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the ordered list of input (parameter) types.
     *
     * @return immutable list of input types.
     */
    @Contract(pure = true)
    public @NotNull List<Type> getInputs() {
      return inputs;
    }

    /**
     * Returns the return type of this function, or {@code null} for void functions.
     *
     * @return the return type, or {@code null}.
     */
    @Contract(pure = true)
    public @Nullable Type getOutput() {
      return output;
    }
  }
}
