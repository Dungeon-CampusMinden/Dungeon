package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Operation implements IIdentifiableType, Serializable {
  /**
   * The input values of this operation.
   */
  @JsonManagedReference
  private List<ValueOperand> operands;

  /**
   * The output of this operation.
   */
  @JsonManagedReference
  private OperationResult output;

  /**
   * The attributes of this operation.
   */
  @JsonManagedReference
  private List<NamedAttribute> attributes;

  /**
   * The regions of this operation.
   */
  @JsonManagedReference
  private List<Region> regions;

  /**
   * The parent block of this operation.
   */
  @JsonBackReference
  public Block owner;

  /**
   * Remove the operation from its currently containing block
   */
  public void removeFromBlock() {
    if (owner != null)
      owner.removeOperation(this);
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
    operand.owner = this;
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
   *
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
   * Get or create the attribute with the given name.
   *
   * @param name         The name of the attribute.
   * @param defaultValue The default value of the attribute if it does not exist.
   * @return The attribute.
   */
  public NamedAttribute getOrCreateAttribute(String name, Attribute defaultValue) {
    return getOrCreateAttributes().stream().filter(a -> a.getName().equals(name)).findFirst().orElseGet(() -> {
      NamedAttribute attribute = new NamedAttribute(name, defaultValue, this);
      addAttribute(attribute);
      return attribute;
    });
  }

  /**
   * Get the attribute with the given name.
   * @param name The name of the attribute.
   * @return The attribute.
   */
  public Optional<NamedAttribute> getAttribute(String name) {
    return getOrCreateAttributes().stream().filter(a -> a.getName().equals(name)).findFirst();
  }

  /**
   * Add an attribute to this operation.
   *
   * @param attribute the attribute to add
   */
  public void addAttribute(String name, Attribute attribute) {
    var attributes = getOrCreateAttributes();
    if (!attributeExists(name)) {
      attributes.add(new NamedAttribute(name, attribute, this));
    } else {
      throw new IllegalArgumentException("Attribute with name " + name + " already exists.");
    }
  }

  /**
   * Add an attribute to this operation.
   *
   * @param attribute the attribute to add
   */
  public void addAttribute(NamedAttribute attribute) {
    var attributes = getOrCreateAttributes();
    if (!attributeExists(attribute.getName())) {
      attributes.add(attribute);
    } else {
      throw new IllegalArgumentException("Attribute with name " + attribute.getName() + " already exists.");
    }
  }

  public boolean attributeExists(String name) {
    return getOrCreateAttributes().stream().anyMatch(a -> a.getName().equals(name));
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
   *
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
}
