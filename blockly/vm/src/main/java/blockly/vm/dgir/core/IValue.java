package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.ValueTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "valType")
@JsonTypeIdResolver(ValueTypeIdResolver.class)
public interface IValue {
  Type getType();
  @JsonIgnore()
  Object getValue();
}
