package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.*;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.ReadableObjectId;
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

    // Create the object ID generator for looking up Value references.
    // The generator is scoped to Value.class to match the @JsonIdentityInfo on Value
    ObjectIdGenerator<?> idGenerator = new ObjectIdGenerators.UUIDGenerator().forScope(Value.class);
    // Create a simple resolver for object IDs
    SimpleObjectIdResolver idResolver = new SimpleObjectIdResolver();

    // Deserialize regions if they exist.
    // This has to be done before deserializing operands since an operand can reference a body value inside a region.
    List<Region> regions = null;
    if (node.has("regions")) {
      regions = new ArrayList<>();
      for (JsonNode regionNode : node.get("regions")) {
        Region region = ctxt.readTreeAsValue(regionNode, Region.class);
        regions.add(region);
      }
    }

    /* Deserialize the operands from the node, these are serialized as a list of value references.
    e.g.
    "operands" : [ {
                "value" : "187b5131-0518-4b67-9aa0-59b8679f53c7"
              } ]
     */
    List<Value> operands = null;
    if (node.has("operands")) {
      operands = new ArrayList<>();
      for (JsonNode operandNode : node.get("operands")) {
        String valueId = operandNode.get("value").asString();
        // Convert the string UUID to the proper key type used by the generator
        Object idKey = idGenerator.key(java.util.UUID.fromString(valueId));
        // Find or create the object ID entry - this will handle forward/backward references
        ReadableObjectId readableId = ctxt.findObjectId(idKey, idGenerator, idResolver);
        Value value = (Value) readableId.resolve();
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
    // Since other operations can have references to the output value, we need to create a new Value object for the output
    // and register it with the DeserializationContext.
    Type outputType = null;
    String outputValueId = null;
    if (node.has("output")) {
      JsonNode outputNode = node.get("output");
      // This output is a reference to an existing value
      if (!outputNode.has("@id"))
        outputValueId = outputNode.asString();
      else {
        outputValueId = outputNode.get("@id").asString();
        outputType = ctxt.readTreeAsValue(outputNode.get("type"), Type.class);
      }
    }
    // TODO deserialize block operands (successors).

    Operation operation = null;
    // In case we do have the output value id but no type we must resolve the reference and get the type from the value.
    // Afterward we set the value on the operation so that it points to the correct value
    if (outputType == null && outputValueId != null) {
      // Convert the string UUID to the proper key type used by the generator
      Object idKey = idGenerator.key(java.util.UUID.fromString(outputValueId));
      // Find or create the object ID entry - this will handle forward/backward references
      ReadableObjectId readableId = ctxt.findObjectId(idKey, idGenerator, idResolver);
      Value value = (Value) readableId.resolve();

      // Create the operation instance with the resolved output value type and set the output value on the operation.
      operation = Operation.Create(
        operationDetails.get(),
        operands,
        null,
        value.getType(),
        regions != null ? regions.size() : 0);
      operation.setOutputValue(value);
    } else {
      // Create the operation instance.
      operation = Operation.Create(
        operationDetails.get(),
        operands,
        null,
        outputType,
        regions != null ? regions.size() : 0);
    }

    // Set the attributes if they were deserialized.
    if (attributes != null) {
      for (NamedAttribute attribute : attributes) {
        operation.setAttribute(attribute.getName(), attribute.getAttribute());
      }
    }

    // if the output was deserialized, get the Value object from the operation and register it.
    if (outputType != null && outputValueId != null) {
      Value outputValue = operation.getOutputValue();
      // Convert the string UUID to the proper key type used by the generator
      Object idKey = idGenerator.key(java.util.UUID.fromString(outputValueId));
      ReadableObjectId id = ctxt.findObjectId(idKey, idGenerator, idResolver);
      id.bindItem(ctxt, outputValue);
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
