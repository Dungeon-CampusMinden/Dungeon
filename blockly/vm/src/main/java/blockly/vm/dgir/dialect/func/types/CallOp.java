package blockly.vm.dgir.dialect.func.types;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.func.Func;

import java.util.List;

public class CallOp extends Op {
  @Override
  public OperationDetails.Impl createDetails() {
    class CallOpModel extends OperationDetails.Impl {
      CallOpModel() {
        super(CallOp.getIdent(), CallOp.class, DGIRContext.registeredDialects.get(Func.class), null);
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {

      }
    }
    return new CallOpModel();
  }

  public static String getIdent() {
    return "func.call";
  }

  public static String getNamespace() {
    return "func";
  }
}
