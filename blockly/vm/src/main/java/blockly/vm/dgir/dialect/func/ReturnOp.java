package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.traits.ITerminator;
import blockly.vm.dgir.dialect.builtin.Builtin;

import java.util.List;

public class ReturnOp extends Op implements ITerminator {
  @Override
  public OperationDetails.Impl createDetails() {
    class ReturnOpModel extends OperationDetails.Impl {
      ReturnOpModel() {
        super(ReturnOp.getIdent(), ReturnOp.class, DGIRContext.registeredDialects.get(Builtin.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
        // TODO This check still has to be implemented
        System.out.println("Missing verification for operation " + getIdent());
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
      }
    }
    return new ReturnOpModel();
  }

  public ReturnOp() {
  }

  public ReturnOp(Operation operation) {
    super(operation);
  }

  public ReturnOp(List<Value> operands) {
    setOperation(Operation.Create(getIdent(), operands, null, null));
  }

  public static String getIdent() {
    return "func.return";
  }

  public static String getNamespace() {
    return "func";
  }
}
