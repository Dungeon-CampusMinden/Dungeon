package core.detail;

import core.DGIRContext;
import core.ir.Type;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A fully registered {@link TypeDetails} instance. Created by {@link #insert(Type)} during dialect
 * initialisation.
 */
public class RegisteredTypeDetails extends TypeDetails {

  // =========================================================================
  // Static Registration
  // =========================================================================

  /**
   * Register the given type in the global context. If the type already carries a {@link
   * RegisteredTypeDetails}, it is reused; otherwise a new one is created via {@link
   * Type#createImpl()}.
   *
   * @param type The type instance to register.
   */
  public static void insert(@NotNull Type type) {
    RegisteredTypeDetails details;
    if (type.getDetails() instanceof RegisteredTypeDetails existing) {
      details = existing;
    } else {
      details = new RegisteredTypeDetails(type.createImpl());
    }

    // Populate the unregistered caches so look-ups before registration still resolve
    DGIRContext.types.put(details.getType(), details.getImpl());
    DGIRContext.typesByIdent.put(details.getIdent(), details.getImpl());

    // Populate the registered caches
    DGIRContext.registeredTypes.put(details.getType(), details);
    DGIRContext.registeredTypesByIdent.put(details.getIdent(), details);

    type.setDetails(details);
  }

  // =========================================================================
  // Static Lookups
  // =========================================================================

  @Contract(pure = true)
  public static @NotNull Optional<RegisteredTypeDetails> lookup(
      @NotNull Class<? extends Type> clazz) {
    return Optional.ofNullable(DGIRContext.registeredTypes.get(clazz));
  }

  @Contract(pure = true)
  public static @NotNull Optional<RegisteredTypeDetails> lookup(@NotNull String name) {
    return Optional.ofNullable(DGIRContext.registeredTypesByIdent.get(name));
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  protected RegisteredTypeDetails(@NotNull Impl impl) {
    super(impl);
  }
}
