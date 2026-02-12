package blockly.vm.dgir.dialect.cf;

import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.detail.OperationDetails;
import blockly.vm.dgir.core.ir.*;
import blockly.vm.dgir.core.traits.IControlFlow;
import blockly.vm.dgir.core.traits.ITerminator;

import java.util.List;

public class BranchCondOp extends Op implements ITerminator, IControlFlow {
  @Override
  public OperationDetails.Impl createDetails() {
    class BranchCondOpDetails extends OperationDetails.Impl {
      BranchCondOpDetails() {
        super(BranchCondOp.getIdent(), BranchCondOp.class, Dialect.get(CF.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
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

  public BranchCondOp() {
  }

  public BranchCondOp(Operation operation) {
    super(operation);
  }

  public BranchCondOp(Value condition, Block target, Block elseTarget) {
    super(Operation.Create(getIdent(), List.of(condition), List.of(target, elseTarget), null));
  }
}
