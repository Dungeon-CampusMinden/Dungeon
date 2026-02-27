package dialect.arith.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class CompModeAttr extends ArithAttribute {
  @Override
  public @NotNull String getIdent() {
    return "arith.compMode";
  }

  @Override
  public @NotNull Object getStorage() {
    return mode;
  }

  public enum CompMode {
    EQ, // Equal
    NE, // Not equal
    LT, // Less than
    LE, // Less than or equal
    GT, // Greater than
    GE // Greater than or equal
  }

  private final @NotNull CompMode mode;

  public CompModeAttr() {
    this(CompMode.EQ);
  }

  @JsonCreator
  public CompModeAttr(@JsonProperty("mode") @NotNull CompMode mode) {
    super();
    this.mode = mode;
  }

  public @NotNull CompMode getMode() {
    return mode;
  }
}
