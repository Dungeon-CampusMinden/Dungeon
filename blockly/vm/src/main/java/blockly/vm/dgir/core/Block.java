package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.util.StdConverter;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
// JsonDeserialize is run after the full deserialization and used to update the references in the children
@JsonDeserialize(converter = BlockConverter.class)
public final class Block {
  public String ident;
  public List<Argument> arguments = new ArrayList<>();
  public List<Operation> operations = new ArrayList<>();

  @JsonIgnore
  public Region parent;

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

/**
 * Used to update references post deserialization.
 */
class BlockConverter extends StdConverter<Block, Block> {
  @Override
  public Block convert(Block value) {
    for (var op : value.operations) {
      op.setParent(value);
    }
    return value;
  }
}
