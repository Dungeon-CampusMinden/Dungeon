package dialect.arith;

import core.ir.Location;
import core.ir.NamedAttribute;
import core.ir.Operation;
import core.ir.Value;
import core.traits.IHasResult;
import dialect.arith.attributes.CompModeAttr;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

public final class CompareOp extends BinaryNumericOp implements Arith, IHasResult {
  @Override
  public @NotNull String getIdent() {
    return "arith.comp";
  }

  @Override
  public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
    return List.of(new NamedAttribute("compMode", new CompModeAttr(CompModeAttr.CompMode.EQ)));
  }

  private CompareOp() {}

  public CompareOp(Operation op) {
    super(op);
  }

  public CompareOp(
      @NotNull Location loc,
      @NotNull Value lhs,
      @NotNull Value rhs,
      @NotNull CompModeAttr.CompMode mode) {
    setOperation(Operation.Create(loc, this, List.of(lhs, rhs), null, IntegerT.BOOL));
    setAttribute("compMode", new CompModeAttr(mode));
  }

  public @NotNull CompModeAttr.CompMode getCompMode() {
    return getAttribute("compMode", CompModeAttr.class)
        .map(CompModeAttr::getMode)
        .orElseThrow(() -> new AssertionError("No compMode attribute found."));
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      CompareOp compareOp = operation.as(CompareOp.class).orElseThrow();
      if (!verifyBinaryNumericOperands(operation)) {
        return false;
      }
      if (!compareOp.getResultType().equals(IntegerT.BOOL)) {
        operation.emitError("Compare result type must be bool");
        return false;
      }
      return true;
    };
  }
}
