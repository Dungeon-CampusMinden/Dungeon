package core.ir;

import core.serialization.NamedAttributeDeserializer;
import core.serialization.NamedAttributeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Optional;

@JsonSerialize(using = NamedAttributeSerializer.class)
@JsonDeserialize(using = NamedAttributeDeserializer.class)
public final class NamedAttribute {
  private final @NotNull String name;
  private @Nullable Attribute attribute;

  @JsonCreator
  public NamedAttribute(@JsonProperty("name") @NotNull String name,
                        @JsonProperty("attribute") @Nullable Attribute attribute) {
    this.name = name;
    this.attribute = attribute;
  }

  public @NotNull String getName() {
    return name;
  }

  public @NotNull Optional<Attribute> getAttribute() {
    return Optional.ofNullable(attribute);
  }

  public void setAttribute(@NotNull Attribute attribute) {
    this.attribute = attribute;
  }
}
