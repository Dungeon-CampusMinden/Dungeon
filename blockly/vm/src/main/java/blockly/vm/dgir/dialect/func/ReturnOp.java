package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.opinterfaces.ITerminator;
import blockly.vm.dgir.dialect.builtin.Builtin;

import java.util.List;

public class ReturnOp extends Op implements ITerminator<ReturnOp> {
  @Override
  public OperationDetails.Impl createDetails() {
    class ReturnOpModel extends OperationDetails.Impl {
      ReturnOpModel() {
        super(ReturnOp.getIdent(), ReturnOp.class, DGIRContext.registeredDialects.get(Builtin.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
        return false;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
      }
    }
    return new ReturnOpModel();
  }

  public ReturnOp() {
  }

  public ReturnOp(List<Value> operands) {
    setOperation(Operation.Create(getIdent(), operands, null, null, null));
  }

  @Override
  public boolean verifyTrait(Operation op) {
    return true;
  }

  public static String getIdent() {
    return "func.return";
  }

  public static String getNamespace() {
    return "func";
  }
}
