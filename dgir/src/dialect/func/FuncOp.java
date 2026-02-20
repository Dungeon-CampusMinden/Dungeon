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
import org.jetbrains.annotations.NotNull;

public class FuncOp extends Op implements ISymbol, IIsolatedFromAbove, IGlobal, ISingleRegion {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class FuncOpModel extends OperationDetails.Impl {
      FuncOpModel() {
        super(
            FuncOp.getIdent(),
            FuncOp.class,
            DGIRContext.registeredDialects.get(Func.class),
            List.of(SymbolTable.getSymbolAttributeName(), "type"));
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {
        attributes.get(0).setAttribute(new StringAttribute("foo"));
        attributes.get(1).setAttribute(new TypeAttribute(new FuncType()));
      }
    }
    return new FuncOpModel();
  }

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public FuncOp() {}

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

  // =========================================================================
  // Functions
  // =========================================================================

  public StringAttribute getFuncNameAttribute() {
    return getAttribute(StringAttribute.class, SymbolTable.getSymbolAttributeName())
        .orElseThrow(() -> new RuntimeException("Symbol attribute not found"));
  }

  public String getFuncName() {
    return getFuncNameAttribute().getValue();
  }

  public TypeAttribute getTypeAttribute() {
    return getOperation()
        .getAttribute(TypeAttribute.class, "type")
        .orElseThrow(() -> new RuntimeException("Type attribute not found"));
  }

  public FuncType getType() {
    return (FuncType) getTypeAttribute().getStorage();
  }
}
