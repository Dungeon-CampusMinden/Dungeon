package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.OperationSerializer;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import tools.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the data state associated with each concrete implementation of an
 * operation. This structure is used so that operations can be constructed independently
 * of their behavior. This is especially useful for serialization and deserialization.
 */
@JsonSerialize(using = OperationSerializer.class)
public final class Operation implements Serializable {
  /**
   * The unique identifier of this operation.
   */
  private final OperationDetails details;

  /**
   * The input values of this operation.
   */
  @JsonManagedReference
  private final List<ValueOperand> operands = new ArrayList<>();

  /**
   * The input blocks of this operation.
   */
  @JsonManagedReference
  private final List<BlockOperand> blockOperands;

  /**
   * The output of this operation.
   */
  @JsonManagedReference
  private OperationResult output;

  /**
   * The attributes of this operation.
   */
  @JsonManagedReference
  private final Map<String, NamedAttribute> attributes;

  /**
   * The regions of this operation.
   */
  @JsonManagedReference
  private final List<Region> regions;

  /**
   * The block containing this operation.
   */
  @JsonBackReference
  private Block parent = null;

  /**
   * Static factory method to create an Operation instance.
   *
   * @param name       The name of the operation.
   * @param operands   The input value operands.
   * @param successors The blocks that succeed this operation (branching).
   * @param outputType The output result type.
   * @param regions    The regions.
   * @return A new Operation instance.
   */
  public static Operation Create(String name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 List<Region> regions) {
    assert RegisteredOperationDetails.lookup(name).isPresent() : "OperationDetails not found for name: " + name + "\n Make sure it is registered in with the dialect.";
    return Create(RegisteredOperationDetails.lookup(name).orElseThrow(), operands, successors, outputType, regions);
  }

  public static Operation Create(OperationDetails name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 List<Region> regions) {
    assert name != null : "OperationDetails name cannot be null.";

    // Ensure operands is a valid arraylist
    operands = operands == null ? new ArrayList<>() : operands;

    // Ensure block operands is a valid arraylist
    successors = successors == null ? new ArrayList<>() : successors;

    // Create the default attributes
    List<NamedAttribute> attributes = new ArrayList<>(name.getAttributeNames().size());
    for (String attrName : name.getAttributeNames()) {
      attributes.add(new NamedAttribute(attrName, null));
    }
    name.populateDefaultAttrs(attributes);
    // Create the attribute map
    Map<String, NamedAttribute> attributeMap = attributes.stream().collect(Collectors.toUnmodifiableMap(NamedAttribute::getName, attr -> attr));

    // Ensure regions is a valid arraylist
    regions = regions == null ? new ArrayList<>() : new ArrayList<>(regions);
    return new Operation(name, operands, successors, outputType, attributeMap, regions);
  }

  /**
   * Full constructor for Operation.
   * This constructor is not intended to be called directly. Use the static Create method instead as it will handle default values.
   *
   * @param details    The operation details.
   * @param operands   The input values.
   * @param successors The blocks succeeding this operation.
   * @param resultType The output result type.
   * @param attributes The named attributes.
   * @param regions    The regions.
   */
  public Operation(OperationDetails details,
                   List<Value> operands,
                   List<Block> successors,
                   Type resultType,
                   Map<String, NamedAttribute> attributes,
                   List<Region> regions) {
    this.details = details;

    if (resultType != null) {
      this.output = new OperationResult(this, resultType);
    }

    for (var operand : operands)
      addOperand(operand);

    // Populate a new list for block operands and make it unmodifiable
    List<BlockOperand> blockOperands = new ArrayList<>(successors.size());
    for (int i = 0; i < successors.size(); i++) {
      blockOperands.set(i, new BlockOperand(this, successors.get(i)));
    }
    this.blockOperands = Collections.unmodifiableList(blockOperands);

    // Create an unmodifiable map for the attributes
    this.attributes = Map.copyOf(attributes);

    // Create an unmodifiable list for the regions
    this.regions = List.copyOf(regions);
  }

  public boolean verify(boolean recursive) {
    return false;
  }

  public OperationDetails getDetails() {
    return details;
  }

  public List<ValueOperand> getOperands() {
    return Collections.unmodifiableList(operands);
  }

  public void addOperand(Value value) {
    assert value != null : "Operand cannot be null.";

    operands.add(new ValueOperand(this, value));
  }

  public List<BlockOperand> getBlockOperands() {
    return blockOperands;
  }

  public OperationResult getOutput() {
    return output;
  }

  public Map<String, NamedAttribute> getAttributes() {
    return attributes;
  }

  public List<Region> getRegions() {
    return regions;
  }

  public Block getParent() {
    return parent;
  }

  public void setParent(Block parent) {
    assert Utils.Caller.getCallingClass() == Block.class : "Assigning the parent of an operation is only allowed from the Block class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || parent == null : "Operation already has a parent. Unparent first before setting a new parent. (Use the block interface to unparent.)";

    this.parent = parent;
  }
}
