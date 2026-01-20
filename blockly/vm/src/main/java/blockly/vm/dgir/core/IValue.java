package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.ValueTypeIdResolver;
import blockly.vm.dgir.core.type.IType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "valType")
@JsonTypeIdResolver(ValueTypeIdResolver.class)
public interface IValue {
  IType getType();

  Object getValue();
}
