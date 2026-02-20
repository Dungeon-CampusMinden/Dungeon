package core.detail;

import core.DGIRContext;
import core.ir.Attribute;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A fully registered {@link AttributeDetails} instance.
 * Created by {@link #insert(Attribute)} during dialect initialisation.
 */
public class RegisteredAttributeDetails extends AttributeDetails {

  // =========================================================================
  // Static Registration
  // =========================================================================

  /**
   * Register the given attribute in the global context.
   * If the attribute already carries a {@link RegisteredAttributeDetails}, it is reused;
   * otherwise a new one is created via {@link Attribute#createImpl()}.
   *
   * @param attr The attribute instance to register.
   */
  public static void insert(@NotNull Attribute attr) {
    RegisteredAttributeDetails details;
    if (attr.getDetails() instanceof RegisteredAttributeDetails existing) {
      details = existing;
    } else {
      details = new RegisteredAttributeDetails(attr.createImpl());
    }

    // Populate the unregistered caches so look-ups before registration still resolve
    DGIRContext.attributes.put(details.getType(), details.getImpl());
    DGIRContext.attributesByIdent.put(details.getIdent(), details.getImpl());

    // Populate the registered caches
    DGIRContext.registeredAttributes.put(details.getType(), details);
    DGIRContext.registeredAttributesByIdent.put(details.getIdent(), details);

    attr.setDetails(details);
  }

  // =========================================================================
  // Static Lookups
  // =========================================================================

  @Contract(pure = true)
  public static @NotNull Optional<RegisteredAttributeDetails> lookup(@NotNull Class<? extends Attribute> clazz) {
    return Optional.ofNullable(DGIRContext.registeredAttributes.get(clazz));
  }

  @Contract(pure = true)
  public static @NotNull Optional<RegisteredAttributeDetails> lookup(@NotNull String name) {
    return Optional.ofNullable(DGIRContext.registeredAttributesByIdent.get(name));
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  protected RegisteredAttributeDetails(@NotNull Impl impl) {
    super(impl);
  }
}
