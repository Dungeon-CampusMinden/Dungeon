package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.util.StdConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A block containing a list of {@link Operation}.
 * Blocks are always attached to a {@link Region}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
// JsonDeserialize is run after the full deserialization and used to update the references in the children
@JsonDeserialize(converter = BlockConverter.class)
public final class Block implements Serializable {
  private List<BlockArgument> arguments;
  private final List<Operation> operations;

  private Region parent;

  public Block() {
    this.arguments = null;
    this.operations = new ArrayList<>();
  }

  @JsonCreator
  public Block(@JsonProperty("name") String name,
               @JsonProperty("arguments") List<BlockArgument> arguments,
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
  public Optional<List<BlockArgument>> getArguments() {
    return Optional.ofNullable(arguments);
  }

  @JsonIgnore
  public List<BlockArgument> getOrCreateArguments() {
    return arguments == null ? arguments = new ArrayList<>() : arguments;
  }

  @JsonProperty("arguments")
  private List<BlockArgument> getArgumentsRaw() {
    return arguments;
  }

  public void setOperations(List<Operation> operations) {
    for (var op : this.operations)
      op.setParent(null);
    this.operations.clear();

    // Make sure the operation is not attached to a block anymore
    for (var op : operations)
      op.removeFromBlock();

    this.operations.addAll(operations);
    for (var op : this.operations)
      op.setParent(this);
  }

  public List<Operation> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  public void addOperation(Operation op) {
    insertOperationAt(op, operations.size());
  }

  public void insertOperationAt(Operation op, int index) {
    op.removeFromBlock();
    operations.add(index, op);
    op.setParent(this);
  }

  public void insertOperationBefore(Operation op, Operation before) {
    insertOperationAt(op, operations.indexOf(before));
  }

  public void insertOperationAfter(Operation op, Operation after) {
    insertOperationAt(op, operations.indexOf(after) + 1);
  }

  public void removeOperation(Operation op) {
    var result = operations.remove(op);
    if (result)
      op.setParent(null);
  }

  public void removeOperationAt(int index) {
    var op = operations.remove(index);
    if (op != null)
      op.setParent(null);
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
