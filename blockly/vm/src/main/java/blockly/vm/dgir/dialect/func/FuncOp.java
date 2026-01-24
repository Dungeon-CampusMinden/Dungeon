package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.TypeAttribute;
import blockly.vm.dgir.dialect.func.types.FuncType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class FuncOp extends Op {
  @JsonIgnore
  private StringAttribute name;
  @JsonIgnore
  private TypeAttribute type;

  @Override
  public OperationName.Impl createImpl() {
    class FuncOpModel extends OperationName.Impl {
      FuncOpModel() {
        super(getIdent(), FuncOp.class, DGIRContext.registeredDialects.get(Func.class), new String[]{"name", "type"});
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(NamedAttribute[] attributes) {
        if (attributes[0].getAttribute() == null)
          attributes[0].setAttribute(new StringAttribute("foo"));
        if (attributes[1].getAttribute() == null)
          attributes[1].setAttribute(new TypeAttribute(new FuncType(null, null)));
      }
    }
    return new FuncOpModel();
  }

  FuncOp() {
  }

  public FuncOp(String name) {
    super(Operation.Create(getIdent(), null, null, null, List.of(Region.createWithBlock())));
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
