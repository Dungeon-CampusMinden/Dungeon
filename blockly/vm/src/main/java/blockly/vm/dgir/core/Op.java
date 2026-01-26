package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.OpSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public abstract class Op {
  private Operation operation;

  public abstract OperationDetails.Impl createDetails();

  // Every op should be default-constructible
  public Op() {
    this.operation = null;
  }

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  @JsonIgnore
  public OperationDetails getDetails() {
    assert getOperation() != null : "Operation is null.";
    return getOperation().getDetails();
  }

  public void setDetails(OperationDetails details) {
    assert getOperation() != null : "Operation is null.";
    getOperation().setDetails(details);
  }

  @JsonIgnore
  public List<ValueOperand> getOperands() {
    assert getOperation() != null : "Operation is null.";
    return getOperation().getOperands();
  }

  public void addOperand(ValueOperand operand) {
    assert getOperation() != null : "Operation is null.";
    getOperation().addOperand(operand);
  }

  @JsonIgnore
  public OperationResult getOutput() {
    assert getOperation() != null : "Operation is null.";
    return getOperation().getOutput();
  }

  public void setOutput(OperationResult output) {
    assert getOperation() != null : "Operation is null.";
    getOperation().setOutput(output);
  }

  public void removeOutput() {
    assert getOperation() != null : "Operation is null.";
    getOperation().removeOutput();
  }

  @JsonIgnore
  public Map<String, NamedAttribute> getAttributes() {
    assert getOperation() != null : "Operation is null.";
    return getOperation().getAttributes();
  }

  @JsonIgnore
  public List<Region> getRegions() {
    assert getOperation() != null : "Operation is null.";
    return getOperation().getRegions();
  }

  public void addRegion(Region region) {
    assert getOperation() != null : "Operation is null.";
    getOperation().addRegion(region);
  }

  public void removeRegion(Region region) {
    assert getOperation() != null : "Operation is null.";
    getOperation().removeRegion(region);
  }
}
