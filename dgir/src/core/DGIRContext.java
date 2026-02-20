package core;

import core.detail.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Global registry for all dialects, operations, types, and attributes known to the DGIR.
 * <p>
 * Each category has two parallel maps: an <em>unregistered</em> cache (populated on first
 * reference, even before a dialect is initialised) and a <em>registered</em> map (populated
 * by the dialect's {@code init()} call). Look-ups always prefer the registered entry.
 */
public class DGIRContext {

  // =========================================================================
  // Operations
  // =========================================================================

  /** Unregistered cache: class → impl (ident and class are unreliable until registered). */
  public static final @NotNull Map<Class<? extends Op>, OperationDetails.Impl> operations = new HashMap<>();
  /** Unregistered cache: ident → impl. */
  public static final @NotNull Map<String, OperationDetails.Impl> operationsByIdent = new HashMap<>();

  /** Registered operations by class. */
  public static final @NotNull Map<Class<? extends Op>, RegisteredOperationDetails> registeredOperations = new HashMap<>();
  /** Registered operations by ident. */
  public static final @NotNull Map<String, RegisteredOperationDetails> registeredOperationsByIdent = new HashMap<>();

  // =========================================================================
  // Attributes
  // =========================================================================

  /** Unregistered cache: class → impl. */
  public static final @NotNull Map<Class<? extends Attribute>, AttributeDetails.Impl> attributes = new HashMap<>();
  /** Unregistered cache: ident → impl. */
  public static final @NotNull Map<String, AttributeDetails.Impl> attributesByIdent = new HashMap<>();

  /** Registered attributes by class. */
  public static final @NotNull Map<Class<? extends Attribute>, RegisteredAttributeDetails> registeredAttributes = new HashMap<>();
  /** Registered attributes by ident. */
  public static final @NotNull Map<String, RegisteredAttributeDetails> registeredAttributesByIdent = new HashMap<>();

  // =========================================================================
  // Types
  // =========================================================================

  /** Unregistered cache: class → impl. */
  public static final @NotNull Map<Class<? extends Type>, TypeDetails.Impl> types = new HashMap<>();
  /** Unregistered cache: ident → impl. */
  public static final @NotNull Map<String, TypeDetails.Impl> typesByIdent = new HashMap<>();

  /** Registered types by class. */
  public static final Map<Class<? extends Type>, RegisteredTypeDetails> registeredTypes = new HashMap<>();
  /** Registered types by ident. */
  public static final Map<String, RegisteredTypeDetails> registeredTypesByIdent = new HashMap<>();

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
   * <p>
   * If the name contains a {@code '.'}, the part before the first dot is treated as the
   * dialect namespace. If no matching dialect is found, the builtin dialect ({@code ""})
   * is returned.
   *
   * @param name The ident string to resolve (e.g. {@code "arith.constant"} or {@code "int32"}).
   * @return The owning {@link Dialect}, or the builtin dialect as a fallback.
   */
  public static @NotNull Dialect getReferencedDialect(@NotNull String name) {
    var i = name.indexOf('.');
    if (i >= 0) {
      var namespace = name.substring(0, i);
      var dialect = registeredDialectsByName.get(namespace);
      if (dialect != null) {
        return dialect;
      }
    }
    return Objects.requireNonNull(registeredDialectsByName.get(""));
  }
}
