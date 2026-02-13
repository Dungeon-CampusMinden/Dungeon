package core.ir;

import core.detail.OperationDetails;
import core.detail.RegisteredOperationDetails;
import core.serialization.OpDeserializer;
import core.serialization.OpSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import java.util.Map;

/**
 * Abstract base class for all operations in the DGIR.
 * This class contains the actual state in form an operation object.
 * The derived classes are responsible for creating the specific implementations of the operation behavior.
 * They are also responsible for serialization and deserialization behavior of the operations.
 * The Op class will never be serialized, but the state will be which contains all the necessary information to recreate the operation.
 */
@JsonSerialize(using = OpSerializer.class)
@JsonDeserialize(using = OpDeserializer.class)
public abstract class Op {
  private Operation operation;

  public abstract OperationDetails.Impl createDetails();

  // Every op should be default-constructible
  public Op() {
    this.operation = null;
  }

  public Op(Operation operation) {
    this.operation = operation;
  }

  public Op(boolean ensureEntryBlocks, Operation operation) {
    this.operation = operation;
    if (ensureEntryBlocks)
      ensureEntryBlocks();
  }

  /**
   * Sets the operation and ensures that all regions have an entry block.
   *
   * @param operation the operation to set
   */
  public void setOperation(boolean ensureEntryBlocks, Operation operation) {
    this.operation = operation;
    if (ensureEntryBlocks)
      ensureEntryBlocks();
  }

  /**
   * Returns the underlying operation if it exists, otherwise returns null.
   *
   * @return the underlying operation or null
   */
  public Operation getOperationOrNull() {
    return operation;
  }

  /**
   * Returns the underlying operation.
   *
   * @return the underlying operation (never null)
   */
  public Operation getOperation() {
    assert operation != null : "Operation is null.";
    return operation;
  }

  public boolean verify(boolean recursive) {
    return getOperation().verify(recursive);
  }

  public <OpT extends Op> OpT as(Class<OpT> clazz) {
    assert clazz.isInstance(this) : "Operation is not of type " + clazz.getName();
    return clazz.cast(this);
  }

  @JsonIgnore
  public OperationDetails getDetails() {
    return getOperation().getDetails();
  }

  @JsonIgnore
  public List<ValueOperand> getOperands() {
    return getOperation().getOperands();
  }

  @JsonIgnore
  public OperationResult getOutput() {
    return getOperation().getOutput();
  }

  @JsonIgnore
  public Value getOutputValue() {
    return getOutput().getValue();
  }

  public Op setOutputValue(Value value) {
    getOperation().setOutputValue(value);
    return this;
  }

  @JsonIgnore
  public Map<String, NamedAttribute> getAttributes() {
    return getOperation().getAttributes();
  }

  /**
   * Goes over all blocks and ensures that they have at least their entry block.
   */
  public void ensureEntryBlocks() {
    for (Region region : getRegions()) {
      region.ensureEntryBlock();
    }
  }

  @JsonIgnore
  public List<Region> getRegions() {
    return getOperation().getRegions();
  }

  @JsonIgnore
  public Region getRegion(int index) {
    return getRegions().get(index);
  }

  @JsonIgnore
  public Region getFirstRegion() {
    return getRegion(0);
  }

  public <T extends Attribute> T getAttribute(Class<T> clazz, String name) {
    return clazz.cast(getAttributes().get(name).getAttribute());
  }

  public List<Block> getSuccessors() {
    return getOperation().getSuccessors();
  }

  /**
   * Check equality based on the underlying operation.
   * Ops are only a sematic wrapper of the operation state and therefore not indicative of equality.
   *
   * @param obj the reference object with which to compare.
   * @return true if the underlying operations are equal, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof Op other && this.getOperation().equals(other.getOperation());
  }

  /**
   * Hash code based on the underlying operation.
   *
   * @return the hash code value for the operation stored in this op.
   */
  @Override
  public int hashCode() {
    return operation.hashCode();
  }

  /**
   * This is a helper method to execute a callback if an op class is registered in the RegisteredOperationDetails.
   * It is mainly intended for use in default constructors which also get invoked for registration and therefore cannot
   * rely on the registration being completed.
   * <p>
   * In this case, during registration through the {@link core.Dialect} no code is executed and the OperationDetails can
   * be retreived through the {@link Op#createDetails} method.
   * All subsequent uses of the default constructor will be used to create an actual instance of that op.
   * <p>
   * This is useful for cases where we want to execute some code only if a certain op is registered, without having to
   * check for the registration multiple times.
   *
   */
  public static void executeIfRegistered(Class<? extends Op> opClass, Runnable callback) {
    var details = RegisteredOperationDetails.lookup(opClass);
    if (details.isPresent()) {
      callback.run();
    }
  }
}
