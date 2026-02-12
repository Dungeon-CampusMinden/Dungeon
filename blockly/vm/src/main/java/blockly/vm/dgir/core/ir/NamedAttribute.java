package blockly.vm.dgir.core.ir;

import blockly.vm.dgir.core.serialization.NamedAttributeDeserializer;
import blockly.vm.dgir.core.serialization.NamedAttributeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = NamedAttributeSerializer.class)
@JsonDeserialize(using = NamedAttributeDeserializer.class)
public final class NamedAttribute {
  private final String name;
  private Attribute attribute;

  @JsonCreator
  public NamedAttribute(@JsonProperty("name") String name,
                        @JsonProperty("attribute") Attribute attribute) {
    this.name = name;
    this.attribute = attribute;
  }

  public String getName() {
    return name;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }
}
