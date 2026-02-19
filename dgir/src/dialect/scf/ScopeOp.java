package dialect.scf;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.traits.IControlFlow;
import core.traits.ISingleRegion;

import java.util.List;

/**
 * Op which opens a new scope. The scope has no effect other than hiding new variables from the outside.
 */
public class ScopeOp extends Op implements ISingleRegion, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.Impl createDetails() {
    class ScopeOpDetails extends OperationDetails.Impl {
      ScopeOpDetails() {
        super(ScopeOp.getIdent(), ScopeOp.class, Dialect.get(SCF.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
      }
    }
    return new ScopeOpDetails();
  }

  public static String getIdent() {
    return "scf.scope";
  }

  public static String getNamespace() {
    return "scf";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ScopeOp() {
    executeIfRegistered(ScopeOp.class, () ->
      setOperation(true, Operation.Create(getIdent(), null, null, null, 1))
    );
  }

  public ScopeOp(Operation operation) {
    super(operation);
  }
}
