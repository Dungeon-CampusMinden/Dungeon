package dialect.func;

import core.*;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.ISymbolUser;
import dialect.builtin.Builtin;
import dialect.builtin.attributes.SymbolRefAttribute;
import dialect.func.types.FuncType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class CallOp extends Op implements ISymbolUser {

  // =========================================================================
  // Type Info
  // =========================================================================

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
        CallOp callOp = operation.as(CallOp.class).orElseThrow();

        // Make sure that the callee function exists in the nearest symbol table
        Optional<FuncOp> callee = SymbolTable.lookupSymbolInNearestTableAsOp(operation, callOp.getCallee(), FuncOp.class);
        if (callee.isEmpty()) {
          operation.emitError("Could not find function " + callOp.getCallee());
          return false;
        }

        // Make sure that the function type matches the call site
        var calleeType = callee.get().getType();
        var funcType = callOp.getFunctionType();
        if (!calleeType.equals(funcType)) {
          callOp.getOperation().emitError("Function type does not match call site type for function "
            + callOp.getCallee() + ": "
            + calleeType.getParameterizedIdent() + " != " + funcType.getParameterizedIdent());
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

  public static String getCalleeAttributeName() {
    return "callee";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public CallOp() {
  }

  public CallOp(Operation operation) {
    super(operation);
  }

  public CallOp(String name, List<Value> operands, FuncType calleeType) {
    super(Operation.Create(getIdent(), operands, null, calleeType.getOutput()));
    setCallee(name);
  }

  public CallOp(String name, FuncType calleeType, Value... operands) {
    this(name, List.of(operands), calleeType);
  }

  public CallOp(FuncOp funcOp, List<Value> operands) {
    super(Operation.Create(getIdent(), operands, null, funcOp.getType().getOutput()));
    setCallee(funcOp.getFuncName());
  }

  public CallOp(FuncOp funcOp, Value... operands) {
    this(funcOp, List.of(operands));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public String getCallee() {
    return getAttribute(SymbolRefAttribute.class, getCalleeAttributeName())
      .orElseThrow(() -> new AssertionError("No callee attribute found"))
      .getStorage();
  }

  private void setCallee(String name) {
    getSymbolRefAttribute().setValue(name);
  }

  /**
   * Get the function type that results from this call's operands and output.
   *
   * @return The function type that results from this call's operands and output.
   */
  public @NotNull FuncType getFunctionType() {
    List<Type> inputTypes = getOperands().stream().map(ValueOperand::getType).toList();
    Type outputType = getOutput().map(OperationResult::getType).orElse(null);
    return new FuncType(inputTypes, outputType);
  }

  @Override
  public @NotNull SymbolRefAttribute getSymbolRefAttribute() {
    return getAttribute(SymbolRefAttribute.class, getCalleeAttributeName())
      .orElseThrow(() -> new RuntimeException("No symbol attribute found"));
  }
}
