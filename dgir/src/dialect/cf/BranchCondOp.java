package dialect.cf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.IControlFlow;
import core.traits.ITerminator;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BranchCondOp extends Op implements ITerminator, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class BranchCondOpDetails extends OperationDetails.Impl {
      BranchCondOpDetails() {
        super(BranchCondOp.getIdent(), BranchCondOp.class, Dialect.getOrThrow(CF.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {
      }
    }
    return new BranchCondOpDetails();
  }

  public static String getIdent() {
    return "cf.br_cond";
  }

  public static String getNamespace() {
    return "cf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public BranchCondOp() {
  }

  public BranchCondOp(Operation operation) {
    super(operation);
  }

  public BranchCondOp(Value condition, Block target, Block elseTarget) {
    super(Operation.Create(getIdent(), List.of(condition), List.of(target, elseTarget), null));
    assert condition.getType().equals(IntegerT.BOOL) : "Condition must be of type bool/int1.";
  }
}
