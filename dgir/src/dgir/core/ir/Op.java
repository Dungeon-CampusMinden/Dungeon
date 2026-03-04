package dgir.core.ir;

import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.serialization.OpDeserializer;
import dgir.core.serialization.OpSerializer;
import dgir.core.traits.IOpTrait;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Abstract base class for all operations in the DGIR.
 *
 * <p>Each subclass represents a specific operation kind and is responsible for defining its details
 * via the associated abstract functions. The actual operation state is held in a backing {@link
 * Operation} instance; this class is a semantic wrapper around that state.
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

  /**
   * Get the dialect that contributed this operation.
   *
   * @return the dialect that contributed this operation.
   */
  @Contract(pure = true)
  @NotNull
  public abstract Class<? extends Dialect> getDialect();

  /**
   * Get the namespace of this dialect.
   *
   * @return the namespace of this dialect.
   */
  @Contract(pure = true)
  @NotNull
  public abstract String getNamespace();

  /**
   * Get the unique identifier of this operation.
   *
   * @return the unique identifier of this operation.
   */
  @Contract(pure = true)
  @NotNull
  public abstract String getIdent();

  /**
   * Get a list of all default attributes for this operation. These attributes are populated on the
   * operation when it is created, and can be used to provide default values for attributes that are
   * not explicitly set by the user.
   *
   * <p>All attributes need to be defined at this point since it is a immutable property of the
   * operation.
   *
   * @return a list of all default attributes for this operation.
   */
  @Contract(pure = true)
  @NotNull
  @Unmodifiable
  public List<NamedAttribute> getDefaultAttributes() {
    return List.of();
  }

  /**
   * Get a verifier function for this operation. This function is called during the verification
   * phase of the operation, and is used to check that the operation is well-formed. The function
   * should return true if the operation is well-formed, and false otherwise.
   *
   * @return a verifier function for this operation.
   */
  public abstract Function<Operation, Boolean> getVerifier();

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Every op must be default-constructible (used during dialect registration). */
  public Op() {
    this.operation = null;
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
   * Sets the backing operation.
   *
   * @param operation the operation to set.
   */
  public void setOperation(@NotNull Operation operation) {
    this.operation = operation;
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

  @Contract(pure = true)
  public @NotNull List<ValueOperand> getOperands() {
    return getOperation().getOperands();
  }

  @Contract(pure = true)
  public @NotNull Optional<ValueOperand> getOperand(int index) {
    return getOperation().getOperand(index);
  }

  @Contract(pure = true)
  public @NotNull Optional<Value> getOperandValue(int i) {
    return getOperation().getOperandValue(i);
  }

  @Contract(pure = true)
  public @NotNull List<BlockOperand> getBlockOperands() {
    return getOperation().getBlockOperands();
  }

  @Contract(pure = true)
  public @NotNull List<Block> getSuccessors() {
    return getOperation().getSuccessors();
  }

  @Contract(pure = true)
  public Optional<OperationResult> getOutput() {
    return getOperation().getOutput();
  }

  @Contract(pure = true)
  public Optional<Value> getOutputValue() {
    return getOperation().getOutputValue();
  }

  public @NotNull Op setOutputValue(@NotNull Value value) {
    getOperation().setOutputValue(value);
    return this;
  }

  // =========================================================================
  // Attributes
  // =========================================================================

  @Contract(pure = true)
  public @NotNull @Unmodifiable Map<String, NamedAttribute> getAttributeMap() {
    return getOperation().getAttributeMap();
  }

  @Contract(pure = true)
  public @NotNull @Unmodifiable List<NamedAttribute> getNamedAttributes() {
    return getOperation().getNamedAttributes();
  }

  @Contract(pure = true)
  public @NotNull @Unmodifiable List<Attribute> getAttributes() {
    return getOperation().getAttributes();
  }

  @Contract(pure = true)
  public @NotNull Optional<Attribute> getAttribute(@NotNull String name) {
    return getOperation().getAttribute(name);
  }

  @Contract(pure = true)
  public <T extends Attribute> @NotNull Optional<T> getAttributeAs(
      @NotNull String name, @NotNull Class<T> clazz) {
    return getOperation().getAttributeAs(name, clazz);
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

  @Contract(pure = true)
  public @NotNull List<Region> getRegions() {
    return getOperation().getRegions();
  }

  @Contract(pure = true)
  public @NotNull Optional<Region> getRegion(int index) {
    return getOperation().getRegion(index);
  }

  @Contract(pure = true)
  public @NotNull Optional<Region> getFirstRegion() {
    return getOperation().getFirstRegion();
  }

  // =========================================================================
  // Parent & Navigation
  // =========================================================================

  @Contract(pure = true)
  public @NotNull Optional<Block> getParent() {
    return getOperation().getParent();
  }

  @Contract(pure = true)
  public @NotNull Optional<Region> getParentRegion() {
    return getOperation().getParentRegion();
  }

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

  /**
   * Get the source location of this operation.
   *
   * @see Operation#getLocation()
   */
  @Contract(pure = true)
  public @NotNull Location getLocation() {
    return getOperation().getLocation();
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
    var details = OperationDetails.Registered.lookup(opClass);
    if (details.isPresent()) {
      callback.run();
    }
  }
}
