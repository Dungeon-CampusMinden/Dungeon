package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.OperationTypeIdResolver;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({"ident"})
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "ident")
@JsonTypeIdResolver(OperationTypeIdResolver.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Operation implements IIdentifiableType, Cloneable, Serializable {
  /**
   * The input values of this operation.
   */
  private List<ValueOperand> operands;

  /**
   * The output of this operation.
   */
  private OperationResult output;

  /**
   * The attributes of this operation.
   */
  private List<NamedAttribute> attributes;

  /**
   * The regions of this operation.
   */
  private List<Region> regions;

  /**
   * The parent block of this operation.
   */
  private Block parent;

  public Operation() {
    operands = null;
    output = null;
    attributes = null;
    regions = null;
  }

  @JsonCreator
  public Operation(@JsonProperty("operands") List<ValueOperand> operands, @JsonProperty("output") OperationResult output, @JsonProperty("attributes") List<NamedAttribute> attributes, @JsonProperty("regions") List<Region> regions) {
    this.operands = operands;
    this.output = output;
    this.attributes = attributes;
    this.regions = regions;
  }

  @Override
  public Operation clone() {
    try {
      Operation clone = (Operation) super.clone();
      clone.output = this.output;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  // ---------- Operands --------------------------------------------

  /**
   * Only used for serialization.
   *
   * @return The operands.
   */
  @JsonProperty("operands")
  private List<ValueOperand> getOperandsRaw() {
    return operands;
  }

  /**
   * Get the operands of this operation.
   *
   * @return The optional operands.
   */
  @JsonIgnore
  public Optional<List<ValueOperand>> getOperands() {
    return Optional.ofNullable(operands);
  }

  /**
   * Get or create the operands of this operation.
   *
   * @return The operands.
   */
  @JsonIgnore
  public List<ValueOperand> getOrCreateOperands() {
    if (operands == null) {
      operands = new ArrayList<>();
    }
    return operands;
  }

  /**
   * Add an operand to this operation.
   *
   * @param operand The operand to add.
   */
  public void addOperand(ValueOperand operand) {
    var operands = getOrCreateOperands();
    operands.add(operand);
  }

  /**
   * Set the operands of this operation.
   *
   * @param operands The new operands.
   */
  public void setOperands(List<ValueOperand> operands) {
    this.operands = operands;
  }

  /**
   * Remove an operand from this operation.
   *
   * @param operand The operand to remove.
   */
  public void removeOperand(ValueOperand operand) {
    if (operands != null) operands.remove(operand);
  }

  // ---------- Output --------------------------------------------

  /**
   * Checks if this operation has an output.
   *
   * @return true if the operation has an output, false otherwise
   */
  public boolean hasOutput() {
    return output != null;
  }

  /**
   * Get the output of this operation.
   *
   * @return The output of this operation.
   */
  public OperationResult getOutput() {
    return output;
  }

  /**
   * Set the output of this operation.
   *
   * @param output The new output of this operation.
   */
  public void setOutput(OperationResult output) {
    this.output = output;
  }

  // ---------- Attributes --------------------------------------------

  /**
   * Only used for serialization.
   * @return The attributes.
   */
  @JsonProperty("attributes")
  private List<NamedAttribute> getAttributesRaw() {
    return attributes;
  }

  /**
   * Get the attributes of this operation.
   *
   * @return The optional list of attributes.
   */
  @JsonIgnore
  public Optional<List<NamedAttribute>> getAttributes() {
    return Optional.ofNullable(attributes);
  }

  /**
   * Get or create the attributes of this operation.
   *
   * @return The list of attributes.
   */
  @JsonIgnore
  public List<NamedAttribute> getOrCreateAttributes() {
    if (attributes == null) {
      attributes = new ArrayList<>();
    }
    return attributes;
  }

  /**
   * Add an attribute to this operation.
   *
   * @param attribute the attribute to add
   */
  public void addAttribute(NamedAttribute attribute) {
    var attributes = getOrCreateAttributes();
    attributes.add(attribute);
  }

  /**
   * Set the attributes of this operation.
   *
   * @param attributes the new attributes
   */
  public void setAttributes(List<NamedAttribute> attributes) {
    this.attributes = attributes;
  }

  /**
   * Remove an attribute from this operation.
   *
   * @param attribute the attribute to remove
   */
  public void removeAttribute(NamedAttribute attribute) {
    if (attributes != null) attributes.remove(attribute);
  }

  // ---------- Regions --------------------------------------------

  /**
   * Checks if this operation has regions.
   *
   * @return true if regions are present, false otherwise
   */
  public boolean hasRegion() {
    return regions != null && !regions.isEmpty();
  }

  /**
   * Only used for serialization.
   * @return The regions.
   */
  @JsonProperty("regions")
  private List<Region> getRegionsRaw() {
    return regions;
  }

  /**
   * Get the regions of this operation.
   *
   * @return An optional unmodifiable view of regions.
   */
  @JsonIgnore
  public Optional<List<Region>> getRegions() {
    if (regions == null) return Optional.empty();
    return Optional.of(Collections.unmodifiableList(regions));
  }

  /**
   * Get or create the regions of this operation.
   *
   * @return An unmodifiable view of regions.
   */
  @JsonIgnore
  public List<Region> getOrCreateRegions() {
    if (regions == null) {
      regions = new ArrayList<>();
    }
    return Collections.unmodifiableList(regions);
  }

  /**
   * Add a region to this operation.
   *
   * @param region the region to add
   */
  protected void addRegion(Region region) {
    if (regions == null) {
      regions = new ArrayList<>();
    }
    regions.add(region);
  }

  /**
   * Remove a region from this operation.
   *
   * @param region the region to remove
   */
  protected void removeRegion(Region region) {
    if (regions == null) return;
    regions.remove(region);
  }

  /**
   * Set the regions of this operation.
   *
   * @param regions the new regions
   */
  protected void setRegions(List<Region> regions) {
    this.regions = regions;
  }

  // ---------- Regions --------------------------------------------

  @JsonIgnore
  public Block getParent() {
    return parent;
  }

  public void setParent(Block parent) {
    this.parent = parent;
  }
}
