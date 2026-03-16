package dgir.core.serialization;

import dgir.core.debug.Location;
import dgir.core.ir.*;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserializes an {@link Operation} from its JSON object form.
 *
 * <p>Required field: {@code ident}. Optional fields ({@code operands}, {@code attributes}, {@code
 * output}, {@code successors}, {@code regions}, {@code loc}) are validated when present. Malformed
 * input is reported via {@link DeserializationContext#reportInputMismatch}.
 */
public class OperationDeserializer extends StdDeserializer<Operation> {
  /**
   * Stores unresolved successor references until all nested regions/blocks/operations are available
   * for identity resolution.
   */
  private static final Map<Operation, Map<BlockOperand, JsonNode>> unresolvedSuccessorReferences =
      new HashMap<>();

  public OperationDeserializer() {
    this(Operation.class);
  }

  public OperationDeserializer(Class<?> vc) {
    super(vc);
  }

  /** Deserialize a full operation payload, then resolve successor references in a second step. */
  @Override
  public Operation deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
    JsonNode node = jp.readValueAsTree();

    // Resolve operation kind from ident before touching optional fields.
    JsonNode identNode = node.get("ident");
    if (identNode == null || identNode.isNull()) {
      return ctxt.reportInputMismatch(Operation.class, "Missing required field 'ident'.");
    }
    if (!identNode.isString()) {
      return ctxt.reportInputMismatch(Operation.class, "Field 'ident' must be a string.");
    }

    String ident = identNode.asString();
    var operationDetails = OperationDetails.Registered.lookup(ident);
    if (operationDetails.isEmpty()) {
      return ctxt.reportInputMismatch(
          Operation.class,
          "Operation '%s' must be registered to deserialize. Load its dialect first.",
          ident);
    }

    List<Value> operands = null;
    JsonNode operandsNode = node.get("operands");
    if (operandsNode != null && !operandsNode.isNull()) {
      if (!operandsNode.isArray()) {
        return ctxt.reportInputMismatch(Operation.class, "Field 'operands' must be an array.");
      }
      operands = new ArrayList<>();
      for (JsonNode operandNode : operandsNode) {
        // Operand entries are wrappers of the form {"value": <value-ref>}.
        JsonNode valueRef = operandNode.get("value");
        if (valueRef == null || valueRef.isNull()) {
          return ctxt.reportInputMismatch(
              Operation.class, "Each operand entry must contain field 'value'.");
        }
        Value value = ctxt.readTreeAsValue(valueRef, Value.class);
        operands.add(value);
      }
    }

    List<NamedAttribute> attributes = null;
    JsonNode attributesNode = node.get("attributes");
    if (attributesNode != null && !attributesNode.isNull()) {
      if (!attributesNode.isArray()) {
        return ctxt.reportInputMismatch(Operation.class, "Field 'attributes' must be an array.");
      }
      attributes = new ArrayList<>();
      for (JsonNode attributeNode : attributesNode) {
        NamedAttribute attribute = ctxt.readTreeAsValue(attributeNode, NamedAttribute.class);
        attributes.add(attribute);
      }
    }

    Value outputValue = null;
    JsonNode outputNode = node.get("output");
    if (outputNode != null && !outputNode.isNull()) {
      outputValue = ctxt.readTreeAsValue(outputNode, Value.class);
    }

    List<Block> successors = null;
    Map<Block, JsonNode> unresolvedSuccessors = new HashMap<>();
    JsonNode successorsNode = node.get("successors");
    if (successorsNode != null && !successorsNode.isNull()) {
      if (!successorsNode.isArray()) {
        return ctxt.reportInputMismatch(Operation.class, "Field 'successors' must be an array.");
      }
      successors = new ArrayList<>();
      for (JsonNode successorNode : successorsNode) {
        JsonNode valueRef = successorNode.get("value");
        if (valueRef == null || valueRef.isNull()) {
          return ctxt.reportInputMismatch(
              Operation.class, "Each successor entry must contain field 'value'.");
        }
        // Use placeholders first; resolve to real blocks once all child regions are read.
        Block placeHolderBlock = new Block();
        successors.add(placeHolderBlock);
        unresolvedSuccessors.put(placeHolderBlock, valueRef);
      }
    }

    List<Region> regions = null;
    JsonNode regionsNode = node.get("regions");
    if (regionsNode != null && !regionsNode.isNull()) {
      if (!regionsNode.isArray()) {
        return ctxt.reportInputMismatch(Operation.class, "Field 'regions' must be an array.");
      }
      regions = new ArrayList<>();
      for (JsonNode regionNode : regionsNode) {
        Region region = ctxt.readTreeAsValue(regionNode, Region.class);
        regions.add(region);
      }
    }

    Location location = Location.UNKNOWN;
    JsonNode locNode = node.get("loc");
    if (locNode != null && !locNode.isNull()) {
      location = ctxt.readTreeAsValue(locNode, Location.class);
    }

    if (regions != null) {
      for (Region region : regions) {
        for (Block block : region.getBlocks()) {
          for (Operation operation : block.getOperations()) {
            Map<BlockOperand, JsonNode> unresolvedReferences =
                unresolvedSuccessorReferences.get(operation);
            if (unresolvedReferences != null) {
              for (Map.Entry<BlockOperand, JsonNode> entry : unresolvedReferences.entrySet()) {
                BlockOperand blockOperand = entry.getKey();
                JsonNode blockId = entry.getValue();
                if (blockId == null || blockId.isNull()) {
                  return ctxt.reportInputMismatch(
                      Operation.class, "Encountered unresolved successor block reference.");
                }
                // Let Jackson resolve block identity references after region materialization.
                Block targetBlock = ctxt.readTreeAsValue(blockId, Block.class);
                blockOperand.setValue(targetBlock);
              }
            }
          }
        }
      }
    }

    Op op = operationDetails.get().createDefaultInstance();
    Operation operation;
    if (outputValue != null) {
      operation =
          Operation.Create(
              location,
              op,
              operands,
              successors,
              outputValue.getType(),
              regions != null ? regions.size() : 0);
      operation.setOutputValue(outputValue);
    } else {
      operation =
          Operation.Create(
              location, op, operands, successors, null, regions != null ? regions.size() : 0);
    }

    if (attributes != null) {
      for (NamedAttribute attribute : attributes) {
        operation.setAttribute(attribute.getName(), attribute.getAttribute());
      }
    }

    if (regions != null) {
      for (int i = 0; i < regions.size(); i++) {
        operation.getRegions().get(i).setBodyValues(regions.get(i).getBodyValues());
        operation.getRegions().get(i).takeRegion(regions.get(i));
      }
    }

    Map<BlockOperand, JsonNode> unresolvedBlockOperands = new HashMap<>();
    for (BlockOperand blockOperand : operation.getBlockOperands()) {
      Block placeholder = blockOperand.getValue().orElse(null);
      if (placeholder == null) {
        return ctxt.reportInputMismatch(
            Operation.class, "Encountered unset successor placeholder.");
      }
      JsonNode unresolvedId = unresolvedSuccessors.get(placeholder);
      if (unresolvedId == null || unresolvedId.isNull()) {
        return ctxt.reportInputMismatch(
            Operation.class,
            "Missing unresolved successor id for block operand in operation '%s'.",
            ident);
      }
      // Keep unresolved ids so parent-region deserialization can bind forward edges later.
      unresolvedBlockOperands.put(blockOperand, unresolvedId);
    }
    if (!unresolvedBlockOperands.isEmpty()) {
      unresolvedSuccessorReferences.put(operation, unresolvedBlockOperands);
    }

    return operation;
  }
}
