package blockly.vm.dgir.core.type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public interface IType {
  public String getIdentifier();
  @JsonIgnore
  public Object getDefaultValue();
  public List<IType> getArguments();
  public boolean validate(Object value);
}
