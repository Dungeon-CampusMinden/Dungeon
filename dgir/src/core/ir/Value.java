package core.ir;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.*;
import core.IRObjectWithUseList;
import core.debug.Location;
import core.debug.ValueDebugInfo;
import core.serialization.ValueIdGenerator;
import java.io.Serializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic value produced by an {@link Operation} or introduced as a block/region argument. Values
 * carry a {@link Type} and maintain a use-list of all {@link ValueOperand}s that reference them.
 *
 * <p>Each value also carries optional debug metadata via {@link ValueDebugInfo}. This metadata
 * includes a source {@link Location} and a user-facing name (e.g. a variable name) used by
 * debugging tools. When the debug info is unknown, it is treated as absent and omitted from
 * serialization.
 */
@JsonIdentityInfo(generator = ValueIdGenerator.class)
public final class Value extends IRObjectWithUseList<Value, ValueOperand> implements Serializable {

  // =========================================================================
  // Members
  // =========================================================================

  /**
   * The type of this value. This is immutable and must be provided at construction. It defines the
   * kind of data this value represents (e.g. integer, float, pointer) and is used for type
   * checking.
   */
  private final @NotNull Type type;

  /**
   * The debug information for this value. By default, this is {@link ValueDebugInfo#UNKNOWN}, which
   * indicates that the value's location and name are not known. This can be updated later when the
   * value is created or when more information becomes available. The debug info is not serialized
   * if it is UNKNOWN, to save space and indicate that the value's debug information is not known.
   */
  private @NotNull ValueDebugInfo debugInfo = ValueDebugInfo.UNKNOWN;

  // =========================================================================
  // Constructors
  // =========================================================================

  public Value(@NotNull Type type) {
    this.type = type;
  }

  @JsonCreator
  public Value(
      @JsonProperty("type") @NotNull Type type,
      @JsonProperty("debug") @Nullable ValueDebugInfo debugInfo) {
    this.type = type;
    if (debugInfo != null) {
      this.debugInfo = debugInfo;
    }
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /** Returns the static type of this value. */
  @Contract(pure = true)
  public @NotNull Type getType() {
    return type;
  }

  /**
   * Returns debug info only when known; otherwise returns {@code null} for compact serialization.
   */
  @Contract(pure = true)
  @JsonProperty("debug")
  @JsonInclude(NON_NULL)
  @Nullable
  ValueDebugInfo getDebugInfoIfKnown() {
    return debugInfo.equals(ValueDebugInfo.UNKNOWN) ? null : debugInfo;
  }

  /** Returns the debug info, or {@link ValueDebugInfo#UNKNOWN} when not set. */
  @Contract(pure = true)
  @JsonIgnore
  public @NotNull ValueDebugInfo getDebugInfo() {
    return debugInfo;
  }

  /** Replace the entire debug info object. */
  public void setDebugInfo(@NotNull ValueDebugInfo debugInfo) {
    this.debugInfo = debugInfo;
  }

  /** Returns the source location for this value, or {@link Location#UNKNOWN}. */
  @Contract(pure = true)
  @JsonIgnore
  public Location getLocation() {
    return getDebugInfo().location();
  }

  /** Updates the source location while preserving the current debug name. */
  public void setLocation(@NotNull Location location) {
    debugInfo = new ValueDebugInfo(location, debugInfo.name());
  }

  /** Returns the user-facing debug name, or an empty string when unknown. */
  @Contract(pure = true)
  @JsonIgnore
  public String getName() {
    return getDebugInfo().name();
  }

  /** Updates the debug name while preserving the current source location. */
  public void setName(@NotNull String name) {
    debugInfo = new ValueDebugInfo(debugInfo.location(), name);
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public String toString() {
    return debugInfo.name() + ": " + type;
  }
}
