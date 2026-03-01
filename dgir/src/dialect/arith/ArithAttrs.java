package dialect.arith;

import core.Dialect;
import core.ir.Attribute;
import org.jetbrains.annotations.NotNull;

public sealed interface ArithAttrs {
  abstract class ArithAttribute extends Attribute {
    @Override
    public @NotNull String getNamespace() {
      return "arith";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return ArithDialect.class;
    }

    protected ArithAttribute() {
      super();
    }
  }

  final class BinModeAttr extends ArithAttribute implements ArithAttrs {
    @Override
    public @NotNull String getIdent() {
      return "arith.binMode";
    }

    @Override
    public @NotNull Object getStorage() {
      return mode;
    }

    public enum Mode {
      ADD,
      SUB,
      MUL,
      DIV,
      MOD
    }

    private @NotNull BinModeAttr.Mode mode;

    public BinModeAttr() {
      this(Mode.ADD);
    }

    public BinModeAttr(@NotNull BinModeAttr.Mode mode) {
      super();
      this.mode = mode;
    }

    public @NotNull BinModeAttr.Mode getMode() {
      return mode;
    }

    public void setMode(@NotNull Mode mode) {
      this.mode = mode;
    }
  }

  final class CompModeAttr extends ArithAttribute implements ArithAttrs {
    @Override
    public @NotNull String getIdent() {
      return "arith.compMode";
    }

    @Override
    public @NotNull Object getStorage() {
      return mode;
    }

    public enum Mode {
      EQ, // Equal
      NE, // Not equal
      LT, // Less than
      LE, // Less than or equal
      GT, // Greater than
      GE // Greater than or equal
    }

    private @NotNull Mode mode;

    public CompModeAttr() {
      this(Mode.EQ);
    }

    public CompModeAttr(@NotNull Mode mode) {
      super();
      this.mode = mode;
    }

    public @NotNull Mode getMode() {
      return mode;
    }

    public void setMode(@NotNull Mode mode) {
      this.mode = mode;
    }
  }
}
