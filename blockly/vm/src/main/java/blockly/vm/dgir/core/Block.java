package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.io.Serial;
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
@JsonIdentityInfo(generator = BlockSequenceGenerator.class, property = "@id")
public final class Block implements Serializable {
  private List<BlockArgument> arguments;
  private final List<Operation> operations;

  @JsonBackReference
  public Region owner;

  public Block() {
    this.arguments = null;
    this.operations = new ArrayList<>();
  }

  @JsonCreator
  public Block(@JsonProperty("arguments") List<BlockArgument> arguments,
               @JsonProperty("operations") List<Operation> operations) {
    this.arguments = arguments;
    this.operations = operations;
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

  public List<Operation> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  public void addOperation(Operation op) {
    insertOperationAt(op, operations.size());
  }

  public void insertOperationAt(Operation op, int index) {
    op.removeFromBlock();
    operations.add(index, op);
    op.owner = this;
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
      op.owner = null;
  }

  public void removeOperationAt(int index) {
    var op = operations.remove(index);
    if (op != null)
      op.owner = null;
  }
}

/**
 * Generates unique IDs for block instances. This is used by Jackson to serialize/deserialize
 * e.g. ".blk_0" ".blk_1" ...
 */
final class BlockSequenceGenerator extends ObjectIdGenerator<String> {
  @Serial
  private static final long serialVersionUID = 1L;

  private transient int _nextIndex;

  private final Class<?> _scope;

  public BlockSequenceGenerator() {
    this(Object.class, -1);
  }

  private BlockSequenceGenerator(Class<?> scope, int fv) {
    _scope = scope;
  }

  @Override
  public final Class<?> getScope() {
    return _scope;
  }

  @Override
  public boolean canUseFor(ObjectIdGenerator<?> gen) {
    return (gen.getClass() == getClass()) && (gen.getScope() == _scope);
  }

  private int initialValue() {
    return 1;
  }

  @Override
  public ObjectIdGenerator<String> forScope(Class<?> scope) {
    return (_scope == scope) ? this : new BlockSequenceGenerator(scope, _nextIndex);
  }

  @Override
  public ObjectIdGenerator<String> newForSerialization(Object context) {
    return new BlockSequenceGenerator(_scope, initialValue());
  }

  @Override
  public IdKey key(Object key) {
    if (key == null) {
      return null;
    }
    return new IdKey(getClass(), _scope, key);
  }

  @Override
  public String generateId(Object forPojo) {
    if (forPojo == null) {
      return null;
    }
    return ".blk_" + _nextIndex++;
  }
}
