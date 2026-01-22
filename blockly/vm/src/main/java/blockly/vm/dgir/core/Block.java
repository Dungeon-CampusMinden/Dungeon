package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.util.StdConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
// JsonDeserialize is run after the full deserialization and used to update the references in the children
@JsonDeserialize(converter = BlockConverter.class)
public final class Block implements Serializable {
  private List<Argument> arguments;
  private final List<Operation> operations;

  private Region parent;

  public Block() {
    this.arguments = null;
    this.operations = new ArrayList<>();
  }

  @JsonCreator
  public Block(@JsonProperty("name") String name,
               @JsonProperty("arguments") List<Argument> arguments,
               @JsonProperty("operations") List<Operation> operations) {
    this.arguments = arguments;
    this.operations = operations;
  }

  @JsonIgnore
  public Region getParent() {
    return parent;
  }

  public void setParent(Region parent) {
    this.parent = parent;
  }

  @JsonIgnore
  public Optional<List<Argument>> getArguments() {
    return Optional.ofNullable(arguments);
  }

  @JsonIgnore
  public List<Argument> getOrCreateArguments() {
    return arguments == null ? arguments = new ArrayList<>() : arguments;
  }

  @JsonProperty("arguments")
  private List<Argument> getArgumentsRaw() {
    return arguments;
  }

  public List<Operation> getOperations() {
    return operations;
  }

  public void addOperation(Operation op) {
    operations.add(op);
  }

  public void setOperations(List<Operation> operations) {
    this.operations.clear();
    this.operations.addAll(operations);
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
    for (var op : value.getOperations()) {
      op.setParent(value);
    }
    return value;
  }
}
