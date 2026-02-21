package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.Op;
import core.ir.Operation;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class BreakOp extends Op implements ITerminator, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull OperationDetails.Impl createDetails() {
    class BreakOpDetails extends OperationDetails.Impl {
      BreakOpDetails() {
        super(BreakOp.getIdent(), BreakOp.class, Dialect.getOrThrow(SCF.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<core.ir.NamedAttribute> attributes) {}
    }
    return new BreakOpDetails();
  }

  public static @NotNull String getIdent() {
    return "scf.break";
  }

  public static @NotNull String getNamespace() {
    return "scf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public BreakOp() {
    executeIfRegistered(
        BreakOp.class, () -> setOperation(false, Operation.Create(getIdent(), null, null, null)));
  }

  public BreakOp(Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Override
  public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
    return List.of(ForOp.class);
  }
}
