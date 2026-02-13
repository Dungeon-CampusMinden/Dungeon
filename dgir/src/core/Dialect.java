package core;

import core.detail.RegisteredAttributeDetails;
import core.detail.RegisteredOperationDetails;
import core.detail.RegisteredTypeDetails;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.arith.Arith;
import dialect.builtin.Builtin;
import dialect.cf.CF;
import dialect.func.Func;
import dialect.io.IO;
import dialect.scf.SCF;

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
      new CF(),
      new Func(),
      new IO(),
      new SCF()
    );

    dialects.forEach(Dialect::init);
  }
}
