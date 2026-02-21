package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.IControlFlow;
import core.traits.ISingleRegion;
import dialect.builtin.types.IntegerT;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Op which represents a for loop. It has one region for the body of the loop. Its parameters are: -
 * initValue: the initial value of the induction variable - lowerBound: the lower bound of the loop
 * - upperBound: the upper bound of the loop - step: the step size of the loop
 */
public class ForOp extends Op implements IControlFlow, ISingleRegion {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class ForOpDetails extends OperationDetails.Impl {
      ForOpDetails() {
        super(ForOp.getIdent(), ForOp.class, Dialect.getOrThrow(SCF.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {}
    }
    return new ForOpDetails();
  }

  public static String getIdent() {
    return "scf.for";
  }

  public static String getNamespace() {
    return "scf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ForOp() {}

  public ForOp(Operation operation) {
    super(operation);
  }

  public ForOp(Value initValue, Value lowerBound, Value upperBound, Value step) {
    super(
        true,
        Operation.Create(
            getIdent(),
            List.of(initValue, lowerBound, upperBound, step),
            null,
            null,
            List.of(IntegerT.INT32)));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public Value getInductionValue() {
    return getRegion().getBodyValue(0).orElseThrow();
  }

  public Value getLowerBound() {
    return getRegion().getBodyValue(1).orElseThrow();
  }

  public Value getUpperBound() {
    return getRegion().getBodyValue(2).orElseThrow();
  }

  public Value getStep() {
    return getRegion().getBodyValue(3).orElseThrow();
  }
}
