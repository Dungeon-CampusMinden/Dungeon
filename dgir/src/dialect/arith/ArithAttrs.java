package dialect.arith;

import static dialect.arith.ArithOps.BinaryOp;
import static dialect.builtin.BuiltinTypes.isNumeric;

import core.Dialect;
import core.ir.Attribute;
import dialect.builtin.BuiltinTypes;
import java.util.Optional;
import java.util.function.Function;
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

  final class UnaryModeAttr extends ArithAttribute implements ArithAttrs {
    @Override
    public @NotNull String getIdent() {
      return "arith.unaryMode";
    }

    @Override
    public @NotNull Object getStorage() {
      return unaryMode;
    }

    public enum UnaryMode {
      NEGATE,
      INCREMENT,
      DECREMENT,
      COMPLEMENT
    }

    private @NotNull UnaryMode unaryMode;

    public UnaryModeAttr() {
      this(UnaryMode.NEGATE);
    }

    public UnaryModeAttr(@NotNull UnaryMode unaryMode) {
      super();
      this.unaryMode = unaryMode;
    }

    public @NotNull UnaryMode getMode() {
      return unaryMode;
    }

    public void setMode(@NotNull UnaryMode unaryMode) {
      this.unaryMode = unaryMode;
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
      // Arithmetic
      /** Addition */
      ADD(BinMode::onlyNumericOperands),
      /** Subtraction */
      SUB(BinMode::onlyNumericOperands),
      /** Multiplication */
      MUL(BinMode::onlyNumericOperands),
      /** Division */
      DIV(BinMode::onlyNumericOperands),
      /** Remainder */
      MOD(BinMode::onlyNumericOperands),

      // Bitwise
      /** Bitwise OR */
      BOR(BinMode::onlyNumericOperands),
      /** Bitwise AND */
      BAND(BinMode::onlyNumericOperands),
      /** Bitwise XOR */
      BXOR(BinMode::onlyNumericOperands),
      /** Bitwise shift left */
      LSH(BinMode::onlyNumericOperands),
      /** Signed bitwise shift right */
      RSHS(BinMode::onlyNumericOperands),
      /** Unsigned bitwise shift right */
      RSHU(BinMode::onlyNumericOperands),

      // Logical
      AND(BinMode::onlyBooleanOperands),
      OR(BinMode::onlyBooleanOperands),
      XOR(BinMode::onlyBooleanOperands),
      LT(BinMode::onlyNumericOperands),
      LE(BinMode::onlyNumericOperands),
      GT(BinMode::onlyNumericOperands),
      GE(BinMode::onlyNumericOperands),

      EQ(BinMode::anyOperands),
      NE(BinMode::anyOperands);

      public final @NotNull Function<@NotNull BinaryOp, @NotNull Optional<String>> verifier;

      BinMode(@NotNull Function<@NotNull BinaryOp, @NotNull Optional<String>> verifier) {
        this.verifier = verifier;
      }

      static Optional<String> onlyNumericOperands(@NotNull BinaryOp binaryOp) {
        int widthLhs =
            switch (binaryOp.getLhs().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              case BuiltinTypes.FloatT floatT -> floatT.getWidth();
              default -> 0;
            };
        int widthRhs =
            switch (binaryOp.getRhs().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              case BuiltinTypes.FloatT floatT -> floatT.getWidth();
              default -> 0;
            };
        if (widthLhs <= 1) {
          return Optional.of("LHS must be a numeric type with a width greater than 1");
        }
        if (widthRhs <= 1) {
          return Optional.of("RHS must be a numeric type with a width greater than 1");
        }
        return Optional.empty();
      }

      static Optional<String> onlyBooleanOperands(@NotNull BinaryOp binaryOp) {
        int widthLhs =
            switch (binaryOp.getLhs().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              default -> 1;
            };
        int widthRhs =
            switch (binaryOp.getRhs().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              default -> 1;
            };
        if (widthLhs != 1) {
          return Optional.of("LHS must be a boolean type with a width of 1");
        }
        if (widthRhs != 1) {
          return Optional.of("RHS must be a boolean type with a width of 1");
        }
        return Optional.empty();
      }

      static Optional<String> anyOperands(@NotNull BinaryOp binaryOp) {
        if (!isNumeric(binaryOp.getResult().getType()) || !isNumeric(binaryOp.getRhs().getType())) {
          return Optional.of("Operands must be numeric");
        }
        return Optional.empty();
      }
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
}
