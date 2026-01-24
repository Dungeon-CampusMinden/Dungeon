package blockly.vm.dgir.core;

import java.util.List;

public abstract class Dialect {
  public void init() {
    DGIRContext.registeredDialects.put(this.getClass(), this);
    DGIRContext.registeredDialectsByName.put(this.getNamespace(), this);

    System.out.println("Initializing dialect: " + getNamespace());
    for (var op : allOps()) {
      RegisteredOperationName.insert(op.createImpl());
    }
    for (var type : allTypes()) {
      RegisteredTypeName.insert(type.createImpl());
    }
    for (var attr : allAttributes()) {
      RegisteredAttributeName.insert(attr.createImpl());
    }
    System.out.println("Dialect " + getNamespace() + " initialized successfully.");
  }

  public abstract String getNamespace();

  public abstract List<Op> allOps();

  public abstract List<Type> allTypes();

  public abstract List<Attribute> allAttributes();

  public static Dialect get(Class<? extends Dialect> dialectClass) {
    return DGIRContext.registeredDialects.get(dialectClass);
  }
}
