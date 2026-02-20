package core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.OperationVerifier;
import core.Utils;
import core.detail.OperationDetails;
import core.detail.RegisteredOperationDetails;
import core.serialization.OperationDeserializer;
import core.serialization.OperationSerializer;
import core.traits.IOpTrait;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * This class represents the data state associated with each concrete implementation of an
 * operation. This structure is used so that operations can be constructed independently of their
 * behavior. This is especially useful for serialization and deserialization.
 */
@JsonSerialize(using = OperationSerializer.class)
@JsonDeserialize(using = OperationDeserializer.class)
public final class Operation implements Serializable {

  // =========================================================================
  // Static Factory
  // =========================================================================

  /**
   * Static factory method to create an Operation instance.
   *
   * @param name The name of the operation.
   * @param operands The input value operands.
   * @param successors The blocks that succeed this operation (branching).
   * @param outputType The output result type.
   * @return A new Operation instance.
   */
  @Contract(pure = true)
  @SafeVarargs
  public static Operation Create(
      @NotNull String name,
      @Nullable List<Value> operands,
      @Nullable List<Block> successors,
      @Nullable Type outputType,
      @NotNull List<Type>... regionBodyValueTypes) {
    assert RegisteredOperationDetails.lookup(name).isPresent()
        : MessageFormat.format(
            "OperationDetails not found for name: {0}\n Make sure it is registered in with the dialect.",
            name);
    return Create(
        RegisteredOperationDetails.lookup(name).orElseThrow(),
        operands,
        successors,
        outputType,
        regionBodyValueTypes);
  }

  @Contract(pure = true)
  @SafeVarargs
  public static Operation Create(
      @NotNull OperationDetails name,
      @Nullable List<Value> operands,
      @Nullable List<Block> successors,
      @Nullable Type outputType,
      @NotNull List<Type>... regionBodyValueTypes) {
    var operation = Create(name, operands, successors, outputType, regionBodyValueTypes.length);
    for (int i = 0; i < regionBodyValueTypes.length; i++) {
      operation
          .getRegions()
          .get(i)
          .setBodyValues(regionBodyValueTypes[i].stream().map(Value::new).toList());
    }
    return operation;
  }

  @Contract(pure = true)
  public static Operation Create(
      @NotNull String name,
      @Nullable List<Value> operands,
      @Nullable List<Block> successors,
      @Nullable Type outputType,
      int numRegions) {
    assert RegisteredOperationDetails.lookup(name).isPresent()
        : MessageFormat.format(
            "OperationDetails not found for name: {0}\n Make sure it is registered in with the dialect.",
            name);
    return Create(
        RegisteredOperationDetails.lookup(name).orElseThrow(),
        operands,
        successors,
        outputType,
        numRegions);
  }

  @Contract(pure = true)
  public static Operation Create(
      @NotNull OperationDetails name,
      @Nullable List<Value> operands,
      @Nullable List<Block> successors,
      @Nullable Type outputType,
      int numRegions) {
    operands = operands == null ? List.of() : operands;
    successors = successors == null ? List.of() : successors;

    List<NamedAttribute> attributes = new ArrayList<>(name.getAttributeNames().size());
    for (String attrName : name.getAttributeNames()) {
      attributes.add(new NamedAttribute(attrName, null));
    }
    name.populateDefaultAttrs(attributes);
    Map<String, NamedAttribute> attributeMap =
        attributes.stream()
            .collect(Collectors.toUnmodifiableMap(NamedAttribute::getName, attr -> attr));

    return new Operation(name, operands, successors, outputType, attributeMap, numRegions);
  }

  // =========================================================================
  // Members
  // =========================================================================

  /** The unique identifier of this operation. */
  @NotNull private final OperationDetails details;

  /** The input values of this operation. */
  @NotNull @Unmodifiable private final List<ValueOperand> operands;

  /** The input blocks of this operation (branch successors). */
  @NotNull @Unmodifiable private final List<BlockOperand> blockOperands;

  /** The output of this operation. */
  @Nullable private final OperationResult output;

  /** The attributes of this operation. */
  @NotNull @Unmodifiable private final Map<String, NamedAttribute> attributes;

  /** The regions of this operation. */
  @NotNull @Unmodifiable private final List<Region> regions;

  /** The block containing this operation. */
  @JsonIgnore @Nullable private Block parent = null;

  // =========================================================================
  // Constructors
  // =========================================================================

