package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.Op;
import core.ir.Operation;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Marks the end of a structured control flow region.
 */
public class ContinueOp extends Op implements ITerminator, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class ContinueOpDetails extends OperationDetails.Impl {
      ContinueOpDetails() {
        super(ContinueOp.getIdent(), ContinueOp.class, Dialect.getOrThrow(SCF.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<core.ir.NamedAttribute> attributes) {
      }
    }
    return new ContinueOpDetails();
  }

  public static String getIdent() {
    return "scf.continue";
  }

  public static String getNamespace() {
    return "scf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ContinueOp() {
    executeIfRegistered(ContinueOp.class, () ->
      setOperation(true, Operation.Create(getIdent(), null, null, null)));
  }

  public ContinueOp(Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
    return List.of(IfOp.class, ScopeOp.class, ForOp.class);
  }
}
