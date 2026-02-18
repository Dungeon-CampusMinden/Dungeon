package core.ir;

import org.jetbrains.annotations.NotNull;

public abstract class TypedAttribute extends Attribute {
  private final @NotNull Type type;

  protected TypedAttribute(@NotNull Type type) {
    this.type = type;
  }

  public @NotNull Type getType() {
    return type;
  }
}
