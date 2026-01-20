package blockly.vm.dgir.core.type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Float32_t implements IType {
  public static final Float32_t INSTANCE = new Float32_t();

  @Override
  public String getIdentifier() {
    return "float32";
  }

  @Override
  public Object getDefaultValue() {
    return 0.0f;
  }

  @Override
  public List<IType> getArguments() {
    return List.of();
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof Float;
  }
}
