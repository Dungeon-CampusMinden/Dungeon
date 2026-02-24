package core;

import core.detail.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Global registry for all dialects, operations, types, and attributes known to the DGIR.
 *
 * <p>Each category has two parallel maps: an <em>unregistered</em> cache (populated on first
 * reference, even before a dialect is initialised) and a <em>registered</em> map (populated by the
 * dialect's {@code init()} call). Look-ups always prefer the registered entry.
 */
public class DGIRContext {

  // =========================================================================
  // Operations
  // =========================================================================

  /** Unregistered cache: class → impl (ident and class are unreliable until registered). */
  public static final @NotNull Map<Class<? extends Op>, OperationDetails> operations =
      new HashMap<>();

  /** Unregistered cache: ident → impl. */
  public static final @NotNull Map<String, OperationDetails> operationsByIdent = new HashMap<>();

  /** Registered operations by class. */
  public static final @NotNull Map<Class<? extends Op>, OperationDetails.Registered>
      registeredOperations = new HashMap<>();

  /** Registered operations by ident. */
  public static final @NotNull Map<String, OperationDetails.Registered>
      registeredOperationsByIdent = new HashMap<>();

  // =========================================================================
  // Attributes
  // =========================================================================

  /** Unregistered cache: class → details (ident and class are unreliable until registered). */
  public static final @NotNull Map<Class<? extends Attribute>, AttributeDetails> attributes =
      new HashMap<>();

  /** Unregistered cache: ident → details. */
  public static final @NotNull Map<String, AttributeDetails> attributesByIdent = new HashMap<>();

  /** Registered attributes by class. */
  public static final @NotNull Map<Class<? extends Attribute>, AttributeDetails.Registered>
      registeredAttributes = new HashMap<>();

  /** Registered attributes by ident. */
  public static final @NotNull Map<String, AttributeDetails.Registered>
      registeredAttributesByIdent = new HashMap<>();

  // =========================================================================
  // Types
  // =========================================================================

  /** Unregistered cache: class → impl. */
  public static final @NotNull Map<Class<? extends Type>, TypeDetails> types = new HashMap<>();

  /** Unregistered cache: ident → impl. */
  public static final @NotNull Map<String, TypeDetails> typesByIdent = new HashMap<>();

  /** Registered types by class. */
  public static final Map<Class<? extends Type>, TypeDetails.Registered> registeredTypes =
      new HashMap<>();

  /** Registered types by ident. */
  public static final Map<String, TypeDetails.Registered> registeredTypesByIdent = new HashMap<>();

  // =========================================================================
  // Dialects
  // =========================================================================

  /** All registered dialects by class. */
  public static final Map<Class<? extends Dialect>, Dialect> registeredDialects = new HashMap<>();

  /** All registered dialects by namespace string. */
  public static final Map<String, Dialect> registeredDialectsByName = new HashMap<>();

  // =========================================================================
  // Static Helpers
  // =========================================================================

  /**
   * Resolve the dialect that owns the given type or operation name.
   *
   * <p>If the name contains a {@code '.'}, the part before the first dot is treated as the dialect
   * namespace. If no matching dialect is found, the builtin dialect ({@code ""}) is returned.
   *
   * @param name The ident string to resolve (e.g. {@code "arith.constant"} or {@code "int32"}).
   * @return The owning {@link Dialect}, or the builtin dialect as a fallback.
   */
  @Contract(pure = true)
  public static @NotNull Optional<Dialect> getReferencedDialect(@NotNull String name) {
    var i = name.indexOf('.');
    if (i >= 0) {
      var namespace = name.substring(0, i);
      var dialect = registeredDialectsByName.get(namespace);
      if (dialect != null) {
        return Optional.of(dialect);
      }
    }
    return Optional.empty();
  }
}
