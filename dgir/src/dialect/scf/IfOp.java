package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.IControlFlow;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Op which represents an if statement.
 * It has one region for the "then" block and optionally one region for the "else" block.
 */
public class IfOp extends Op implements IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class IfOpDetails extends OperationDetails.Impl {
      IfOpDetails() {
        super(IfOp.getIdent(), IfOp.class, Dialect.getOrThrow(SCF.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {
      }
    }
    return new IfOpDetails();
  }

  public static String getIdent() {
    return "scf.if";
  }

  public static String getNamespace() {
    return "scf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public IfOp() {
  }

  public IfOp(Operation operation) {
    super(operation);
  }

  public IfOp(Value condition, boolean withElseBlock) {
    super(Operation.Create(getIdent(), List.of(condition), null, null, withElseBlock ? 2 : 1));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public Region getThenRegion() {
    return getRegions().getFirst();
  }

  public Optional<Region> getElseRegion() {
    if (getRegions().size() == 1) return Optional.empty();
    return Optional.of(getRegions().get(1));
  }
}
