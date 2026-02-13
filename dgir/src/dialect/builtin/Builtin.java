package dialect.builtin;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.attributes.SymbolRefAttribute;
import dialect.builtin.attributes.TypeAttribute;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import dialect.builtin.types.StringT;

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
      IntegerT.INT1,
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
      TypeAttribute.INSTANCE,
      SymbolRefAttribute.INSTANCE
    );
  }
}

