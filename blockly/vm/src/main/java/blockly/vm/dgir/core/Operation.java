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
  private OperationDetails details = null;

  /**
   * The input values of this operation.
   */
  @JsonManagedReference
  private final List<ValueOperand> operands = new ArrayList<>();

  /**
   * The output of this operation.
   */
  @JsonManagedReference
  private OperationResult output = null;

  /**
   * The attributes of this operation.
   */
  @JsonManagedReference
  private final Map<String, NamedAttribute> attributes = new HashMap<>();

  /**
   * The regions of this operation.
   */
  @JsonManagedReference
  private final List<Region> regions = new ArrayList<>();

  /**
   * The block containing this operation.
   */
  @JsonBackReference
  private Block parent = null;

  public static Operation Create(String name,
                                 List<ValueOperand> operands,
                                 OperationResult output,
                                 List<Region> regions) {
    return Create(RegisteredOperationDetails.lookup(name).orElseThrow(), operands, output, regions);
  }

  public static Operation Create(OperationDetails name,
                                 List<ValueOperand> operands,
                                 OperationResult output,
                                 List<Region> regions) {
    // Ensure the most important arguments is non-null
    if (name == null) throw new IllegalArgumentException("Operation details cannot be null");

    // Ensure operands is a valid arraylist
    operands = operands == null ? new ArrayList<>() : new ArrayList<>(operands);

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
    return new Operation(name, operands, output, attributeMap, regions);
  }

  // Operations should always be default constructible (for serialization purposes).
  public Operation() {

  }

  public Operation(OperationDetails details,
                   List<ValueOperand> operands,
                   OperationResult output,
                   Map<String, NamedAttribute> attributes,
                   List<Region> regions) {
    setDetails(details);
    setOutput(output);

    for (var operand : operands)
      addOperand(operand);
    for (var attribute : attributes.values())
      addAttribute(attribute);
    for (var region : regions)
      addRegion(region);
  }

  public OperationDetails getDetails() {
    return details;
  }

  public void setDetails(OperationDetails details) {
    assert this.details == null : "Operation details already set.";
    assert details != null : "Operation details cannot be null.";

    this.details = details;
  }

  public List<ValueOperand> getOperands() {
    return Collections.unmodifiableList(operands);
  }

  public void addOperand(ValueOperand operand) {
    assert operand != null : "Operand cannot be null.";
    assert operand.getParent() == null : "Operand already has a parent.";

    operands.add(operand);
    operand.setParent(this);
  }

  public OperationResult getOutput() {
    return output;
  }

  public void setOutput(OperationResult output) {
    assert this.output == null || output == null : "Output already set.";
    assert output == null || output.getParent() == null : "Output already has a parent.";

    if (this.output != null)
      removeOutput();
    this.output = output;
    if (output != null)
      output.setParent(this);
  }

  public void removeOutput() {
    this.output.setParent(null);
    this.output = null;
  }


  public Map<String, NamedAttribute> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  private void addAttribute(NamedAttribute attribute) {
    assert attribute != null : "Attribute cannot be null.";
    assert attribute.getParent() == null : "Attribute already has a parent.";

    attributes.put(attribute.getName(), attribute);
    attribute.setParent(this);
  }

  private void removeAttribute(NamedAttribute attribute) {
    assert attribute != null : "Attribute cannot be null.";
    assert attribute.getParent() == this : "Attribute does not belong to this operation.";

    attributes.remove(attribute.getName());
    attribute.setParent(null);
  }

  public List<Region> getRegions() {
    return Collections.unmodifiableList(regions);
  }

  public void addRegion(Region region) {
    assert region != null : "Region cannot be null.";
    assert region.getParent() == null : "Region already has a parent.";

    regions.add(region);
    region.setParent(this);
  }

  public void removeRegion(Region region) {
    assert region != null : "Region cannot be null.";
    assert region.getParent() == this : "Region does not belong to this operation.";

    regions.remove(region);
    region.setParent(null);
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
