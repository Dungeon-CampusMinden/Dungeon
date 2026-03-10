package dgir.core;

import dgir.core.ir.*;
import dgir.dialect.arith.ArithDialect;
import dgir.dialect.builtin.BuiltinDialect;
import dgir.dialect.cf.CfDialect;
import dgir.dialect.func.FuncDialect;
import dgir.dialect.io.IoDialect;
import dgir.dialect.scf.ScfDialect;
import dgir.dialect.str.StrDialect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Base class for all DGIR dialects.
 *
 * <p>A dialect groups a set of related {@link Op operations}, {@link Type types}, and {@link
 * Attribute attributes} under a shared namespace. Dialects are registered once at startup via
 * {@link #registerAllDialects()}, which calls {@link #register()} on each one and populates the
 * global {@link DGIRContext} registries.
 */
public abstract class Dialect {
  Logger logger = Logger.getLogger(getClass().getName());

  // =========================================================================
  // Dialect Info
  // =========================================================================

  /** The namespace prefix used in operation/type idents (e.g. {@code "arith"}, {@code "func"}). */
  @Contract(pure = true)
  public abstract @NotNull String getNamespace();

  /** All operation prototypes contributed by this dialect. */
  @Contract(pure = true)
  public abstract @NotNull @Unmodifiable List<Op> allOps();

  /** All type prototypes contributed by this dialect. */
  @Contract(pure = true)
  public abstract @NotNull @Unmodifiable List<Type> allTypes();

  /** All attribute prototypes contributed by this dialect. */
  @Contract(pure = true)
  public abstract @NotNull @Unmodifiable List<Attribute> allAttributes();

  // =========================================================================
  // Registration
  // =========================================================================

  /**
   * Register this dialect in the global {@link DGIRContext}. Inserts all ops, types, and attributes
   * into their respective registries.
   */
  public void register() {
    if (DGIRContext.registeredDialects.containsKey(this.getClass())) {
      return;
    }
    DGIRContext.registeredDialects.put(this.getClass(), this);
    DGIRContext.registeredDialectsByName.put(this.getNamespace(), this);

    logger.info("Registering dialect: " + getNamespace());
    for (var op : allOps()) {
      OperationDetails.Registered.insert(op);
    }
    for (var type : allTypes()) {
      TypeDetails.Registered.insert(type);
    }
    for (var attr : allAttributes()) {
      AttributeDetails.Registered.insert(attr);
    }
    logger.info("Registered dialect successfully: " + getNamespace());
  }

  // =========================================================================
  // Static Helpers
  // =========================================================================

  /**
   * Look up a registered dialect by its class.
   *
   * @param dialectClass The class of the dialect to look up (e.g. {@code ArithDialect.class} or
   *     {@code FuncDialect.class}).
   * @return An optional containing the registered dialect, or empty if no such dialect is
   *     registered.
   */
  @Contract(pure = true)
  public static @NotNull Optional<Dialect> get(@NotNull Class<? extends Dialect> dialectClass) {
    return Optional.ofNullable(DGIRContext.registeredDialects.get(dialectClass));
  }

  /**
   * Look up a registered dialect by its class, throwing an exception if no such dialect is
   * registered.
   *
   * @param dialectClass The class of the dialect to look up (e.g. {@code ArithDialect.class} or
   *     {@code FuncDialect.class}).
   * @return The registered dialect.
   */
  @Contract(pure = true)
  public static @NotNull Dialect getOrThrow(@NotNull Class<? extends Dialect> dialectClass) {
    return get(dialectClass)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Dialect not registered: " + dialectClass.getSimpleName()));
  }

  /**
   * Register all built-in dialects in dependency order. Must be called once before constructing any
   * IR.
   */
  public static void registerAllDialects() {
    List<Dialect> dialects =
        List.of(
            new ArithDialect(),
            new BuiltinDialect(),
            new CfDialect(),
            new FuncDialect(),
            new IoDialect(),
            new ScfDialect(),
            new StrDialect());
    dialects.forEach(Dialect::register);
  }
}
