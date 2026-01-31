package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.OperationSerializer;
import blockly.vm.dgir.core.traits.IOpTrait;
import blockly.vm.dgir.core.traits.ISymbolTable;
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
   * @param numRegions The number of regions.
   * @return A new Operation instance.
   */
  public static Operation Create(String name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 int numRegions) {
    assert RegisteredOperationDetails.lookup(name).isPresent() : "OperationDetails not found for name: " + name + "\n Make sure it is registered in with the dialect.";
    return Create(RegisteredOperationDetails.lookup(name).orElseThrow(), operands, successors, outputType, numRegions);
  }

  public static Operation Create(OperationDetails name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 int numRegions) {
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

    return new Operation(name, operands, successors, outputType, attributeMap, numRegions);
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
   * @param numRegions The number of regions.
   */
  public Operation(OperationDetails details,
                   List<Value> operands,
                   List<Block> successors,
                   Type resultType,
                   Map<String, NamedAttribute> attributes,
                   int numRegions) {
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

    // Create an unmodifiable list of the regions
    var regions = new ArrayList<Region>(numRegions);
    for (int i = 0; i < numRegions; i++) {
      regions.add(new Region(this));
    }
    this.regions = Collections.unmodifiableList(regions);
  }

  // TODO implement verify
  public boolean verify(boolean recursive) {
    return false;
  }

  public OperationDetails getDetails() {
    return details;
  }

  public boolean hasTrait(Class<? extends IOpTrait> traitClass) {
    return details.hasTrait(traitClass);
  }

  /**
   * Create an instance of the op from the operation state.
   * Only returns a value if the operation is of type of op.
   *
   * @param clazz The class of the op to create
   * @return The op instance or null if the operation is not of the given type
   */
  public <T extends Op> T as(Class<T> clazz) {
    return getDetails().as(clazz, this);
  }

  /**
   * Create an instance of the op from the operation state if it implements the given trait.
   *
   * @param clazz The trait to check for
   * @param <T>   The trait type
   * @return The op instance or null if the operation does not implement the trait
   */
  public <T extends IOpTrait> T asTrait(Class<T> clazz) {
    if (hasTrait(clazz)) {
      return clazz.cast(as(getDetails().getType()));
    }
    return null;
  }

  /**
   * Create an instance of the op from the operation state.
   *
   * @return The op instance
   */
  public Op asOp() {
    return getDetails().asOp(this);
  }

  /**
   * Check if this operation is of the given type.
   *
   * @param clazz The type to check for
   * @return true if this operation is of the given type, false otherwise
   */
  public boolean isa(Class<? extends Op> clazz) {
    return getDetails().isa(clazz);
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

  public <T extends Attribute> T getAttribute(Class<T> clazz, String name) {
    return clazz.cast(getAttributes().get(name).getAttribute());
  }

  public List<Region> getRegions() {
    return regions;
  }

  public Region getFirstRegion() {
    return regions.getFirst();
  }

  public Block getParent() {
    return parent;
  }

  public Region getParentRegion() {
    if (getParent() == null) return null;
    return getParent().getParent();
  }

  public Operation getParentOperation() {
    if (getParent() == null) return null;
    return getParent().getParent().getParent();
  }

  public void setParent(Block parent) {
    assert Utils.Caller.getCallingClass() == Block.class : "Assigning the parent of an operation is only allowed from the Block class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || parent == null : "Operation already has a parent. Unparent first before setting a new parent. (Use the block interface to unparent.)";

    this.parent = parent;
  }

  /**
   * Walks though the parent structure and returns the first parent operation that implements the given trait.
   *
   * @param traitClass The trait to search for
   * @param <T>        The trait type
   * @return The first parent operation that implements the given trait or null if none was found.
   */
  public <T extends IOpTrait> T getParentWithTrait(Class<T> traitClass) {
    if (getParent() == null) return null;
    Operation currentParent = getParentOperation();
    while (currentParent != null) {
      if (currentParent.hasTrait(traitClass)) {
        return currentParent.asTrait(traitClass);
      }
      currentParent = currentParent.getParentOperation();
    }
    return null;
  }
}
