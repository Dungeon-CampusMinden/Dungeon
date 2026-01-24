package blockly.vm.dgir.dialect.io;

import blockly.vm.dgir.core.*;

import java.util.List;


public class PrintOp extends Op {
  @Override
  public OperationName.Impl createImpl() {
    class PrintOpModel extends OperationName.Impl {
      PrintOpModel(String name, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
        super(name, type, dialect, attributeNames);
      }

      @Override
      public boolean verify(Operation operation) {
        return false;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {

      }
    }
    return new PrintOpModel(getIdent(), this.getClass(), Dialect.get(IO.class), List.of());
  }

  @Override
  public OperationName getName() {
    return null;
  }

  public PrintOp() {
  }

  public PrintOp(List<Value> operands) {
    super(Operation.Create(getIdent(), operands.stream().map(ValueOperand::new).toList(), null, null));
  }

  public static String getIdent() {
    return "io.print";
  }

  public static String getNamespace() {
    return "io";
  }
}
