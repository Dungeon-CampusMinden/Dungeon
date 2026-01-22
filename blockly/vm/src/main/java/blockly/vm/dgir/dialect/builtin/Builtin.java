package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.dialect.builtin.attributes.IntegerAttribute;
import blockly.vm.dgir.dialect.builtin.types.FloatT;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import blockly.vm.dgir.dialect.builtin.types.StringT;

import java.util.List;

public class Builtin implements IDialect {
  @Override
  public String getNamespace() {
    return "";
  }

  @Override
  public List<Operation> AllOperations() {
    return List.of(

    );
  }

  @Override
  public List<Type> AllTypes() {
    return List.of(
      IntegerT.INT8,
      IntegerT.INT16,
      IntegerT.INT32,
      IntegerT.INT64,
      FloatT.FLOAT32,
      FloatT.FLOAT64,
      StringT.INSTANCE
    );
  }

  @Override
  public List<Attribute> AllAttributes() {
    return List.of(
      IntegerAttribute.INSTANCE
    );
  }
}

