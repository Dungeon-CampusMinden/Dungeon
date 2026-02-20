package core.ir;

import core.serialization.NamedAttributeDeserializer;
import core.serialization.NamedAttributeSerializer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Optional;

/**
 * An {@link Attribute} paired with its name, as stored inside an {@link Operation}.
 */
@JsonSerialize(using = NamedAttributeSerializer.class)
@JsonDeserialize(using = NamedAttributeDeserializer.class)
public final class NamedAttribute {

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull String name;
  private @Nullable Attribute attribute;

  // =========================================================================
  // Constructors
  // =========================================================================

  @JsonCreator
  public NamedAttribute(@JsonProperty("name") @NotNull String name,
                        @JsonProperty("attribute") @Nullable Attribute attribute) {
    this.name = name;
    this.attribute = attribute;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull String getName() {
    return name;
  }

  @Contract(pure = true)
  public @NotNull Optional<Attribute> getAttribute() {
    return Optional.ofNullable(attribute);
  }

  public void setAttribute(@NotNull Attribute attribute) {
    this.attribute = attribute;
  }
}
