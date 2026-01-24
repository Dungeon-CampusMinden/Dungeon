package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.attributes.IntegerAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.TypeAttribute;
import blockly.vm.dgir.dialect.builtin.types.FloatT;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import blockly.vm.dgir.dialect.builtin.types.StringT;
import blockly.vm.dgir.dialect.io.PrintOp;

import java.util.List;

public class Builtin extends Dialect {
  @Override
  public String getNamespace() {
    return "";
  }

  @Override
  public List<Op> allOps() {
    return List.of(
      new ProgramOp()
    );
  }

  @Override
  public List<Type> allTypes() {
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
  public List<Attribute> allAttributes() {
    return List.of(
      IntegerAttribute.INSTANCE,
      StringAttribute.INSTANCE,
      TypeAttribute.INSTANCE
    );
  }
}

