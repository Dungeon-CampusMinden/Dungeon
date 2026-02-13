package dialect.func.types;

import core.*;
import core.detail.TypeDetails;
import core.ir.Type;
import dialect.func.Func;

import java.util.Collections;
import java.util.List;

public class FuncType extends Type {
  public static final FuncType INSTANCE = new FuncType();

  private final List<Type> inputs;
  private final Type output;

  @Override
  public TypeDetails.Impl createImpl() {
    class FuncTypeModel extends TypeDetails.Impl {
      FuncTypeModel() {
        super(INSTANCE, FuncType.getIdent(), FuncType.class, Dialect.get(Func.class));
      }

      @Override
      public String getParameterizedIdent(Type type) {
        assert type instanceof FuncType : "Expected FuncType, got " + type.getClass().getSimpleName();
        var funcType = (FuncType) type;
        return FuncType.getIdent() + "<("
          + String.join(", ", funcType.getInputs().stream().map(t -> t.getDetails().getParameterizedIdent(t)).toList())
          + ") -> ("
          + (funcType.getOutput() == null ?  "" : funcType.getOutput().getDetails().getParameterizedIdent(funcType.getOutput()))
          + ")>";
      }

      @Override
      public Type fromParameterizedIdent(String parameterizedIdent) {
        // Check that the ident matches up to the parameterized part
        String[] parts = getStrings(parameterizedIdent);
        String inputsPart = parts[0].trim();
        String outputPart = parts[1].trim();
        List<Type> inputs;
        Type output;
        {
          // Parse inputs
          inputsPart = inputsPart.substring(1, inputsPart.length() - 1).trim(); // Remove surrounding parentheses
          // Handle all inputs, including nested parameterized types
          inputs = TypeDetails.fromParameterString(inputsPart);
        }
        {
          // Parse output
          outputPart = outputPart.substring(1, outputPart.length() - 1).trim(); // Remove surrounding parentheses
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
        // Example: func.func<((int, string, ptr<int>, struct<int, float, ptr<bool>) -> (bool))>
        // Strip prefix and suffix
        String inner = parameterizedIdent.substring(FuncType.getIdent().length() + 1, parameterizedIdent.length() - 1);
        // Split inputs and output
        String[] parts = inner.split("->", -1);
        return parts;
      }
    }
    return new FuncTypeModel();
  }

  public FuncType() {
    inputs = List.of();
    output = null;
  }

  public FuncType(List<Type> inputs, Type output) {
    this.inputs = Collections.unmodifiableList(inputs);
    this.output = output;
  }

  public List<Type> getInputs() {
    return inputs;
  }

  public Type getOutput() {
    return output;
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof FuncType;
  }

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }
}
