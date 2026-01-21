package blockly.vm.dgir.core.type;


import blockly.vm.dgir.core.IDialect;

public abstract class PrimitiveType extends Type {
  public PrimitiveType(Class<? extends IDialect> dialectClass, String ident) {
    super(dialectClass, ident);
  }
}

