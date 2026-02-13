package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.IControlFlow;

import java.util.List;
import java.util.Optional;

/**
 * Op which represents an if statement. It has one region for the "then" block and optionally one region for the "else" block.
 */
public class IfOp extends Op implements IControlFlow {
  @Override
  public OperationDetails.Impl createDetails() {
    class IfOpDetails extends OperationDetails.Impl {
      IfOpDetails() {
        super(IfOp.getIdent(), IfOp.class, Dialect.get(SCF.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
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


  public IfOp() {
  }

  public IfOp(Operation operation) {
    super(operation);
  }

  public IfOp(Value condition, boolean withElseBlock) {
    super(Operation.Create(getIdent(), List.of(condition), null, null, withElseBlock ? 2 : 1));
  }

  public Region getThenRegion() {
    return getRegions().getFirst();
  }

  public Optional<Region> getElseRegion() {
    if (getRegions().size() == 1) return Optional.empty();
    return Optional.of(getRegions().get(1));
  }
}
