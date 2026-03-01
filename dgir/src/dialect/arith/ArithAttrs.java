package dialect.arith;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    private final @NotNull ArithAttrs.BinModeAttr.Mode mode;

    public BinModeAttr() {
      this(Mode.ADD);
    }

    @JsonCreator
    public BinModeAttr(@JsonProperty("mode") @NotNull ArithAttrs.BinModeAttr.Mode mode) {
      super();
      this.mode = mode;
    }

    public @NotNull ArithAttrs.BinModeAttr.Mode getMode() {
      return mode;
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

    private final @NotNull Mode mode;

    public CompModeAttr() {
      this(Mode.EQ);
    }

    @JsonCreator
    public CompModeAttr(@JsonProperty("mode") @NotNull Mode mode) {
      super();
      this.mode = mode;
    }

    public @NotNull Mode getMode() {
      return mode;
    }
  }
}
