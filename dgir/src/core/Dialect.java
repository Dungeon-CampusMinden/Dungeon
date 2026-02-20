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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

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
  public abstract @NotNull String getNamespace();

  /**
   * All operation prototypes contributed by this dialect.
   */
  public abstract @NotNull @Unmodifiable List<Op> allOps();

  /**
   * All type prototypes contributed by this dialect.
   */
  public abstract @NotNull @Unmodifiable List<Type> allTypes();

  /**
   * All attribute prototypes contributed by this dialect.
   */
  public abstract @NotNull @Unmodifiable List<Attribute> allAttributes();

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
   * Look up a registered dialect by its class.
   *
   * @param dialectClass The class of the dialect to look up (e.g. {@code Arith.class} or {@code Func.class}).
   * @return An optional containing the registered dialect, or empty if no such dialect is registered.
   */
  public static @NotNull Optional<Dialect> get(@NotNull Class<? extends Dialect> dialectClass) {
    return Optional.ofNullable(DGIRContext.registeredDialects.get(dialectClass));
  }

  /**
   * Look up a registered dialect by its class, throwing an exception if no such dialect is registered.
   *
   * @param dialectClass The class of the dialect to look up (e.g. {@code Arith.class} or {@code Func.class}).
   * @return The registered dialect.
   */
  public static @NotNull Dialect getOrThrow(@NotNull Class<? extends Dialect> dialectClass) {
    return get(dialectClass).orElseThrow(() -> new IllegalArgumentException("Dialect not registered: " + dialectClass.getSimpleName()));
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
