package blockly.vm.dgir.core.serialization;

import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

// TODO This is only a placeholder. Implement it properly
public class OperationTypeValidator extends PolymorphicTypeValidator {
  @Override
  public Validity validateBaseType(DatabindContext ctxt, JavaType baseType) {
    return Validity.INDETERMINATE;
  }

  @Override
  public Validity validateSubClassName(DatabindContext ctxt, JavaType baseType, String subClassName) {
    return Validity.INDETERMINATE;
  }

  @Override
  public Validity validateSubType(DatabindContext ctxt, JavaType baseType, JavaType subType) {
    return Validity.ALLOWED;
  }
}
