package blockly.vm.dgir.core.ir;

import blockly.vm.dgir.core.OperationVerifier;
import blockly.vm.dgir.core.Utils;
import blockly.vm.dgir.core.detail.OperationDetails;
import blockly.vm.dgir.core.detail.RegisteredOperationDetails;
import blockly.vm.dgir.core.serialization.OperationDeserializer;
import blockly.vm.dgir.core.serialization.OperationSerializer;
import blockly.vm.dgir.core.traits.IOpTrait;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the data state associated with each concrete implementation of an
 * operation. This structure is used so that operations can be constructed independently
 * of their behavior. This is especially useful for serialization and deserialization.
 */
@JsonSerialize(using = OperationSerializer.class)
@JsonDeserialize(using = OperationDeserializer.class)
public final class Operation implements Serializable {
  /**
   * The unique identifier of this operation.
   */
  private final OperationDetails details;

  /**
   * The input values of this operation.
   */
  private final List<ValueOperand> operands;

  /**
   * The input blocks of this operation.
   */
  private final List<BlockOperand> blockOperands;

  /**
   * The output of this operation.
   */
  private OperationResult output;

  /**
   * The attributes of this operation.
   */
  private final Map<String, NamedAttribute> attributes;

  /**
   * The regions of this operation.
   */
  private final List<Region> regions;

  /**
   * The block containing this operation.
   */
  @JsonIgnore
  private Block parent = null;

  /**
   * Static factory method to create an Operation instance.
   *
   * @param name       The name of the operation.
   * @param operands   The input value operands.
   * @param successors The blocks that succeed this operation (branching).
   * @param outputType The output result type.
   * @return A new Operation instance.
   */
  @SafeVarargs
  public static Operation Create(String name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 List<Type>... regionBodyValueTypes) {
    assert RegisteredOperationDetails.lookup(name).isPresent() : MessageFormat.format("OperationDetails not found for name: {0}\n Make sure it is registered in with the dialect.", name);
    return Create(RegisteredOperationDetails.lookup(name).orElseThrow(), operands, successors, outputType, regionBodyValueTypes);
  }

  @SafeVarargs
  public static Operation Create(OperationDetails name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 List<Type>... regionBodyValueTypes) {
    var operation = Create(name, operands, successors, outputType, regionBodyValueTypes.length);
    // Populate the region body value types
    for (int i = 0; i < regionBodyValueTypes.length; i++) {
      operation.getRegions().get(i).setBodyValues(regionBodyValueTypes[i].stream().map(Value::new).toList());
    }
    return operation;
  }

  public static Operation Create(String name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 int numRegions) {
    assert RegisteredOperationDetails.lookup(name).isPresent() : MessageFormat.format("OperationDetails not found for name: {0}\n Make sure it is registered in with the dialect.", name);
    return Create(RegisteredOperationDetails.lookup(name).orElseThrow(), operands, successors, outputType, numRegions);
  }

  public static Operation Create(OperationDetails name,
                                 List<Value> operands,
                                 List<Block> successors,
                                 Type outputType,
                                 int numRegions) {
    assert name != null : "OperationDetails name cannot be null.";

    // Ensure operands is a valid list
    operands = operands == null ? List.of() : operands;

    // Ensure block operands is a valid arraylist
    successors = successors == null ? List.of() : successors;

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
   *
   * @param details    The operation details.
   * @param operands   The input values.
   * @param successors The blocks succeeding this operation.
   * @param resultType The output result type.
   * @param attributes The named attributes.
   * @param numRegions The number of regions and their body value types.
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

    List<ValueOperand> operandsList = new ArrayList<>(operands.size());
    for (var operand : operands)
      operandsList.add(new ValueOperand(this, operand));
    this.operands = Collections.unmodifiableList(operandsList);


    // Populate a new list for block operands and make it unmodifiable
    List<BlockOperand> blockOperands = new ArrayList<>(successors.size());
    for (int i = 0; i < successors.size(); i++) {
      blockOperands.add(i, new BlockOperand(this, successors.get(i)));
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

  public boolean verify(boolean recursive) {
    return new OperationVerifier(recursive).verify(this);
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

  public List<BlockOperand> getBlockOperands() {
    return blockOperands;
  }

  public OperationResult getOutput() {
    return output;
  }

  public Value getOutputValue() {
    assert getOutput() != null : "Operation has no output.";
    return getOutput().getValue();
  }

  public void setOutputValue(Value value) {
    assert this.output != null : "Trying to set output value of an operation that has no output.";
    this.output.setValue(value);
  }

  public Map<String, NamedAttribute> getAttributes() {
    return attributes;
  }

  public Attribute getAttributeByName(String name) {
    return getAttributes().get(name).getAttribute();
  }

  public <T extends Attribute> T getAttribute(Class<T> clazz, String name) {
    return clazz.cast(getAttributeByName(name));
  }

  public void setAttribute(String name, Attribute attribute) {
    NamedAttribute namedAttribute = getAttributes().get(name);
    assert namedAttribute != null : MessageFormat.format("Attribute with name {0} does not exist.", name);
    namedAttribute.setAttribute(attribute);
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
    assert Utils.Caller.getCallingClass() == Block.class : MessageFormat.format("Assigning the parent of an operation is only allowed from the Block class. Was called from {0}", Utils.Caller.getCallingClass().getName());
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

  /**
   * Get the successor blocks of this operation by mapping the block operands to their values.
   *
   * @return The successor blocks of this operation.
   */
  @JsonIgnore
  public List<Block> getSuccessors() {
    return getBlockOperands().stream().map(BlockOperand::getValue).toList();
  }

  public void emitMessage(String s) {
    System.out.println(MessageFormat.format("Message: {0}\n\t| {1}", this, s));
  }

  public void emitWarning(String s) {
    // Yellow color for warning
    System.out.println(MessageFormat.format("\u001B[33mWarning: {0}\n\t| {1}\u001B[0m", this, s));
  }

  // Emit an error with the given message and information about this operation.
  public void emitError(String s) {
    System.err.println(MessageFormat.format("Error: {0}\n\t| {1}", this, s));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getDetails().getIdent());

    sb.append(" (");
    sb.append(operands.stream()
      .map(op -> op.getType().getParameterizedIdent())
      .collect(Collectors.joining(", ")));
    sb.append(")");

    sb.append(" -> (");
    if (output != null) {
      sb.append(output.getValue().getType().getParameterizedIdent());
    }
    sb.append(")");

    if (!attributes.isEmpty()) {
      String attrs = attributes.values().stream()
        .filter(attr -> attr.getAttribute() != null)
        .map(attr -> MessageFormat.format("{0} = {1}", attr.getName(), attr.getAttribute().getStorage()))
        .collect(Collectors.joining(", "));

      if (!attrs.isEmpty()) {
        sb.append(" { ");
        sb.append(attrs);
        sb.append(" }");
      }
    }

    if (!regions.isEmpty()) {
      for (Region region : regions) {
        sb.append(" { ");
        List<Block> blocks = region.getBlocks();
        for (int j = 0; j < blocks.size(); j++) {
          Block block = blocks.get(j);
          sb.append("[").append(block.getOperations().size()).append("]");
          if (j < blocks.size() - 1) {
            sb.append(", ");
          }
        }
        sb.append(" }");
      }
    }

    return sb.toString();
  }

  /**
   * Get the index of this operation in its parent's operations list.
   * @return The index of this operation in its parent's operations list.
   */
  public int getIndex() {
    if (getParent() == null) return -1;
    return getParent().getOperations().indexOf(this);
  }
}
