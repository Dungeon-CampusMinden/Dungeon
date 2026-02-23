package dialect.func;

import core.SymbolTable;
import core.ir.Operation;
import core.traits.*;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.attributes.TypeAttribute;
import dialect.func.types.FuncType;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class FuncOp extends FuncBaseOp implements Func, ISymbol, IIsolatedFromAbove, IGlobal, ISingleRegion {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "func.func";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  @Override
  public @NotNull java.util.List<core.ir.NamedAttribute> getDefaultAttributes() {
    return java.util.List.of(
        new core.ir.NamedAttribute(SymbolTable.getSymbolAttributeName(), new StringAttribute("foo")),
        new core.ir.NamedAttribute("type", new TypeAttribute(new FuncType())));
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
    setOperation(true, Operation.Create(this, null, null, type.getOutput(), type.getInputs()));
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
