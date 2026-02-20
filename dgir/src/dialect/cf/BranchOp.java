package dialect.cf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.Block;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.traits.IControlFlow;
import core.traits.ITerminator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class BranchOp extends Op implements ITerminator, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class BranchOpDetails extends OperationDetails.Impl {
      BranchOpDetails() {
        super(BranchOp.getIdent(), BranchOp.class, Dialect.getOrThrow(CF.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {}
    }
    return new BranchOpDetails();
  }

  public static String getIdent() {
    return "cf.br";
  }

  public static String getNamespace() {
    return "cf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public BranchOp() {}

  public BranchOp(Operation operation) {
    super(operation);
  }

  public BranchOp(Block target) {
    super(Operation.Create(getIdent(), null, List.of(target), null));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public Block getTarget() {
    return getSuccessors().getFirst();
  }
}
