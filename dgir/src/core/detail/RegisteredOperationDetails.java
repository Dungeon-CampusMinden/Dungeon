package core.detail;

import core.DGIRContext;
import core.ir.Op;

import java.util.Optional;

/**
 * A fully registered {@link OperationDetails} instance.
 * Created by {@link #insert(Op)} during dialect initialisation.
 */
public class RegisteredOperationDetails extends OperationDetails {

  // =========================================================================
  // Static Registration
  // =========================================================================

  /**
   * Register the given op in the global context.
   * If the op already carries a {@link RegisteredOperationDetails}, it is reused;
   * otherwise a new one is created via {@link Op#createDetails()}.
   *
   * @param op The op instance to register.
   */
  public static void insert(Op op) {
    RegisteredOperationDetails details;
    if (op.getOperationOrNull() != null
      && op.getDetails() instanceof RegisteredOperationDetails existing) {
      details = existing;
    } else {
      details = new RegisteredOperationDetails(op.createDetails());
    }

    // Populate the unregistered caches so look-ups before registration still resolve
    DGIRContext.operations.put(details.getType(), details.getImpl());
    DGIRContext.operationsByIdent.put(details.getIdent(), details.getImpl());

    // Populate the registered caches
    DGIRContext.registeredOperations.put(details.getType(), details);
    DGIRContext.registeredOperationsByIdent.put(details.getIdent(), new RegisteredOperationDetails(details.getImpl()));
  }

  // =========================================================================
  // Static Lookups
  // =========================================================================

  public static Optional<RegisteredOperationDetails> lookup(Class<? extends Op> clazz) {
    return Optional.ofNullable(DGIRContext.registeredOperations.get(clazz));
  }

  public static Optional<RegisteredOperationDetails> lookup(String name) {
    return Optional.ofNullable(DGIRContext.registeredOperationsByIdent.get(name));
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  protected RegisteredOperationDetails(Impl impl) {
    super(impl);
  }
}
