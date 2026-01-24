package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.core.TypeName;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class IntegerT extends Type {
  public static final IntegerT INT8 = new IntegerT(8);
  public static final IntegerT INT16 = new IntegerT(16);
  public static final IntegerT INT32 = new IntegerT(32);
  public static final IntegerT INT64 = new IntegerT(64);

  private final int width;

  @Override
  public TypeName.Impl createImpl() {
    class IntegerTModel extends TypeName.Impl {
      public IntegerTModel(String name, Class<? extends Type> type, Dialect dialect) {
        super(name, type, dialect);
      }
    }
    return new IntegerTModel(getIdent() + getWidth(), getClass(), Dialect.get(Builtin.class));
  }

  public IntegerT() {
    width = 32;
  }

  public IntegerT(int width) {
    this.width = width;
  }

  public int getWidth() {
    return width;
  }

  @Override
  public boolean validate(Object value) {
    if (!(value instanceof Number))
      return false;

    switch (value) {
      case Byte b when getWidth() == 8 -> {
        return true;
      }
      case Short i when getWidth() == 16 -> {
        return true;
      }
      case Integer i when getWidth() == 32 -> {
        return true;
      }
      case Long l when getWidth() == 64 -> {
        return true;
      }
      default -> {
        return false;
      }
    }
  }

  public static String getIdent() {
    return "int";
  }

  public static String getNamespace() {
    return "";
  }
}