  /**
   * Full constructor for Operation.
   *
   * @param details The operation details.
   * @param operands The input values.
   * @param successors The blocks succeeding this operation.
   * @param resultType The output result type.
   * @param attributes The named attributes.
   * @param numRegions The number of regions.
   */
  public Operation(
      @NotNull OperationDetails details,
      @NotNull List<Value> operands,
      @NotNull List<Block> successors,
      @Nullable Type resultType,
      @NotNull Map<String, NamedAttribute> attributes,
      int numRegions) {
    this.details = details;

    this.output = resultType != null ? new OperationResult(this, resultType) : null;

    List<ValueOperand> operandsList = new ArrayList<>(operands.size());
    for (var operand : operands) operandsList.add(new ValueOperand(this, operand));
    this.operands = Collections.unmodifiableList(operandsList);

    List<BlockOperand> blockOperandsList = new ArrayList<>(successors.size());
    for (int i = 0; i < successors.size(); i++) {
      blockOperandsList.add(i, new BlockOperand(this, successors.get(i)));
    }
    this.blockOperands = Collections.unmodifiableList(blockOperandsList);

    this.attributes = Map.copyOf(attributes);

    var regionsList = new ArrayList<Region>(numRegions);
    for (int i = 0; i < numRegions; i++) {
      regionsList.add(new Region(this));
    }
    this.regions = Collections.unmodifiableList(regionsList);
  }

  // =========================================================================
  // Verification
  // =========================================================================

  @Contract(pure = true)
  public boolean verify(boolean recursive) {
    return new OperationVerifier(recursive).verify(this);
  }

  // =========================================================================
  // Details & Traits
  // =========================================================================

  @Contract(pure = true)
  public @NotNull OperationDetails getDetails() {
    return details;
  }

  @Contract(pure = true)
  public boolean hasTrait(@NotNull Class<? extends IOpTrait> traitClass) {
    return details.hasTrait(traitClass);
  }

  /**
   * Create a typed Op wrapper for this operation if it matches the given class.
   *
   * @param clazz The class of the op to wrap
   * @return The op wrapper, or empty if this operation is not of the given type.
   */
  @Contract(pure = true)
  public <T extends Op> @NotNull Optional<T> as(@NotNull Class<T> clazz) {
    return getDetails().as(clazz, this);
  }

  /**
   * Create a typed trait wrapper for this operation if it implements the given trait.
   *
   * @param clazz The trait to check for
   * @return The trait wrapper, or empty if this operation does not implement the trait.
   */
  @Contract(pure = true)
  public <T extends IOpTrait> @NotNull Optional<T> asTrait(@NotNull Class<T> clazz) {
    if (!hasTrait(clazz)) return Optional.empty();
    return Optional.of(clazz.cast(asOp()));
  }

  /**
   * Create a generic Op wrapper for this operation.
   *
   * @return The op wrapper.
   */
  @Contract(pure = true)
  public @NotNull Op asOp() {
    return getDetails().asOp(this);
  }

  /**
   * Check if this operation is of the given Op type.
   *
   * @param clazz The type to check for
   * @return true if this operation is of the given type, false otherwise.
   */
  @Contract(pure = true)
  public boolean isa(@NotNull Class<? extends Op> clazz) {
    return getDetails().isa(clazz);
  }

  // =========================================================================
  // Operands & Output
  // =========================================================================

  @Contract(pure = true)
  public @NotNull @Unmodifiable List<ValueOperand> getOperands() {
    return operands;
  }

  @Contract(pure = true)
  public @NotNull Optional<ValueOperand> getOperand(int index) {
    return operands.size() > index ? Optional.of(operands.get(index)) : Optional.empty();
  }

  @Contract(pure = true)
  public @NotNull @Unmodifiable List<BlockOperand> getBlockOperands() {
    return blockOperands;
  }

  /**
   * Get the successor blocks of this operation via its block operands.
   *
   * @return An unmodifiable list of successor blocks.
   */
  @JsonIgnore
  @Contract(pure = true)
  public @NotNull @Unmodifiable List<Block> getSuccessors() {
    return getBlockOperands().stream()
        .map(BlockOperand::getValue)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  @Contract(pure = true)
  public @NotNull Optional<OperationResult> getOutput() {
    return Optional.ofNullable(output);
  }

  @Contract(pure = true)
  public @NotNull Optional<Value> getOutputValue() {
    if (output == null) return Optional.empty();
    return getOutput().map(OperationResult::getValue);
  }

  public @NotNull Value getOutputValueThrowing() {
    return getOutput().map(OperationResult::getValue).orElseThrow();
  }

  public void setOutputValue(@NotNull Value value) {
    assert this.output != null : "Trying to set output value of an operation that has no output.";
    this.output.setValue(value);
  }

  // =========================================================================
  // Attributes
  // =========================================================================

  @Contract(pure = true)
  public @NotNull @Unmodifiable Map<String, NamedAttribute> getAttributes() {
    return attributes;
  }

  @Contract(pure = true)
  public @NotNull Optional<Attribute> getAttributeByName(@NotNull String name) {
    if (!getAttributes().containsKey(name)) return Optional.empty();
    return getAttributes().get(name).getAttribute();
  }

  @Contract(pure = true)
  public <T extends Attribute> @NotNull Optional<T> getAttribute(
      @NotNull Class<T> clazz, @NotNull String name) {
    var attribute = getAttributeByName(name);
    if (attribute.isEmpty() || !clazz.isInstance(attribute.get())) return Optional.empty();
    return Optional.of(clazz.cast(attribute.get()));
  }

  public void setAttribute(@NotNull String name, @NotNull Attribute attribute) {
    NamedAttribute namedAttribute = getAttributes().get(name);
    assert namedAttribute != null
        : MessageFormat.format("Attribute with name {0} does not exist.", name);
    namedAttribute.setAttribute(attribute);
  }

  // =========================================================================
  // Regions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull @Unmodifiable List<Region> getRegions() {
    return regions;
  }

