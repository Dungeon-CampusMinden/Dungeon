package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.*;

import javax.swing.*;
import java.util.List;

public class ProgramOp extends Op {
  @Override
  public OperationName.Impl createImpl() {
    class ProgramOpModel extends OperationName.Impl {
      ProgramOpModel(String name, Class<? extends Op> type, Dialect dialect, String[] attributeNames) {
        super(name, type, dialect, attributeNames);
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(NamedAttribute[] attributes) {
      }
    }

    return new ProgramOpModel(getIdent(), this.getClass(), DGIRContext.registeredDialects.get(Builtin.class), new String[]{});
  }

  @Override
  public OperationName getName() {
    return getOperation().getName();
  }

  ProgramOp() {}

  public ProgramOp(boolean withRegion){
    if(withRegion) {
      setOperation(Operation.Create(getIdent(), null, null, null, List.of(Region.createWithBlock())));
    }
  }

  public static ProgramOp create() {
    var programOp = new ProgramOp();
    programOp.setOperation(Operation.Create(getIdent(), null, null, null, List.of(Region.createWithBlock())));
    return programOp;
  }


  public static String getIdent() {
    return "program";
  }


  public static String getNamespace() {
    return "";
  }
}
