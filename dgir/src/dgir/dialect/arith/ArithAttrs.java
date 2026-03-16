package dgir.dialect.arith;

import dgir.core.Dialect;
import dgir.core.ir.Attribute;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
      NEGATE {
        @Override
        public @NotNull Optional<String> verifyOperand(@NotNull UnaryOp unaryOp) {
          return UnaryMode.onlyNumericOperand(unaryOp);
        }
      },
      INCREMENT {
        @Override
        public @NotNull Optional<String> verifyOperand(@NotNull UnaryOp unaryOp) {
          return UnaryMode.onlyNumericOperand(unaryOp);
        }
      },
      DECREMENT {
        @Override
        public @NotNull Optional<String> verifyOperand(@NotNull UnaryOp unaryOp) {
          return UnaryMode.onlyNumericOperand(unaryOp);
        }
      },
      COMPLEMENT {
        @Override
        public @NotNull Optional<String> verifyOperand(@NotNull UnaryOp unaryOp) {
          return UnaryMode.onlyIntegerOperand(unaryOp);
        }
      },
      LOGICAL_COMPLEMENT {
        @Override
        public @NotNull Optional<String> verifyOperand(@NotNull UnaryOp unaryOp) {
          return UnaryMode.onlyBooleanOperand(unaryOp);
        }
      },
      ;

      public abstract @NotNull Optional<String> verifyOperand(@NotNull UnaryOp unaryOp);

      private static Optional<String> onlyNumericOperand(@NotNull UnaryOp unaryOp) {
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

      private static Optional<String> onlyIntegerOperand(@NotNull UnaryOp unaryOp) {
        int width =
            unaryOp.getOperand().getType() instanceof BuiltinTypes.IntegerT integerT
                ? integerT.getWidth()
                : 0;
        if (width == 0) {
          return Optional.of("Operand must be an integer type with a width greater than 0");
        }
        return Optional.empty();
      }

      private static Optional<String> onlyBooleanOperand(@NotNull UnaryOp unaryOp) {
        int width =
            unaryOp.getOperand().getType() instanceof BuiltinTypes.IntegerT integerT
                ? integerT.getWidth()
                : 0;
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
      ADD {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      /** Subtraction */
      SUB {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      /** Signed Multiplication */
      MUL {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      /** Division */
      DIV {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      DIVUI {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyIntegerOperands(unaryOp);
        }
      },
      /** Remainder */
      MOD {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      /** Unsigned Remainder */
      MODUI {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyIntegerOperands(unaryOp);
        }
      },

      // Bitwise
      /** Bitwise OR */
      BOR {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlySameIntegerOperands(unaryOp);
        }
      },
      /** Bitwise AND */
      BAND {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlySameIntegerOperands(unaryOp);
        }
      },
      /** Bitwise XOR */
      BXOR {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlySameIntegerOperands(unaryOp);
        }
      },
      /** Bitwise shift left */
      LSH {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyIntegerOperands(unaryOp);
        }
      },
      /** Signed bitwise shift right */
      RSHS {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyIntegerOperands(unaryOp);
        }
      },
      /** Unsigned bitwise shift right */
      RSHU {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyIntegerOperands(unaryOp);
        }
      },

      // Logical
      AND {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyBooleanOperands(unaryOp);
        }
      },
      OR {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyBooleanOperands(unaryOp);
        }
      },
      XOR {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyBooleanOperands(unaryOp);
        }
      },
      LT {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      LE {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      GT {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },
      GE {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.onlyNumericOperands(unaryOp);
        }
      },

      EQ {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.anyOperands(unaryOp);
        }
      },
      NE {
        @Override
        public @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp) {
          return BinMode.anyOperands(unaryOp);
        }
      };

      public abstract @NotNull Optional<String> verifyOperands(@NotNull BinaryOp unaryOp);

      private static Optional<String> onlyNumericOperands(@NotNull BinaryOp binaryOp) {
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

      private static Optional<String> onlyIntegerOperands(@NotNull BinaryOp binaryOp) {
        int widthLhs =
            binaryOp.getLhs().getType() instanceof BuiltinTypes.IntegerT integerT
                ? integerT.getWidth()
                : 0;
        int widthRhs =
            binaryOp.getRhs().getType() instanceof BuiltinTypes.IntegerT integerT
                ? integerT.getWidth()
                : 0;
        if (widthLhs == 0) {
          return Optional.of("LHS must be an integer type with a width greater than 0");
        }
        if (widthRhs == 0) {
          return Optional.of("RHS must be an integer type with a width greater than 0");
        }
        return Optional.empty();
      }

      private static Optional<String> onlySameIntegerOperands(@NotNull BinaryOp binaryOp) {
        Optional<String> error = onlyIntegerOperands(binaryOp);
        if (error.isPresent()) return error;

        if (!binaryOp.getResult().getType().equals(binaryOp.getLhs().getType()))
          return Optional.of("Operands must be of the same type");
        return Optional.empty();
      }

      private static Optional<String> onlyBooleanOperands(@NotNull BinaryOp binaryOp) {
        int widthLhs =
            binaryOp.getLhs().getType() instanceof BuiltinTypes.IntegerT integerT
                ? integerT.getWidth()
                : 0;
        int widthRhs =
            binaryOp.getRhs().getType() instanceof BuiltinTypes.IntegerT integerT
                ? integerT.getWidth()
                : 0;
        if (widthLhs != 1) {
          return Optional.of("LHS must be a boolean type with a width of 1");
        }
        if (widthRhs != 1) {
          return Optional.of("RHS must be a boolean type with a width of 1");
        }
        return Optional.empty();
      }

      private static Optional<String> anyOperands(@NotNull BinaryOp binaryOp) {
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