  @Contract(pure = true)
  public @NotNull Optional<Region> getRegion(int index) {
    return regions.size() > index ? Optional.of(regions.get(index)) : Optional.empty();
  }

  @Contract(pure = true)
  public @NotNull Optional<Region> getFirstRegion() {
    return regions.isEmpty() ? Optional.empty() : Optional.of(regions.getFirst());
  }

  // =========================================================================
  // Parent & Navigation
  // =========================================================================

  @Contract(pure = true)
  public @NotNull Optional<Block> getParent() {
    return Optional.ofNullable(parent);
  }

  @Contract(pure = true)
  public @NotNull Optional<Region> getParentRegion() {
    return getParent().flatMap(Block::getParent);
  }

  @Contract(pure = true)
  public @NotNull Optional<Operation> getParentOperation() {
    return getParentRegion().flatMap(Region::getParent);
  }

  public void setParent(Block parent) {
    assert Utils.Caller.getCallingClass() == Block.class
        : MessageFormat.format(
            "Assigning the parent of an operation is only allowed from the Block class. Was called from {0}",
            Utils.Caller.getCallingClass().getName());
    assert this.parent == null || parent == null
        : "Operation already has a parent. Unparent first before setting a new parent. (Use the block interface to unparent.)";
    this.parent = parent;
  }

  /**
   * Walks the parent chain and returns the first parent operation that implements the given trait.
   *
   * @param traitClass The trait to search for.
   * @return The first parent operation implementing the trait, or empty if none was found.
   */
  @Contract(pure = true)
  public <T extends IOpTrait> @NotNull Optional<T> getParentWithTrait(
      @NotNull Class<T> traitClass) {
    Optional<Operation> currentParent = getParentOperation();
    if (currentParent.isEmpty()) return Optional.empty();

    while (currentParent.isPresent()) {
      Optional<T> asTrait = currentParent.get().asTrait(traitClass);
      if (asTrait.isPresent()) return asTrait;
      currentParent = currentParent.get().getParentOperation();
    }
    return Optional.empty();
  }

  /**
   * Get the index of this operation in its parent block's operations list.
   *
   * @return The index, or -1 if this operation has no parent.
   */
  @Contract(pure = true)
  public int getIndex() {
    return getParent().map(block -> block.getOperations().indexOf(this)).orElse(-1);
  }

  /**
   * Get the next operation in the same block as this operation.
   *
   * @return The next operation, or empty if there is none.
   */
  @Contract(pure = true)
  public @NotNull Optional<Operation> getNext() {
    return getParent()
        .map(
            block -> {
              int index = block.getOperations().indexOf(this);
              if (index == -1 || index == block.getOperations().size() - 1) return null;
              return block.getOperations().get(index + 1);
            });
  }

  // =========================================================================
  // Diagnostics
  // =========================================================================

  @Contract(pure = true)
  public void emitMessage(@NotNull String s) {
    System.out.println(MessageFormat.format("Message: {0}\n\t| {1}", this, s));
  }

  @Contract(pure = true)
  public void emitWarning(@NotNull String s) {
    System.out.println(MessageFormat.format("\u001B[33mWarning: {0}\n\t| {1}\u001B[0m", this, s));
  }

  @Contract(pure = true)
  public void emitError(@NotNull String s) {
    System.err.println(MessageFormat.format("Error: {0}\n\t| {1}", this, s));
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public @NotNull String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getDetails().getIdent());

    sb.append(" (");
    sb.append(
        operands.stream()
            .map(op -> op.getType().orElseThrow().getParameterizedIdent())
            .collect(Collectors.joining(", ")));
    sb.append(")");

    sb.append(" -> (");
    if (output != null) {
      sb.append(output.getValue().getType().getParameterizedIdent());
    }
    sb.append(")");

    if (!attributes.isEmpty()) {
      String attrs =
          attributes.values().stream()
              .filter(attr -> attr.getAttribute().isPresent())
              .map(
                  attr ->
                      MessageFormat.format(
                          "{0} = {1}", attr.getName(), attr.getAttribute().get().getStorage()))
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
          if (j < blocks.size() - 1) sb.append(", ");
        }
        sb.append(" }");
      }
    }

    return sb.toString();
  }
}
