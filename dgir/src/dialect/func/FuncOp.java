package dialect.func;

import core.*;
import core.detail.OperationDetails;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.traits.*;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.attributes.TypeAttribute;
import dialect.func.types.FuncType;

import java.util.List;

public class FuncOp extends Op implements ISymbol, IIsolatedFromAbove, IGlobal, ISingleRegion {
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
    super(true, Operation.Create(getIdent(), null, null, type.getOutput(), type.getInputs()));
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
