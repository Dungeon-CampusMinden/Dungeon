package blockly.vm.dgir.dialect.func.types;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.func.Func;

import java.util.ArrayList;
import java.util.List;

public class FuncType extends Type {
  public static final FuncType INSTANCE = new FuncType();

  private List<Type> inputs = new ArrayList<>();
  private Type output = null;

  @Override
  public TypeDetails.Impl createImpl() {
    class FuncTypeModel extends TypeDetails.Impl {
      FuncTypeModel(String name, Class<? extends Type> type, Dialect dialect) {
        super(name, type, dialect);
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
      public void fromParameterizedIdent(String parameterizedIdent, Type type) {
        assert type instanceof FuncType : "Expected FuncType, got " + type.getClass().getSimpleName();
        var funcType = (FuncType) type;
        // Example: func.func<((int, string, ptr<int>, struct<int, float, ptr<bool>) -> (bool))>
        // Strip prefix and suffix
        String inner = parameterizedIdent.substring(FuncType.getIdent().length() + 1, parameterizedIdent.length() - 1);
        // Split inputs and output
        String[] parts = inner.split("->", -1);
        String inputsPart = parts[0].trim();
        String outputPart = parts[1].trim();
        {
          // Parse inputs
          inputsPart = inputsPart.substring(1, inputsPart.length() - 1).trim(); // Remove surrounding parentheses
          // Handle all inputs, including nested parameterized types
          funcType.inputs = TypeDetails.fromParameterString(inputsPart);
        }
        {
          // Parse output
          outputPart = outputPart.substring(1, outputPart.length() - 1).trim(); // Remove surrounding parentheses
          funcType.output = TypeDetails.fromParameterizedIdent(outputPart);
        }
      }
    }
    return new FuncTypeModel(getIdent(), getClass(), Dialect.get(Func.class));
  }

  public FuncType() {
  }

  public FuncType(List<Type> inputs, Type output) {
    this.inputs = inputs;
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
    return false;
  }

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }
}
