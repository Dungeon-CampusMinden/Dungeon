package blockly.vm.dgir.core.type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Int32_t implements IType {
  public static final Int32_t INSTANCE = new Int32_t();

  @Override
  public String getIdentifier() {
    return "int32";
  }

  @Override
  @JsonIgnore
  public Object getDefaultValue() {
    return 0;
  }

  @Override
  public List<IType> getArguments() {
    return List.of();
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof Integer;
  }
}
