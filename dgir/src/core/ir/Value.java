package core.ir;

import com.fasterxml.jackson.annotation.*;
import core.IRObjectWithUseList;
import core.serialization.ValueIdGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * A dynamic value produced by an {@link Operation} or introduced as a block/region argument. Values
 * carry a {@link Type} and maintain a use-list of all {@link ValueOperand}s that reference them.
 */
@JsonIdentityInfo(generator = ValueIdGenerator.class)
public final class Value extends IRObjectWithUseList<Value, ValueOperand> implements Serializable {

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull Type type;
  private @NotNull Location definitionLocation = Location.UNKNOWN;

  // =========================================================================
  // Constructors
  // =========================================================================

  public Value(@NotNull Type type) {
    this.type = type;
  }

  @JsonCreator
  public Value(
      @JsonProperty("type") @NotNull Type type, @JsonProperty("loc") @Nullable Location location) {
    this.type = type;
    if (location != null) {
      this.definitionLocation = location;
    }
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull Type getType() {
    return type;
  }

  @Contract(pure = true)
  @JsonProperty("loc")
  @JsonInclude(NON_NULL)
  @Nullable
  Location getLocationIfKnown() {
    return definitionLocation.equals(Location.UNKNOWN) ? null : definitionLocation;
  }

  /**
   * The location of the definition of this value. It marks the first definition of the value in the
   * original source. This is used for error reporting and debugging, and may be {@link
   * Location#UNKNOWN} if the value was created without a known source location.
   *
   * @return the location of the definition of this value.
   */
  @Contract(pure = true)
  @JsonIgnore
  public @NotNull Location getLocation() {
    return definitionLocation;
  }

  /**
   * Set the location of the definition of this value.
   *
   * @param location the new location.
   */
  public void setLocation(@NotNull Location location) {
    this.definitionLocation = location;
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public String toString() {
    return type.toString();
  }
}
