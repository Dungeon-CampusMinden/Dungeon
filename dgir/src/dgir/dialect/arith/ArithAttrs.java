package dgir.dialect.arith;

import dgir.core.Dialect;
import dgir.core.ir.Attribute;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

import static dgir.dialect.arith.ArithOps.*;
import static dgir.dialect.arith.ArithOps.BinaryOp;
import static dgir.dialect.builtin.BuiltinTypes.isNumeric;

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
      NEGATE(UnaryMode::onlyNumericOperand),
      INCREMENT(UnaryMode::onlyNumericOperand),
      DECREMENT(UnaryMode::onlyNumericOperand),
      COMPLEMENT(UnaryMode::onlyIntegerOperand),
      LOGICAL_COMPLEMENT(UnaryMode::onlyBooleanOperand),
      ;

      public final @NotNull Function<@NotNull UnaryOp, @NotNull Optional<String>> operandVerifier;

      UnaryMode(@NotNull Function<@NotNull UnaryOp, @NotNull Optional<String>> operandVerifier) {
        this.operandVerifier = operandVerifier;
      }

      public static Optional<String> onlyNumericOperand(@NotNull UnaryOp unaryOp) {
        int width =
            switch (unaryOp.getOperand().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              case BuiltinTypes.FloatT floatT -> floatT.getWidth();
              default -> 0;
            };
        if (width == 0) {
          return Optional.of("Operand must be a numeric type with a width greater than 0");
        }
        return Optional.empty();
      }

      public static Optional<String> onlyIntegerOperand(@NotNull UnaryOp unaryOp) {
        int width =
            switch (unaryOp.getOperand().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              default -> 0;
            };
        if (width == 0) {
          return Optional.of("Operand must be an integer type with a width greater than 0");
        }
        return Optional.empty();
      }

      public static Optional<String> onlyBooleanOperand(@NotNull UnaryOp unaryOp) {
        int width =
            switch (unaryOp.getOperand().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              default -> 1;
            };
        if (width != 1) {
          return Optional.of("Operand must be a boolean type with a width of 1");
        }
        return Optional.empty();
      }
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
      /** Signed Multiplication */
      MUL(BinMode::onlyNumericOperands),
      /** Division */
      DIV(BinMode::onlyNumericOperands),
      DIVUI(BinMode::onlyIntegerOperands),
      /** Remainder */
      MOD(BinMode::onlyNumericOperands),
      /** Unsigned Remainder */
      MODUI(BinMode::onlyIntegerOperands),

      // Bitwise
      /** Bitwise OR */
      BOR(BinMode::onlySameIntegerOperands),
      /** Bitwise AND */
      BAND(BinMode::onlySameIntegerOperands),
      /** Bitwise XOR */
      BXOR(BinMode::onlySameIntegerOperands),
      /** Bitwise shift left */
      LSH(BinMode::onlyIntegerOperands),
      /** Signed bitwise shift right */
      RSHS(BinMode::onlyIntegerOperands),
      /** Unsigned bitwise shift right */
      RSHU(BinMode::onlyIntegerOperands),

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

      public final @NotNull Function<@NotNull BinaryOp, @NotNull Optional<String>> operandsVerifier;

      BinMode(@NotNull Function<@NotNull BinaryOp, @NotNull Optional<String>> operandsVerifier) {
        this.operandsVerifier = operandsVerifier;
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
        if (widthLhs == 0) {
          return Optional.of("LHS must be a numeric type with a width greater than 0");
        }
        if (widthRhs == 0) {
          return Optional.of("RHS must be a numeric type with a width greater than 0");
        }
        return Optional.empty();
      }

      static Optional<String> onlyIntegerOperands(@NotNull BinaryOp binaryOp) {
        int widthLhs =
            switch (binaryOp.getLhs().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              default -> 0;
            };
        int widthRhs =
            switch (binaryOp.getRhs().getType()) {
              case BuiltinTypes.IntegerT integerT -> integerT.getWidth();
              default -> 0;
            };
        if (widthLhs == 0) {
          return Optional.of("LHS must be an integer type with a width greater than 0");
        }
        if (widthRhs == 0) {
          return Optional.of("RHS must be an integer type with a width greater than 0");
        }
        return Optional.empty();
      }

      static Optional<String> onlySameIntegerOperands(@NotNull BinaryOp binaryOp) {
        Optional<String> error = onlyIntegerOperands(binaryOp);
        if (error.isPresent()) return error;

        if (!binaryOp.getResult().getType().equals(binaryOp.getLhs().getType()))
          return Optional.of("Operands must be of the same type");
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
