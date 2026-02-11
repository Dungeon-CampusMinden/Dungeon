package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.traits.INoTerminator;
import blockly.vm.dgir.core.traits.ISymbolTable;

import java.util.List;

public class ProgramOp extends Op implements ISymbolTable, INoTerminator {
  @Override
  public OperationDetails.Impl createDetails() {
    class ProgramOpModel extends OperationDetails.Impl {
      ProgramOpModel(String name, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
        super(name, type, dialect, attributeNames);
      }

      @Override
      public boolean verify(Operation operation) {
        // TODO This check still has to be implemented
        System.out.println("Missing verification for operation " + getIdent());
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {

      }
    }

    return new ProgramOpModel(getIdent(), this.getClass(), Dialect.get(Builtin.class), List.of());
  }

  public ProgramOp() {
  }

  public ProgramOp(Operation operation) {
    super(operation);
  }

  public ProgramOp(boolean withRegion) {
    if (withRegion) {
      setOperation(Operation.Create(getIdent(), null, null, null, 1));
    }
  }

  public static String getIdent() {
    return "program";
  }


  public static String getNamespace() {
    return "";
  }
}
