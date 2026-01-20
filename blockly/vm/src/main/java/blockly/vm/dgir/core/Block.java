package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Block {
  public String ident;
  public List<Argument> arguments = new ArrayList<>();
  public List<Operation> operations = new ArrayList<>();

  @JsonIgnore
  public void seIdentUnique(String base) {
    ident = base + "_" + System.identityHashCode(this);
  }

  public void insertOperationBefore(Operation op, Operation before) {
    int index = operations.indexOf(before);
    operations.add(index, op);
  }

  public void insertOperationAfter(Operation op, Operation after) {
    int index = operations.indexOf(after) + 1;
    operations.add(index, op);
  }
}
