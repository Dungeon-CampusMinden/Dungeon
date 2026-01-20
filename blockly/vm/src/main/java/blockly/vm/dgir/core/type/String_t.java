package blockly.vm.dgir.core.type;

import java.util.List;

public class String_t implements IType {
  public static final String_t INSTANCE = new String_t();

  @Override
  public String getIdentifier() {
    return "string";
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public List<IType> getArguments() {
    return List.of();
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof String;
  }
}
