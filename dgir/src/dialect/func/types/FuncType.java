package dialect.func.types;

import core.*;
import core.detail.TypeDetails;
import core.ir.Type;
import dialect.func.Func;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FuncType extends Type {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final FuncType INSTANCE = new FuncType();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public TypeDetails.@NotNull Impl createImpl() {
    class FuncTypeModel extends TypeDetails.Impl {
      FuncTypeModel() {
        super(INSTANCE, FuncType.getIdent(), FuncType.class, Dialect.getOrThrow(Func.class));
      }

      @Override
      public @NotNull String getParameterizedIdent(@NotNull Type type) {
        assert type instanceof FuncType : "Expected FuncType, got " + type.getClass().getSimpleName();
        var funcType = (FuncType) type;
        return FuncType.getIdent() + "<("
          + String.join(", ", funcType.getInputs().stream().map(t -> t.getDetails().getParameterizedIdent(t)).toList())
          + ") -> ("
          + (funcType.getOutput() == null ? "" : funcType.getOutput().getDetails().getParameterizedIdent(funcType.getOutput()))
          + ")>";
      }

      @Override
      public @NotNull Type fromParameterizedIdent(@NotNull String parameterizedIdent) {
        String[] parts = getStrings(parameterizedIdent);
        String inputsPart = parts[0].trim();
        String outputPart = parts[1].trim();
        List<Type> inputs;
        Type output;
        {
          inputsPart = inputsPart.substring(1, inputsPart.length() - 1).trim();
          inputs = TypeDetails.fromParameterString(inputsPart);
        }
        {
          outputPart = outputPart.substring(1, outputPart.length() - 1).trim();
          if (outputPart.isEmpty()) {
            output = null;
          } else {
            output = TypeDetails.fromParameterizedIdent(outputPart);
          }
        }
        return new FuncType(inputs, output);
      }

      private String[] getStrings(String parameterizedIdent) {
        if (!parameterizedIdent.startsWith(FuncType.getIdent() + "<") || !parameterizedIdent.endsWith(">")) {
          throw new IllegalArgumentException("Invalid parameterized ident for FuncType: " + parameterizedIdent);
        }
        String inner = parameterizedIdent.substring(FuncType.getIdent().length() + 1, parameterizedIdent.length() - 1);
        return inner.split("->", -1);
      }
    }
    return new FuncTypeModel();
  }

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }

  // =========================================================================
  // Members
  // =========================================================================

  private final List<Type> inputs;
  private final Type output;

  // =========================================================================
  // Constructors
  // =========================================================================

  public FuncType() {
    inputs = List.of();
    output = null;
  }

  public FuncType(List<Type> inputs, Type output) {
    this.inputs = Collections.unmodifiableList(inputs);
    this.output = output;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public List<Type> getInputs() {
    return inputs;
  }

  public Type getOutput() {
    return output;
  }

  @Override
  public boolean validate(@Nullable Object value) {
    return value instanceof FuncType;
  }
}
