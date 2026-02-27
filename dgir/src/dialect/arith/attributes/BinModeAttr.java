package dialect.arith.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class BinModeAttr extends ArithAttribute {
  @Override
  public @NotNull String getIdent() {
    return "arith.binMode";
  }

  @Override
  public @NotNull Object getStorage() {
    return mode;
  }

  public enum BinMode {
    ADD,
    SUB,
    MUL,
    DIV,
    MOD
  }

  private final @NotNull BinMode mode;

  public BinModeAttr() {
    this(BinMode.ADD);
  }

  @JsonCreator
  public BinModeAttr(@JsonProperty("mode") @NotNull BinMode mode) {
    super();
    this.mode = mode;
  }

  public @NotNull BinMode getMode() {
    return mode;
  }
}

