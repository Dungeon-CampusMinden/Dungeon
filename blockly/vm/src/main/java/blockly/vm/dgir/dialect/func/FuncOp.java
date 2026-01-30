package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.TypeAttribute;
import blockly.vm.dgir.dialect.func.types.FuncType;

import java.util.List;

public class FuncOp extends Op {
  @Override
  public OperationDetails.Impl createDetails() {
    class FuncOpModel extends OperationDetails.Impl {
      FuncOpModel() {
        super(FuncOp.getIdent(), FuncOp.class, DGIRContext.registeredDialects.get(Func.class), List.of("name", "type"));
      }

      @Override
      public boolean verify(Operation operation) {
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

  FuncOp() {
  }

  public FuncOp(String name) {
    setOperation(Operation.Create(getIdent(), null, null, null, List.of(Region.createWithBlock())));
    setFuncName(name);
  }

  public FuncOp(String name, FuncType type) {
    setOperation(Operation.Create(getIdent(), null, null, null, List.of(Region.createWithBlock())));
    setFuncName(name);
  }

  public StringAttribute getFuncNameAttribute() {
    return (StringAttribute) getOperation().getAttributes().get("name").getAttribute();
  }

  public String getFuncName() {
    return getFuncNameAttribute().getValue();
  }

  public void setFuncName(String newName) {
    getFuncNameAttribute().setValue(newName);
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
