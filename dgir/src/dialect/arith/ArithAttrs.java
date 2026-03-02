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
      return binMode;
    }

    public enum BinMode {
      ADD,
      SUB,
      MUL,
      DIV,
      MOD
    }

    private @NotNull BinMode binMode;

    public BinModeAttr() {
      this(BinMode.ADD);
    }

    public BinModeAttr(@NotNull BinMode binMode) {
      super();
      this.binMode = binMode;
    }

    public @NotNull BinMode getMode() {
      return binMode;
    }

    public void setMode(@NotNull BinMode binMode) {
      this.binMode = binMode;
    }
  }

  final class CompModeAttr extends ArithAttribute implements ArithAttrs {
    @Override
    public @NotNull String getIdent() {
      return "arith.compMode";
    }

    @Override
    public @NotNull Object getStorage() {
      return compMode;
    }

    public enum CompMode {
      EQ, // Equal
      NE, // Not equal
      LT, // Less than
      LE, // Less than or equal
      GT, // Greater than
      GE // Greater than or equal
    }

    private @NotNull CompMode compMode;

    public CompModeAttr() {
      this(CompMode.EQ);
    }

    public CompModeAttr(@NotNull CompMode compMode) {
      super();
      this.compMode = compMode;
    }

    public @NotNull CompMode getMode() {
      return compMode;
    }

    public void setMode(@NotNull CompMode compMode) {
      this.compMode = compMode;
    }
  }
}
