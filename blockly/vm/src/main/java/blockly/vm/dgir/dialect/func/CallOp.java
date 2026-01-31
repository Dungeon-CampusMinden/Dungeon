package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.traits.ICaller;
import blockly.vm.dgir.core.traits.IControlFlow;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.attributes.SymbolRefAttribute;
import blockly.vm.dgir.dialect.func.types.FuncType;

import java.util.List;

public class CallOp extends Op implements IControlFlow, ICaller {
  @Override
  public OperationDetails.Impl createDetails() {
    class CallOpModel extends OperationDetails.Impl {
      CallOpModel() {
        super(
          CallOp.getIdent(),
          CallOp.class,
          Dialect.get(Builtin.class),
          List.of(SymbolTable.getSymbolAttributeName())
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
    setName(name);
  }

  public CallOp(FuncOp funcOp, List<Value> operands) {
    setOperation(Operation.Create(getIdent(), operands, null, funcOp.getType().getOutput(), 0));
    setName(funcOp.getFuncName());
  }

  public String getName() {
    return getAttribute(SymbolRefAttribute.class, "name").getStorage();
  }

  public void setName(String name) {
    getAttribute(SymbolRefAttribute.class, SymbolTable.getSymbolAttributeName()).setValue(name);
  }
}
