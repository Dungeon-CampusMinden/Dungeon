package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.detail.RegisteredOperationDetails;
import blockly.vm.dgir.core.ir.*;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.List;

public class OperationDeserializer extends StdDeserializer<Operation> {
  public OperationDeserializer() {
    this(Operation.class);
  }

  public OperationDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Operation deserialize(JsonParser jp, DeserializationContext ctxt) {
    JsonNode node = jp.readValueAsTree();
    // Get the ident field so that we can lookup the correct operation type.
    var operationDetails = RegisteredOperationDetails.lookup(node.get("ident").asString());
    assert operationDetails.isPresent() : "Operation " + node.get("ident").asString() + " must be a registered to deserialize.\n\tMake sure to load its dialect before deserializing.";

    // Deserialize the operands from the node, these are serialized as a list of value references.
    List<Value> operands = null;
    if (node.has("operands")) {
      operands = new ArrayList<>();
      for (JsonNode operandNode : node.get("operands")) {
        // Let Jackson resolve the identity reference so already-deserialized body values inside a region are reused.
        Value value = ctxt.readTreeAsValue(operandNode, Value.class);
        operands.add(value);
      }
    }
    // Deserialize the attributes if they exist.
    List<NamedAttribute> attributes = null;
    if (node.has("attributes")) {
      attributes = new ArrayList<>();
      for (JsonNode attributeNode : node.get("attributes")) {
        NamedAttribute attribute = ctxt.readTreeAsValue(attributeNode, NamedAttribute.class);
        attributes.add(attribute);
      }
    }
    // Deserialize the output if it exists. Since only the output type is serialized we only need to deserialize that.
    Value outputValue = null;
    if (node.has("output")) {
      JsonNode outputNode = node.get("output");
      outputValue = ctxt.readTreeAsValue(outputNode, Value.class);
    }

    // Deserialize successors if they exist. These are the block operands
    List<Block> successors = null;
    if (node.has("successors")) {
      successors = new ArrayList<>();
      for (JsonNode successorNode : node.get("successors")) {
        Block block = ctxt.readTreeAsValue(successorNode, Block.class);
        successors.add(block);
      }
    }

    // Deserialize regions if they exist.
    List<Region> regions = null;
    if (node.has("regions")) {
      regions = new ArrayList<>();
      for (JsonNode regionNode : node.get("regions")) {
        Region region = ctxt.readTreeAsValue(regionNode, Region.class);
        regions.add(region);
      }
    }

    Operation operation = null;
    // In case we do have the output value resolved we must set it on the operation so that it points to the correct value
    if (outputValue != null) {
      // Create the operation instance with the resolved output value type and set the output value on the operation.
      operation = Operation.Create(
        operationDetails.get(),
        operands,
        successors,
        outputValue.getType(),
        regions != null ? regions.size() : 0);
      operation.setOutputValue(outputValue);
    } else {
      // Create the operation instance.
      operation = Operation.Create(
        operationDetails.get(),
        operands,
        successors,
        null,
        regions != null ? regions.size() : 0);
    }

    // Set the attributes if they were deserialized.
    if (attributes != null) {
      for (NamedAttribute attribute : attributes) {
        operation.setAttribute(attribute.getName(), attribute.getAttribute());
      }
    }

    // Take the deserialized regions and set their parent operation to this operation.
    if (regions != null) {
      for (int i = 0; i < regions.size(); i++) {
        operation.getRegions().get(i).setBodyValues(regions.get(i).getBodyValues());
        operation.getRegions().get(i).takeRegion(regions.get(i));
      }
    }

    return operation;
  }
}
