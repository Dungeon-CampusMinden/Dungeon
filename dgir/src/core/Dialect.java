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

/**
 * Base class for all DGIR dialects.
 * <p>
 * A dialect groups a set of related {@link Op operations}, {@link Type types}, and
 * {@link Attribute attributes} under a shared namespace. Dialects are registered once at
 * startup via {@link #registerAllDialects()}, which calls {@link #init()} on each one and
 * populates the global {@link DGIRContext} registries.
 */
public abstract class Dialect {

  // =========================================================================
  // Dialect Info
  // =========================================================================

  /**
   * The namespace prefix used in operation/type idents (e.g. {@code "arith"}, {@code "func"}).
   */
  public abstract String getNamespace();

  /**
   * All operation prototypes contributed by this dialect.
   */
  public abstract List<Op> allOps();

  /**
   * All type prototypes contributed by this dialect.
   */
  public abstract List<Type> allTypes();

  /**
   * All attribute prototypes contributed by this dialect.
   */
  public abstract List<Attribute> allAttributes();

  // =========================================================================
  // Registration
  // =========================================================================

  /**
   * Register this dialect in the global {@link DGIRContext}.
   * Inserts all ops, types, and attributes into their respective registries.
   */
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

  // =========================================================================
  // Static Helpers
  // =========================================================================

  /**
   * Look up a registered dialect by class.
   *
   * @param dialectClass The dialect class to look up.
   * @return The registered dialect instance.
   */
  public static Dialect get(Class<? extends Dialect> dialectClass) {
    return DGIRContext.registeredDialects.get(dialectClass);
  }

  /**
   * Register all built-in dialects in dependency order.
   * Must be called once before constructing any IR.
   */
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
