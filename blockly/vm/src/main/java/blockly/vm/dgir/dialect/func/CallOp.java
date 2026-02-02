package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.traits.ISymbolUser;
import blockly.vm.dgir.core.traits.IControlFlow;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.attributes.SymbolRefAttribute;
import blockly.vm.dgir.dialect.func.types.FuncType;

import java.util.List;

public class CallOp extends Op implements IControlFlow, ISymbolUser {
  @Override
  public OperationDetails.Impl createDetails() {
    class CallOpModel extends OperationDetails.Impl {
      CallOpModel() {
        super(
          CallOp.getIdent(),
          CallOp.class,
          Dialect.get(Builtin.class),
          List.of(getCalleeAttributeName())
        );
      }

      @Override
      public boolean verify(Operation operation) {
        return false;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
        attributes.get(0).setAttribute(new SymbolRefAttribute("foo"));
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

  public CallOp() {
  }

  public CallOp(Operation operation) {
    super(operation);
  }

  public CallOp(String name, List<Value> operands, FuncType calleeType) {
    setOperation(Operation.Create(getIdent(), operands, null, calleeType.getOutput(), 0));
    setCallee(name);
  }

  public CallOp(FuncOp funcOp, List<Value> operands) {
    setOperation(Operation.Create(getIdent(), operands, null, funcOp.getType().getOutput(), 0));
    setCallee(funcOp.getFuncName());
  }

  public String getCallee() {
    return getAttribute(SymbolRefAttribute.class, getCalleeAttributeName()).getStorage();
  }

  private void setCallee(String name) {
    getSymbolRefAttribute().setValue(name);
  }

  public static String getCalleeAttributeName() {
    return "callee";
  }

  @Override
  public SymbolRefAttribute getSymbolRefAttribute() {
    return getAttribute(SymbolRefAttribute.class, getCalleeAttributeName());
  }

  @Override
  public boolean verifySymbolUser() {
    var calleeName = getCallee();
    var calleeOp = SymbolTable.lookupSymbolInNearestTableAsOp(getOperation(), calleeName, FuncOp.class);
    if (calleeOp.isEmpty())
      return false;

    // Make sure that the function type matches the call site
    var funcType = calleeOp.get().getType();
    // Check operand types
    var operands = getOperands();
    // Check that we have the correct number of operands
    if (operands.size() != funcType.getInputs().size())
      return false;
    // Check that each operand type matches the function input type
    for (int i = 0; i < operands.size(); i++) {
      if (!operands.get(i).getType().equals(funcType.getInputs().get(i))) {
        return false;
      }
    }
    // Check that the outputs are the same
    if (getOutput() != null && funcType.getOutput() != null) {
      if (!getOutput().getType().equals(funcType.getOutput())) {
        return false;
      }
    }

    return true;
  }
}
