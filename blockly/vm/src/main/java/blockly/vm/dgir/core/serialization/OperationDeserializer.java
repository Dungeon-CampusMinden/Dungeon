package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.detail.RegisteredOperationDetails;
import blockly.vm.dgir.core.ir.*;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationDeserializer extends StdDeserializer<Operation> {
  private static final Map<Operation, Map<BlockOperand, JsonNode>> unresolvedSuccessorReferences = new HashMap<>();

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
        Value value = ctxt.readTreeAsValue(operandNode.get("value"), Value.class);
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

    // Deserialize successors if they exist.
    // Since successors are mostly forward references we will perform a 2 step resolve of their IDs. For that we store
    // all the unresolved references in a static lookup map and try to resolve all the references once we deserialized
    // all the child regions, as those are were the operations with the unresolved references reside.
    List<Block> successors = null;
    Map<Block, JsonNode> unresolvedSuccessors = new HashMap<>();
    if (node.has("successors")) {
      successors = new ArrayList<>();
      for (JsonNode successorNode : node.get("successors")) {
        Block placeHolderBlock = new Block();
        successors.add(placeHolderBlock);
        unresolvedSuccessors.put(placeHolderBlock, successorNode.get("value"));
        // The references themselves are connected to their BlockOperands after the operation itself is created and added
        // to the global list of unresolved references for further processing.
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

    // Now that the regions are deserialized, we can go over all their operations and resolve their block operands.
    // Note!: This step does not resolve the block operands of this operation, as those are part of the parent region and
    // not the ones we just deserialized. In fact, this operation is the result of another region deserialization
    if (regions != null) {
      // Go over all regions and their blocks and operations and resolve the block operands.
      for (Region region : regions) {
        for (Block block : region.getBlocks()) {
          for (Operation operation : block.getOperations()) {
            // Check if we have any unresolved references for this operation.
            Map<BlockOperand, JsonNode> unresolvedReferences = unresolvedSuccessorReferences.get(operation);
            if (unresolvedReferences != null) {
              // Go over all unresolved references and resolve them.
              for (Map.Entry<BlockOperand, JsonNode> entry : unresolvedReferences.entrySet()) {
                BlockOperand blockOperand = entry.getKey();
                JsonNode blockId = entry.getValue();
                // Now we just use jackson to resolve the reference
                Block targetBlock = ctxt.readTreeAsValue(blockId, Block.class);
                blockOperand.setValue(targetBlock);
              }
            }
          }
        }
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

    // Connect the unresolved successors and connect them to their corresponding BlockOperands.
    Map<BlockOperand, JsonNode> unresolvedBlockOperands = new HashMap<>();
    for (BlockOperand blockOperand : operation.getBlockOperands()) {
      // This gives us a direct lookup from the BlockOperand to the ID of the Block it must resolve to.
      // That way we can can update the block operands directly, without having to access the operation itself.
      unresolvedBlockOperands.put(blockOperand, unresolvedSuccessors.get(blockOperand.getValue()));
    }
    if (!unresolvedBlockOperands.isEmpty())
      unresolvedSuccessorReferences.put(operation, unresolvedBlockOperands);

    return operation;
  }
}
