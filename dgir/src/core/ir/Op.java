package core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.detail.OperationDetails;
import core.detail.RegisteredOperationDetails;
import core.serialization.OpDeserializer;
import core.serialization.OpSerializer;
import core.traits.IOpTrait;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Abstract base class for all operations in the DGIR.
 *
 * <p>Each subclass represents a specific operation kind and is responsible for defining its details
 * via {@link #createDetails()}. The actual operation state is held in a backing {@link Operation}
 * instance; this class is a semantic wrapper around that state.
 *
 * <p>The Op class itself is never serialized — the backing {@link Operation} carries all necessary
 * information to recreate the operation.
 */
@JsonSerialize(using = OpSerializer.class)
@JsonDeserialize(using = OpDeserializer.class)
public abstract class Op {

  // =========================================================================
  // Members
  // =========================================================================

  private @Nullable Operation operation;

  // =========================================================================
  // Op Info
  // =========================================================================

  /** Create and return the details object that describes this op kind. */
  public abstract @NotNull OperationDetails.Impl createDetails();

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Every op must be default-constructible (used during dialect registration). */
  public Op() {
    this.operation = null;
  }

  public Op(@NotNull Operation operation) {
    this.operation = operation;
  }

  public Op(boolean ensureEntryBlocks, @NotNull Operation operation) {
    this.operation = operation;
    if (ensureEntryBlocks) ensureEntryBlocks();
  }

  // =========================================================================
  // Operation Access
  // =========================================================================

  /**
   * Returns the backing operation, or {@code null} if not yet initialised.
   *
   * @return the backing operation or null.
   */
  public @Nullable Operation getOperationOrNull() {
    return operation;
  }

  /**
   * Returns the backing operation.
   *
   * @return the backing operation (never null).
   */
  @Contract(pure = true)
  public @NotNull Operation getOperation() {
    assert operation != null : "Operation is null.";
    return operation;
  }

  /**
   * Sets the backing operation and optionally ensures every region has an entry block.
   *
   * @param ensureEntryBlocks whether to create missing entry blocks.
   * @param operation the operation to set.
   */
  public void setOperation(boolean ensureEntryBlocks, @NotNull Operation operation) {
    this.operation = operation;
    if (ensureEntryBlocks) ensureEntryBlocks();
  }

  // =========================================================================
  // Verification
  // =========================================================================

  @Contract(pure = true)
  public boolean verify(boolean recursive) {
    return getOperation().verify(recursive);
  }

  // =========================================================================
  // Details & Traits
  // =========================================================================

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull OperationDetails getDetails() {
    return getOperation().getDetails();
  }

  @Contract(pure = true)
  public boolean hasTrait(@NotNull Class<? extends IOpTrait> traitClass) {
    return getOperation().hasTrait(traitClass);
  }

  /**
   * Return this op as an instance of {@code clazz} if it matches, otherwise empty.
   *
   * @see Operation#as(Class)
   */
  @Contract(pure = true)
  public <T extends Op> @NotNull Optional<T> as(@NotNull Class<T> clazz) {
    return getOperation().as(clazz);
  }

  /**
   * Return this op cast to the given trait if it implements it, otherwise empty.
   *
   * @see Operation#asTrait(Class)
   */
  @Contract(pure = true)
  public <T extends IOpTrait> @NotNull Optional<T> asTrait(@NotNull Class<T> clazz) {
    return getOperation().asTrait(clazz);
  }

  /**
   * Check if this op is of the given type.
   *
   * @see Operation#isa(Class)
   */
  @Contract(pure = true)
  public boolean isa(@NotNull Class<? extends Op> clazz) {
    return getOperation().isa(clazz);
  }

  // =========================================================================
  // Operands & Output
  // =========================================================================

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull List<ValueOperand> getOperands() {
    return getOperation().getOperands();
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<ValueOperand> getOperand(int index) {
    return getOperation().getOperand(index);
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Value> getOperandValue(int i) {
    return getOperation().getOperandValue(i);
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull List<BlockOperand> getBlockOperands() {
    return getOperation().getBlockOperands();
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull List<Block> getSuccessors() {
    return getOperation().getSuccessors();
  }

  @JsonIgnore
  @Contract(pure = true)
  public Optional<OperationResult> getOutput() {
    return getOperation().getOutput();
  }

  @JsonIgnore
  @Contract(pure = true)
  public Optional<Value> getOutputValue() {
    return getOperation().getOutputValue();
  }

  @Contract(pure = true)
  public @Nullable Value getOutputValueThrowing() {
    return getOperation().getOutputValueThrowing();
  }

  public @NotNull Op setOutputValue(@NotNull Value value) {
    getOperation().setOutputValue(value);
    return this;
  }

  // =========================================================================
  // Attributes
  // =========================================================================

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Map<String, NamedAttribute> getAttributes() {
    return getOperation().getAttributes();
  }

  @Contract(pure = true)
  public @NotNull Optional<Attribute> getAttributeByName(@NotNull String name) {
    return getOperation().getAttributeByName(name);
  }

  @Contract(pure = true)
  public <T extends Attribute> @NotNull Optional<T> getAttribute(
      @NotNull Class<T> clazz, @NotNull String name) {
    return getOperation().getAttribute(clazz, name);
  }

  public void setAttribute(@NotNull String name, @NotNull Attribute attribute) {
    getOperation().setAttribute(name, attribute);
  }

  // =========================================================================
  // Regions
  // =========================================================================

  /** Goes over all regions and ensures that each has at least one entry block. */
  public void ensureEntryBlocks() {
    for (Region region : getRegions()) {
      region.ensureEntryBlock();
    }
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull List<Region> getRegions() {
    return getOperation().getRegions();
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Region> getRegion(int index) {
    return getOperation().getRegion(index);
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Region> getFirstRegion() {
    return getOperation().getFirstRegion();
  }

  // =========================================================================
  // Parent & Navigation
  // =========================================================================

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Block> getParent() {
    return getOperation().getParent();
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Region> getParentRegion() {
    return getOperation().getParentRegion();
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Operation> getParentOperation() {
    return getOperation().getParentOperation();
  }

  /**
   * Walks the parent chain and returns the first parent that implements the given trait.
   *
   * @see Operation#getParentWithTrait(Class)
   */
  @Contract(pure = true)
  public <T extends IOpTrait> @NotNull Optional<T> getParentWithTrait(Class<T> traitClass) {
    return getOperation().getParentWithTrait(traitClass);
  }

  /**
   * Get the index of this operation in its parent block's operations list.
   *
   * @see Operation#getIndex()
   */
  @Contract(pure = true)
  public int getIndex() {
    return getOperation().getIndex();
  }

  /**
   * Get the next operation in the same block.
   *
   * @see Operation#getNext()
   */
  @Contract(pure = true)
  public @NotNull Optional<Operation> getNext() {
    return getOperation().getNext();
  }

  // =========================================================================
  // Diagnostics
  // =========================================================================

  @Contract(pure = true)
  public void emitMessage(@NotNull String s) {
    getOperation().emitMessage(s);
  }

  @Contract(pure = true)
  public void emitWarning(@NotNull String s) {
    getOperation().emitWarning(s);
  }

  @Contract(pure = true)
  public void emitError(@NotNull String s) {
    getOperation().emitError(s);
  }

  // =========================================================================
  // Object
  // =========================================================================

  /** Equality is based on the backing operation — Op is only a semantic wrapper. */
  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof Op other && this.getOperation().equals(other.getOperation());
  }

  @Override
  public int hashCode() {
    if (operation == null) return 0;
    return operation.hashCode();
  }

  // =========================================================================
  // Static Helpers
  // =========================================================================

  /**
   * Execute {@code callback} only if the given op class is already registered.
   *
   * <p>Intended for use in default constructors: during dialect registration the default
   * constructor is called without registration being complete, so no initialisation should happen
   * then. On all subsequent calls the op is registered and the callback is executed normally.
   */
  public static void executeIfRegistered(
      @NotNull Class<? extends Op> opClass, @NotNull Runnable callback) {
    var details = RegisteredOperationDetails.lookup(opClass);
    if (details.isPresent()) {
      callback.run();
    }
  }
}
