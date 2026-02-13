package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.Op;
import core.ir.Operation;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;

import java.util.List;

/**
 * Marks the end of a structured control flow region.
 */
public class ContinueOp extends Op implements ITerminator, ISpecificParentOp {
  @Override
  public OperationDetails.Impl createDetails() {
    class ContinueOpDetails extends OperationDetails.Impl {
      ContinueOpDetails() {
        super(ContinueOp.getIdent(), ContinueOp.class, Dialect.get(SCF.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<core.ir.NamedAttribute> attributes) {
      }
    }
    return new ContinueOpDetails();
  }

  public static String getIdent() {
    return "scf.continue";
  }

  public static String getNamespace() {
    return "scf";
  }

  public ContinueOp() {
    executeIfRegistered(ContinueOp.class, () ->
      setOperation(true, Operation.Create(getIdent(), null, null, null)));
  }

  public ContinueOp(Operation operation) {
    super(operation);
  }

  @Override
  public List<Class<? extends Op>> getValidParentTypes() {
    return List.of(IfOp.class, ScopeOp.class, ForOp.class);
  }
}
