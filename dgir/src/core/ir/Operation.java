package core.ir;

import core.OperationVerifier;
import core.Utils;
import core.debug.Location;
import core.serialization.OperationDeserializer;
import core.serialization.OperationSerializer;
import core.traits.IOpTrait;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Carries the runtime state associated with a concrete operation instance.
 *
 * <p>The design deliberately separates <em>data</em> (this class) from <em>behaviour</em> (the
 * {@link Op} subclass hierarchy). This decoupling makes serialisation and deserialisation
 * straightforward: an {@code Operation} can be created from JSON without instantiating any
 * dialect-specific {@code Op} class, and the {@code Op} wrapper can be reconstructed on demand via
 * {@link #asOp()}.
 *
 * <p>An {@code Operation} is always created through one of the {@link #Create} static factory
 * methods; direct constructor calls are for deserialisation only.
 */
@JsonSerialize(using = OperationSerializer.class)
@JsonDeserialize(using = OperationDeserializer.class)
public final class Operation implements Serializable {

  // =========================================================================
  // Static Factory
  // =========================================================================

  /**
   * Create an {@link Operation} and populate body values for each of its regions.
   *
   * <p>Each element of {@code regionBodyValueTypes} corresponds to one region in declaration order.
   * The values are created as fresh {@link Value} instances typed according to the provided lists
   * and set as the region's body values after creation.
   *
   * @param location the source location of the operation.
   * @param op a default (no-arg) op prototype used to obtain the ident and default attributes.
   * @param operands input value operands, or {@code null} for none.
   * @param successors successor blocks (for branching ops), or {@code null} for none.
   * @param outputType result type, or {@code null} for void ops.
   * @param regionBodyValueTypes per-region lists of body value types; the number of elements
   * @return the newly constructed operation.
   */
  @Contract(pure = true)
  @NotNull
  @SafeVarargs
  public static Operation Create(
      @NotNull Location location,
      @NotNull Op op,
      @Nullable List<Value> operands,
      @Nullable List<Block> successors,
      @Nullable Type outputType,
      @NotNull List<Type>... regionBodyValueTypes) {
    Operation operation =
        Create(location, op, operands, successors, outputType, regionBodyValueTypes.length);
    for (int i = 0; i < regionBodyValueTypes.length; i++) {
      operation
          .getRegions()
          .get(i)
          .setBodyValues(regionBodyValueTypes[i].stream().map(Value::new).toList());
    }
    return operation;
  }

  /**
   * Create an {@link Operation} with a fixed number of empty regions.
   *
   * @param location the source location of the operation.
   * @param op a default op prototype used to obtain the ident and default attributes.
   * @param operands input value operands, or {@code null} for none.
   * @param successors successor blocks, or {@code null} for none.
   * @param outputType result type, or {@code null} for void ops.
   * @param numRegions number of (initially empty) regions to attach.
   * @return the newly constructed operation.
   * @throws IllegalArgumentException if {@code op}'s ident is not yet registered.
   */
  @Contract(pure = true)
  public static Operation Create(
      @NotNull Location location,
      @NotNull Op op,
      @Nullable List<Value> operands,
      @Nullable List<Block> successors,
      @Nullable Type outputType,
      int numRegions) {
    return new Operation(
        location,
        OperationDetails.Registered.lookup(op.getIdent())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        MessageFormat.format("Operation {0} is not registered.", op.getIdent()))),
        operands != null ? operands : List.of(),
        successors != null ? successors : List.of(),
        outputType,
        op.getDefaultAttributes().stream()
            .collect(Collectors.toMap(NamedAttribute::getName, attr -> attr)),
        numRegions);
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
  @Nullable private Block parent = null;

  /** The source location of this operation. */
  @NotNull private final Location location;

  // =========================================================================
  // Constructors
  // =========================================================================

  /**
   * Full constructor for Operation.
   *
   * @param location The source location of this operation.
   * @param details The operation details.
   * @param operands The input values.
   * @param successors The blocks succeeding this operation.
   * @param resultType The output result type.
   * @param attributes The named attributes.
   * @param numRegions The number of regions.
   */
  public Operation(
      @NotNull Location location,
      @NotNull OperationDetails details,
      @NotNull List<Value> operands,
      @NotNull List<Block> successors,
      @Nullable Type resultType,
      @NotNull Map<String, NamedAttribute> attributes,
      int numRegions) {
    this.location = location;

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

  /**
   * Run the {@link core.OperationVerifier} on this operation.
   *
   * @param recursive {@code true} to also verify all nested operations and blocks.
   * @return {@code true} if verification succeeds.
   */
  @Contract(pure = true)
  public boolean verify(boolean recursive) {
    return new OperationVerifier(recursive).verify(this);
  }

  // =========================================================================
  // Details & Traits
  // =========================================================================

  /**
   * Returns the {@link OperationDetails} that describe this operation kind.
   *
   * @return the details instance, never {@code null}.
   */
  @Contract(pure = true)
  public @NotNull OperationDetails getDetails() {
    return details;
  }

  /**
   * Returns {@code true} if this operation's kind implements the given trait.
   *
   * @param traitClass the trait to check for.
   * @return {@code true} if the trait is present.
   */
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

  /**
   * Returns the input value operands of this operation.
   *
   * @return an unmodifiable list of {@link ValueOperand}s.
   */
  @Contract(pure = true)
  public @NotNull @Unmodifiable List<ValueOperand> getOperands() {
    return operands;
  }

  /**
   * Returns the operand at the given index, if present.
   *
   * @param index zero-based operand index.
   * @return the operand, or empty if the index is out of range.
   */
  @Contract(pure = true)
  public @NotNull Optional<ValueOperand> getOperand(int index) {
    return operands.size() > index ? Optional.of(operands.get(index)) : Optional.empty();
  }

  /**
   * Returns the value referenced by the operand at the given index, if present.
   *
   * @param i zero-based operand index.
   * @return the referenced {@link Value}, or empty if the index is out of range or unset.
   */
  @Contract(pure = true)
  public @NotNull Optional<Value> getOperandValue(int i) {
    return getOperand(i).flatMap(ValueOperand::getValue);
  }

  @Contract(pure = true)
  public @NotNull Optional<Type> getOperandType(int i) {
    return getOperandValue(i).map(Value::getType);
  }

  /**
   * Returns the block operands (successor-block references) of this operation.
   *
   * @return an unmodifiable list of {@link BlockOperand}s.
   */
  @Contract(pure = true)
  public @NotNull @Unmodifiable List<BlockOperand> getBlockOperands() {
    return blockOperands;
  }

  /**
   * Get the successor blocks of this operation via its block operands.
   *
   * @return An unmodifiable list of successor blocks.
   */
  @Contract(pure = true)
  public @NotNull @Unmodifiable List<Block> getSuccessors() {
    return getBlockOperands().stream()
        .map(BlockOperand::getValue)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();
  }

  /**
   * Returns the {@link OperationResult} for this operation, if it produces a value.
   *
   * @return the result wrapper, or empty for void operations.
   */
  @Contract(pure = true)
  public @NotNull Optional<OperationResult> getOutput() {
    return Optional.ofNullable(output);
  }

  /**
   * Returns the output {@link Value} produced by this operation, if any.
   *
   * @return the output value, or empty for void operations.
   */
  @Contract(pure = true)
  public @NotNull Optional<Value> getOutputValue() {
    if (output == null) return Optional.empty();
    return getOutput().map(OperationResult::getValue);
  }

  /**
   * Replace the output value of this operation.
   *
   * @param value the new output value; its type must match the existing result type.
   * @throws AssertionError if this operation has no output.
   */
  public void setOutputValue(@NotNull Value value) {
    assert this.output != null : "Trying to set output value of an operation that has no output.";
    this.output.setValue(value);
  }

  // =========================================================================
  // Attributes
  // =========================================================================

  /**
   * Returns all named attributes of this operation.
   *
   * @return an unmodifiable map from attribute name to {@link NamedAttribute}.
   */
  @Contract(pure = true)
  public @NotNull @Unmodifiable Map<String, NamedAttribute> getAttributes() {
    return attributes;
  }

  /**
   * Returns the attribute value for the given name, if present and set.
   *
   * @param name the attribute name to look up.
   * @return the {@link Attribute}, or empty if not present or not set.
   */
  @Contract(pure = true)
  public @NotNull Optional<Attribute> getAttributeByName(@NotNull String name) {
    if (!getAttributes().containsKey(name)) return Optional.empty();
    return getAttributes().get(name).getAttribute();
  }

  /**
   * Returns the attribute for the given name cast to {@code clazz}, if present, set, and of the
   * correct type.
   *
   * @param clazz the expected attribute class.
   * @param name the attribute name.
   * @param <T> the attribute type.
   * @return the typed attribute, or empty if absent, unset, or the wrong type.
   */
  @Contract(pure = true)
  public <T extends Attribute> @NotNull Optional<T> getAttribute(
      @NotNull Class<T> clazz, @NotNull String name) {
    var attribute = getAttributeByName(name);
    if (attribute.isEmpty() || !clazz.isInstance(attribute.get())) return Optional.empty();
    return Optional.of(clazz.cast(attribute.get()));
  }

  /**
   * Set the value of an existing named attribute.
   *
   * @param name the attribute name; the attribute must already exist in the map.
   * @param attribute the new attribute value.
   * @throws AssertionError if no attribute with the given name exists.
   */
  public void setAttribute(@NotNull String name, @NotNull Attribute attribute) {
    NamedAttribute namedAttribute = getAttributes().get(name);
    assert namedAttribute != null
        : MessageFormat.format("Attribute with name {0} does not exist.", name);
    namedAttribute.setAttribute(attribute);
  }

  // =========================================================================
  // Regions
  // =========================================================================

  /**
   * Returns the regions attached to this operation.
   *
   * @return an unmodifiable list of {@link Region}s.
   */
  @Contract(pure = true)
  public @NotNull @Unmodifiable List<Region> getRegions() {
    return regions;
  }

  /**
   * Returns the region at the given index, if present.
   *
   * @param index zero-based region index.
   * @return the region, or empty if the index is out of range.
   */
  @Contract(pure = true)
  public @NotNull Optional<Region> getRegion(int index) {
    return regions.size() > index ? Optional.of(regions.get(index)) : Optional.empty();
  }

  /**
   * Returns the first region attached to this operation, if any.
   *
   * @return the first region, or empty if this operation has no regions.
   */
  @Contract(pure = true)
  public @NotNull Optional<Region> getFirstRegion() {
    return regions.isEmpty() ? Optional.empty() : Optional.of(regions.getFirst());
  }

  // =========================================================================
  // Parent & Navigation
  // =========================================================================

  /**
   * Returns the block that contains this operation, if any.
   *
   * @return the parent block, or empty if this operation is not yet placed in a block.
   */
  @Contract(pure = true)
  public @NotNull Optional<Block> getParent() {
    return Optional.ofNullable(parent);
  }

  /**
   * Returns the region that contains the parent block of this operation, if any.
   *
   * @return the parent region, or empty if not available.
   */
  @Contract(pure = true)
  public @NotNull Optional<Region> getParentRegion() {
    return getParent().flatMap(Block::getParent);
  }

  /**
   * Returns the operation that owns the parent region of this operation, if any.
   *
   * @return the parent operation, or empty if this operation is at the top of the tree.
   */
  @Contract(pure = true)
  public @NotNull Optional<Operation> getParentOperation() {
    return getParentRegion().flatMap(Region::getParent);
  }

  /**
   * Set the parent block of this operation. May only be called from {@link Block}.
   *
   * @param parent the new parent block, or {@code null} to detach.
   * @throws AssertionError if called from outside {@link Block}, or if this operation already has a
   *     non-null parent and the new value is also non-null.
   */
  public void setParent(Block parent) {
    assert Utils.getCallingClass() == Block.class
        : MessageFormat.format(
            "Assigning the parent of an operation is only allowed from the Block class. Was called from {0}",
            Utils.getCallingClass().getName());
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

  @Contract(pure = true)
  public @NotNull Location getLocation() {
    return location;
  }

  // =========================================================================
  // Diagnostics
  // =========================================================================

  /**
   * Print an informational message referencing this operation to standard output.
   *
   * @param s the message text.
   */
  @Contract(pure = true)
  public void emitMessage(@NotNull String s) {
    System.out.println(MessageFormat.format("Message: {0}\n\t| {1}", this, s));
  }

  /**
   * Print a warning referencing this operation to standard output (in yellow).
   *
   * @param s the warning text.
   */
  @Contract(pure = true)
  public void emitWarning(@NotNull String s) {
    System.out.println(MessageFormat.format("\u001B[33mWarning: {0}\n\t| {1}\u001B[0m", this, s));
  }

  /**
   * Print an error referencing this operation to standard error.
   *
   * @param s the error text.
   */
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
    sb.append(getDetails().ident());

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

    if (!location.equals(Location.UNKNOWN)) {
      sb.append(" @ ");
      sb.append(location);
    }

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
