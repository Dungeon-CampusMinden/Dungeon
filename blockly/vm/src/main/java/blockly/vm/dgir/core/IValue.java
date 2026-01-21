package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.ValueTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "valueType")
@JsonTypeIdResolver(ValueTypeIdResolver.class)
public interface IValue extends Serializable {
  Type getType();
  @JsonIgnore()
  Object getValue();
}
