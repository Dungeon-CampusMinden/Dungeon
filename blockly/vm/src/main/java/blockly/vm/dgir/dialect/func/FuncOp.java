package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.traits.IGlobal;
import blockly.vm.dgir.core.traits.IIsolatedFromAbove;
import blockly.vm.dgir.core.traits.ISymbol;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.TypeAttribute;
import blockly.vm.dgir.dialect.func.types.FuncType;

import java.util.List;

public class FuncOp extends Op implements ISymbol, IIsolatedFromAbove, IGlobal {
  @Override
  public OperationDetails.Impl createDetails() {
    class FuncOpModel extends OperationDetails.Impl {
      FuncOpModel() {
        super(FuncOp.getIdent(),
          FuncOp.class,
          DGIRContext.registeredDialects.get(Func.class),
          List.of(
            SymbolTable.getSymbolAttributeName(),
            "type")
        );
      }

      @Override
      public boolean verify(Operation operation) {
        // Ensure that the op has one region and that that region has exactly one block.
        if (operation.getRegions().size() != 1) {
          operation.emitError("Operation must have exactly one region");
          return false;
        }
        if (operation.getRegions().getFirst().getBlocks().size() != 1) {
          operation.emitError("Operation region must have exactly one block");
          return false;
        }

        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
        attributes.get(0).setAttribute(new StringAttribute("foo"));
        attributes.get(1).setAttribute(new TypeAttribute(new FuncType()));
      }
    }
    return new FuncOpModel();
  }

  public FuncOp() {
  }

  public FuncOp(Operation operation) {
    super(operation);
  }

  public FuncOp(String name) {
    this(name, new FuncType());
  }

  public FuncOp(String name, FuncType type) {
    setOperation(Operation.Create(getIdent(), null, null, type.getOutput(), type.getInputs()));
    getRegions().getFirst().getEntryBlock();
    getFuncNameAttribute().setValue(name);
    getTypeAttribute().setType(type);
  }

  public StringAttribute getFuncNameAttribute() {
    return getAttribute(StringAttribute.class, SymbolTable.getSymbolAttributeName());
  }

  public String getFuncName() {
    return getFuncNameAttribute().getValue();
  }

  public TypeAttribute GetTypeAttribute() {
    return getAttribute(TypeAttribute.class, "type");
  }

  public TypeAttribute getTypeAttribute() {
    return (TypeAttribute) getOperation().getAttributes().get("type").getAttribute();
  }

  public FuncType getType() {
    return (FuncType) getTypeAttribute().getStorage();
  }

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }
}
