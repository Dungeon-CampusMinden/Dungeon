package core.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.serialization.NamedAttributeDeserializer;
import core.serialization.NamedAttributeSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/** An {@link Attribute} paired with its name, as stored inside an {@link Operation}. */
@JsonSerialize(using = NamedAttributeSerializer.class)
@JsonDeserialize(using = NamedAttributeDeserializer.class)
public final class NamedAttribute {

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull String name;
  private @NotNull Attribute attribute;

  // =========================================================================
  // Constructors
  // =========================================================================

  @JsonCreator
  public NamedAttribute(
      @JsonProperty("name") @NotNull String name,
      @JsonProperty("attribute") @NotNull Attribute attribute) {
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
  public @NotNull Attribute getAttribute() {
    return attribute;
  }

  /**
   * Replace the attribute value stored in this named attribute.
   *
   * @param attribute the new attribute value; must not be {@code null}.
   */
  public void setAttribute(@NotNull Attribute attribute) {
    this.attribute = attribute;
  }
}
