package dgir.core.debug;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public record ValueDebugInfo(@JsonProperty("loc") @NotNull Location location, @NotNull String name) {
  public static final ValueDebugInfo UNKNOWN = new ValueDebugInfo(Location.UNKNOWN, "<unknown>");
}
