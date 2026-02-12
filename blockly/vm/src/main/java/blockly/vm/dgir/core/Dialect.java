package blockly.vm.dgir.core;

import blockly.vm.dgir.core.detail.RegisteredAttributeDetails;
import blockly.vm.dgir.core.detail.RegisteredOperationDetails;
import blockly.vm.dgir.core.detail.RegisteredTypeDetails;
import blockly.vm.dgir.core.ir.Attribute;
import blockly.vm.dgir.core.ir.Op;
import blockly.vm.dgir.core.ir.Type;
import blockly.vm.dgir.dialect.arith.Arith;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.func.Func;
import blockly.vm.dgir.dialect.io.IO;

import java.util.List;

public abstract class Dialect {
  public void init() {
    DGIRContext.registeredDialects.put(this.getClass(), this);
    DGIRContext.registeredDialectsByName.put(this.getNamespace(), this);

    System.out.println("Initializing dialect: " + getNamespace());
    for (var op : allOps()) {
      RegisteredOperationDetails.insert(op);
    }
    for (var type : allTypes()) {
      RegisteredTypeDetails.insert(type);
    }
    for (var attr : allAttributes()) {
      RegisteredAttributeDetails.insert(attr);
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

  public static void registerAllDialects() {
    List<Dialect> dialects = List.of(
      new Arith(),
      new Builtin(),
      new Func(),
      new IO()
    );

    dialects.forEach(Dialect::init);
  }
}
