package blockly.vm.dgir.dialect.cf;

import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.detail.OperationDetails;
import blockly.vm.dgir.core.ir.Block;
import blockly.vm.dgir.core.ir.NamedAttribute;
import blockly.vm.dgir.core.ir.Op;
import blockly.vm.dgir.core.ir.Operation;
import blockly.vm.dgir.core.traits.IControlFlow;
import blockly.vm.dgir.core.traits.ITerminator;

import java.util.List;

public class BranchOp extends Op implements ITerminator, IControlFlow {
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

  public BranchOp() {
  }

  public BranchOp(Operation operation) {
    super(operation);
  }

  public BranchOp(Block target){
    super(Operation.Create(getIdent(), null, List.of(target), null));
  }

  public Block getTarget(){
    return getSuccessors().getFirst();
  }
}
