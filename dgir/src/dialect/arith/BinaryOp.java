package dialect.arith;

import core.debug.Location;
import core.ir.*;
import dialect.arith.attributes.BinModeAttr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

/**
 * Unified binary numeric operation for the {@code arith} dialect.
 *
 * <p>MLIR reference: {@code arith.bin}
 */
public final class BinaryOp extends BinaryNumericResultOp implements Arith {

  @Override
  public @NotNull String getIdent() {
    return "arith.bin";
  }

  @Override
  public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
    return List.of(new NamedAttribute("binMode", new BinModeAttr(BinModeAttr.BinMode.ADD)));
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      if (!super.getVerifier().apply(operation)) {
        return false;
      }
      if (operation.getAttribute(BinModeAttr.class, "binMode").isEmpty()) {
        operation.emitError("Binary operation must define a binMode attribute");
        return false;
      }
      return true;
    };
  }

  private BinaryOp() {}

  /**
   * Create a binary op with two numeric operands.
   *
   * @param loc the source location of this operation.
   * @param lhs the left-hand operand.
   * @param rhs the right-hand operand.
   * @param mode the binary operation kind.
   */
  public BinaryOp(
      @NotNull Location loc,
      @NotNull Value lhs,
      @NotNull Value rhs,
      @NotNull BinModeAttr.BinMode mode) {
    setOperation(
        Operation.Create(loc, this, List.of(lhs, rhs), null, getDominantType(lhs.getType(), rhs.getType())));
    setAttribute("binMode", new BinModeAttr(mode));
  }

  public @NotNull BinModeAttr.BinMode getBinMode() {
    return getAttribute("binMode", BinModeAttr.class)
        .map(BinModeAttr::getMode)
        .orElseThrow(() -> new AssertionError("No binMode attribute found."));
  }
}

