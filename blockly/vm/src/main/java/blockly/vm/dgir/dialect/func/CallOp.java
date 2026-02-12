package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.detail.OperationDetails;
import blockly.vm.dgir.core.ir.*;
import blockly.vm.dgir.core.traits.ISymbolUser;
import blockly.vm.dgir.core.traits.IControlFlow;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.attributes.SymbolRefAttribute;
import blockly.vm.dgir.dialect.func.types.FuncType;

import java.util.List;
import java.util.Optional;

public class CallOp extends Op implements ISymbolUser {
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
        CallOp callOp = operation.as(CallOp.class);

        // Make sure that the callee function signature matches the call site
        Optional<FuncOp> callee = SymbolTable.lookupSymbolInNearestTableAsOp(operation, callOp.getCallee(), FuncOp.class);
        if (callee.isEmpty()) {
          operation.emitError("Could not find function " + callOp.getCallee());
          return false;
        }

        // Make sure that the function type matches the call site
        var calleeType = callee.get().getType();
        var funcType = callOp.getFunctionType();

        if (!calleeType.equals(funcType)) {
          callOp.getOperation().emitError("Function type does not match call site type for function " + callOp.getCallee() + ": " + calleeType.getParameterizedIdent() + " != " + funcType.getParameterizedIdent());
          return false;
        }
        return true;
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
    setOperation(Operation.Create(getIdent(), operands, null, calleeType.getOutput()));
    setCallee(name);
  }

  public CallOp(String name, FuncType calleeType, Value... operands) {
    this(name, List.of(operands), calleeType);
  }

  public CallOp(FuncOp funcOp, List<Value> operands) {
    setOperation(Operation.Create(getIdent(), operands, null, funcOp.getType().getOutput()));
    setCallee(funcOp.getFuncName());
  }

  public CallOp(FuncOp funcOp, Value... operands) {
    this(funcOp, List.of(operands));
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

  /**
   * Get the function type that results from this calls operands and output.
   *
   * @return The function type that results from this calls operands and output.
   */
  public FuncType getFunctionType() {
    List<Type> inputTypes = getOperands().stream().map(ValueOperand::getType).toList();
    Type outputType = getOutput() != null ? getOutput().getType() : null;
    return new FuncType(inputTypes, outputType);
  }

  @Override
  public SymbolRefAttribute getSymbolRefAttribute() {
    return getAttribute(SymbolRefAttribute.class, getCalleeAttributeName());
  }
}
