package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.TypeAttribute;
import blockly.vm.dgir.dialect.func.types.FuncType;

import java.util.List;

public class FuncOp extends Op {
  private StringAttribute name;
  private TypeAttribute type;

  @Override
  public OperationName.Impl createImpl() {
    class FuncOpModel extends OperationName.Impl {
      FuncOpModel() {
        super(getIdent(), FuncOp.class, DGIRContext.registeredDialects.get(Func.class), List.of("name", "type"));
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
          attributes.get(0).setAttribute(new StringAttribute("foo"));
          attributes.get(1).setAttribute(new TypeAttribute(new FuncType(null, null)));
      }
    }
    return new FuncOpModel();
  }

  FuncOp() {
  }

  public FuncOp(String name) {
    super(Operation.Create(getIdent(), null, null, List.of(Region.createWithBlock())));
    setFuncName(name);
  }

  public StringAttribute getFuncNameAttribute() {
    if (name == null)
      name = (StringAttribute) (getOperation().getAttributes().get("name").getAttribute());
    return name;
  }

  public String getFuncName() {
    return getFuncNameAttribute().getValue();
  }

  public void setFuncName(String newName) {
    getFuncNameAttribute().setValue(newName);
  }

  public TypeAttribute getTypeAttribute() {
    if (type == null)
      type = (TypeAttribute) (getOperation().getAttributes().get("type").getAttribute());
    return type;
  }

  public FuncType getType() {
    return (FuncType) getTypeAttribute().getType();
  }

  public void setType(FuncType type) {
    getTypeAttribute().setType(type);
  }

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }
}
