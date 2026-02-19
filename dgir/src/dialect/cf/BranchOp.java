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

public class BranchOp extends Op implements ITerminator, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.Impl createDetails() {
    class BranchOpDetails extends OperationDetails.Impl {
      BranchOpDetails() {
        super(BranchOp.getIdent(), BranchOp.class, Dialect.get(CF.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
      }
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

  public BranchOp() {
  }

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
