package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.*;

/**
 * This class represents the data state associated with each concrete implementation of an
 * operation. This structure is used so that operations can be constructed independently
 * of their behavior. This is especially useful for serialization and deserialization.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Operation implements Serializable {
  /**
   * The unique identifier of this operation.
   */
  private OperationName name;

  /**
   * The input values of this operation.
   */
  private ValueOperand[] operands;

  // TODO Add the block operands

  /**
   * The output of this operation.
   */
  private OperationResult output;

  /**
   * The attributes of this operation.
   */
  private Map<String, NamedAttribute> attributes;

  /**
   * The regions of this operation.
   */
  private Region[] regions;

  /**
   * The block containing this operation.
   */
  @JsonIgnore
  private Block block = null;

  public static Operation Create(String name,
                                 List<ValueOperand> operands,
                                 OperationResult output,
                                 List<NamedAttribute> attributes,
                                 List<Region> regions) {
    return Create(RegisteredOperationName.lookup(name).orElseThrow(), operands, output, attributes, regions);
  }

  public static Operation Create(OperationName name,
                                 List<ValueOperand> operands,
                                 OperationResult output,
                                 List<NamedAttribute> attributes,
                                 List<Region> regions) {
    ValueOperand[] operandsArray = null;
    if (operands != null) {
      operandsArray = operands.toArray(ValueOperand[]::new);
    }
    // Ensure that each attribute that is listed inside the OperationName is also part of the attributes list before
    // initializing the attributes
    if (name.getAttributeNames() != null) {
      if (attributes == null) {
        attributes = new ArrayList<>(name.getAttributeNames().length);
        for (var attributeName : name.getAttributeNames()) {
          attributes.add(new NamedAttribute(attributeName, null));
        }
      }else {
        for (var attributeName : name.getAttributeNames()) {
          if (attributes.stream().noneMatch(attr -> attr.getName().equals(attributeName))) {
            attributes.add(new NamedAttribute(attributeName, null));
          }
        }
      }
    }
    var attributesArray = attributes.toArray(NamedAttribute[]::new);
    name.populateDefaultAttrs(attributesArray);
    var attributesDict = Arrays.stream(attributesArray).collect(
      java.util.stream.Collectors.toMap(NamedAttribute::getName, attr -> attr));
    var regionArray = regions.toArray(Region[]::new);
    return new Operation(name, operandsArray, output, attributesDict, regionArray);
  }

  public Operation(OperationName name,
                   ValueOperand[] operands,
                   OperationResult output,
                   Map<String, NamedAttribute> attributes,
                   Region[] regions) {
    this.name = name;
    this.operands = operands;
    this.output = output;
    this.attributes = attributes;
    this.regions = regions;
  }


  public OperationName getName() {
    return name;
  }

  public ValueOperand[] getOperands() {
    if (operands == null) return null;
    return operands;
  }

  public OperationResult getOutput() {
    return output;
  }

  public Map<String, NamedAttribute> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  public Region[] getRegions() {
    return regions;
  }
}
