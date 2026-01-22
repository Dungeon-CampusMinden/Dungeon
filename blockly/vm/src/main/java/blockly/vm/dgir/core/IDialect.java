package blockly.vm.dgir.core;

import java.util.List;

public interface IDialect {
  String getNamespace();
  List<Class<? extends Operation>> AllOperations();
  List<Class<? extends Type>> AllTypes();
  List<Class<? extends Attribute>> AllAttributes();
}
