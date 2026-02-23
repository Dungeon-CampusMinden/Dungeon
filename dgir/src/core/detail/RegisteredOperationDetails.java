package core.detail;

import core.DGIRContext;
import core.ir.Op;
import java.util.Optional;

import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A fully registered {@link OperationDetails} instance. Created by {@link #insert(Op)} during
 * dialect initialisation.
 */
public class RegisteredOperationDetails extends OperationDetails {

  public static class RegisteredOperationImpl extends OperationDetails.Impl {
    public RegisteredOperationImpl(@NotNull Op op) {
      super(op);
    }
  }

  // =========================================================================
  // Static Registration
  // =========================================================================

  /**
   * Register the given op in the global context. If the op already carries a {@link
   * RegisteredOperationDetails}, it is reused. Otherwise, a new {@link RegisteredOperationDetails}
   * is created and used for registration.
   *
   * @param op The op instance to register.
   */
  public static void insert(@NotNull Op op) {
    RegisteredOperationDetails details;
    if (op.getOperationOrNull() != null
        && op.getDetails() instanceof RegisteredOperationDetails existing) {
      details = existing;
    } else {
      details = new RegisteredOperationDetails(new RegisteredOperationImpl(op));
    }

    // Populate the unregistered caches so look-ups before registration still resolve
    DGIRContext.operations.put(details.getType(), details.getImpl());
    DGIRContext.operationsByIdent.put(details.getIdent(), details.getImpl());

    // Populate the registered caches
    DGIRContext.registeredOperations.put(details.getType(), details);
    DGIRContext.registeredOperationsByIdent.put(
        details.getIdent(), new RegisteredOperationDetails(details.getImpl()));
  }

  // =========================================================================
  // Static Lookups
  // =========================================================================

  @Contract(pure = true)
  public static @NotNull Optional<RegisteredOperationDetails> lookup(
      @NotNull Class<? extends Op> clazz) {
    return Optional.ofNullable(DGIRContext.registeredOperations.get(clazz));
  }

  @Contract(pure = true)
  public static @NotNull Optional<RegisteredOperationDetails> lookup(@NotNull String name) {
    return Optional.ofNullable(DGIRContext.registeredOperationsByIdent.get(name));
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  protected RegisteredOperationDetails(@NotNull Impl impl) {
    super(impl);
  }
}
