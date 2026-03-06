package dgir.dialect.arith;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.debug.Location;
import dgir.core.ir.*;
import dgir.core.traits.IBinaryOperands;
import dgir.core.traits.IHasResult;
import dgir.core.traits.INoOperands;
import dgir.core.traits.ISingleOperand;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dgir.dialect.arith.ArithAttrs.BinModeAttr;
import static dgir.dialect.arith.ArithAttrs.BinModeAttr.BinMode;
import static dgir.dialect.builtin.BuiltinAttrs.*;
import static dgir.dialect.builtin.BuiltinTypes.*;

/**
 * Sealed marker interface for all operations in the {@link ArithDialect}.
 *
 * <p>Every concrete op must both extend {@link ArithOp} and implement this interface so that {@link
 * Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface ArithOps {
  /**
   * Abstract base class for all operations in the {@code arith} (arithmetic) dialect.
   *
   * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
   * implement {@link ArithOps} to be enumerated by {@link ArithDialect}.
   */
  abstract class ArithOp extends Op {

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    ArithOp() {
      super();
    }

    // =========================================================================
    // Op Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return ArithDialect.class;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getNamespace() {
      return "arith";
    }
  }

  abstract class UnaryNumericOp extends ArithOp implements ISingleOperand {
    /** Default constructor used during dialect registration. */
    UnaryNumericOp() {
      super();
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return UnaryNumericOp::verifyUnaryNumericOperand;
    }

    protected static boolean verifyUnaryNumericOperand(@NotNull Operation operation) {
      ISingleOperand unaryOperand =
          operation
              .asTrait(ISingleOperand.class)
              .orElseThrow(
                  () ->
                      new AssertionError(
                          "Operation does not implement ISingleOperand: " + operation));
      Type target = unaryOperand.getOperand().getType();
      if (!isNumeric(target)) {
        operation.emitError("Operands must be numeric");
        return false;
      }
      return true;
    }
  }

  abstract class UnaryNumericResultOp extends UnaryNumericOp implements IHasResult {
    /** Default constructor used during dialect registration. */
    UnaryNumericResultOp() {
      super();
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return operation -> {
        if (!verifyUnaryNumericOperand(operation)) {
          return false;
        }
        var unaryOp = operation.asTrait(ISingleOperand.class).orElseThrow();
        if (operation.getOutput().isEmpty()) {
          operation.emitError("Operation must have an output");
          return false;
        }
        var expectedType = unaryOp.getOperand().getType();
        var actualType = operation.getOutput().map(OperationResult::getType).orElseThrow();
        if (!actualType.equals(expectedType)) {
          operation.emitError("Result type must match the operand type");
          return false;
        }
        return true;
      };
    }
  }

  final class UnaryOp extends UnaryNumericResultOp implements ArithOps {

    @Override
    public @NotNull String getIdent() {
      return "arith.unary";
    }
  }

  /**
   * Unified binary numeric operation for the {@code arith} dialect.
   *
   * <p>MLIR reference: {@code arith.bin}
   */
  final class BinaryOp extends ArithOp implements ArithOps, IBinaryOperands, IHasResult {

    @Override
    public @NotNull String getIdent() {
      return "arith.bin";
    }

    @Override
    public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
      return List.of(new NamedAttribute("binMode", new BinModeAttr(BinMode.ADD)));
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return operation -> {
        BinaryOp binaryOp = operation.as(BinaryOp.class).orElseThrow();
        Type lhsType = binaryOp.getLhs().getType();
        Type rhsType = binaryOp.getRhs().getType();
        if (!isNumeric(lhsType) || !isNumeric(rhsType)) {
          binaryOp.emitError("Operands must be numeric");
          return false;
        }

        if (binaryOp.getAttributeAs("binMode", BinModeAttr.class).isEmpty()) {
          binaryOp.emitError("Binary operation must define a binMode attribute");
          return false;
        }
        BinMode binMode = binaryOp.getAttributeAs("binMode", BinModeAttr.class).get().getMode();
        Optional<String> modeError = binMode.operandsVerifier.apply(binaryOp);
        if (modeError.isPresent()) {
          binaryOp.emitError(modeError.get());
          return false;
        }
        switch (binMode) {
          // Regular arithmetic operations.
          case ADD, SUB, MUL, MULUI, DIV, DIVUI, MOD, MODUI -> {
            if (!binaryOp.getResult().getType().equals(getDominantType(lhsType, rhsType))) {
              binaryOp.emitError("Result type must be the dominant type of LHS and RHS");
              return false;
            }
          }
          case BOR, BAND, BXOR -> {
            if (!binaryOp.getResult().getType().equals(lhsType)) {
              binaryOp.emitError("Result type must match LHS and RHS type");
              return false;
            }
          }
          // Binary shift operations.
          case LSH, RSHS, RSHU -> {
            if (!binaryOp.getResult().getType().equals(lhsType)) {
              binaryOp.emitError("Result type must match LHS type");
              return false;
            }
          }
          // Logical operations.
          case AND, OR, XOR, EQ, NE, LT, LE, GT, GE -> {
            if (!binaryOp.getResult().getType().equals(IntegerT.BOOL)) {
              binaryOp.emitError("Result type must be boolean");
              return false;
            }
          }
        }
        return true;
      };
    }

    private BinaryOp() {}

    /**
     * Create a binary op with two numeric operands.
     *
     * @param loc the source location of this operation.
     * @param lhs the left-hand operand.
     * @param rhs the right-hand operand.
     * @param binMode the binary operation kind.
     */
    public BinaryOp(
        @NotNull Location loc, @NotNull Value lhs, @NotNull Value rhs, @NotNull BinMode binMode) {
      // Get the right output type for the given operands and binary operation kind.
      Type outputType =
          switch (binMode) {
            // Regular arithmetic operations.
            case ADD, SUB, MUL, MULUI, DIV, DIVUI, MOD, MODUI ->
                getDominantType(lhs.getType(), rhs.getType());
            // Binary operations.
            case BOR, BAND, BXOR, LSH, RSHS, RSHU -> lhs.getType();
            // Logical operations.
            case AND, OR, XOR, EQ, NE, LT, LE, GT, GE -> IntegerT.BOOL;
          };

      setOperation(Operation.Create(loc, this, List.of(lhs, rhs), null, outputType));
      setAttribute("binMode", new BinModeAttr(binMode));
    }

    public @NotNull BinMode getMode() {
      return getAttributeAs("binMode", BinModeAttr.class)
          .map(BinModeAttr::getMode)
          .orElseThrow(() -> new AssertionError("No binMode attribute found."));
    }
  }

  /** Casts a numeric operand to a target numeric type. */
  final class CastOp extends ArithOp implements ArithOps, ISingleOperand, IHasResult {

    @Override
    public @NotNull String getIdent() {
      return "arith.cast";
    }

    @Override
    public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
      return List.of(new NamedAttribute("to", new TypeAttribute()));
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return operation -> {
        var castOp = operation.as(CastOp.class).orElseThrow();
        if (!isNumeric(castOp.getOperandType())) {
          castOp.emitError("Cast operand must be numeric");
          return false;
        }
        Type targetType = castOp.getTargetType();
        if (!isNumeric(targetType)) {
          castOp.emitError("Cast target type must be numeric");
          return false;
        }
        if (!castOp.getResultType().equals(targetType)) {
          castOp.emitError("Cast result type must match the target type");
          return false;
        }
        return true;
      };
    }

    private CastOp() {}

    /**
     * Create a cast op.
     *
     * @param loc the source location of this operation.
     * @param value the value to cast.
     * @param targetType the target numeric type.
     */
    public CastOp(@NotNull Location loc, @NotNull Value value, @NotNull Type targetType) {
      setOperation(Operation.Create(loc, this, List.of(value), null, targetType));
      setAttribute("to", new TypeAttribute(targetType));
    }

    public @NotNull Type getTargetType() {
      return getAttributeAs("to", TypeAttribute.class)
          .map(TypeAttribute::getType)
          .orElseThrow(() -> new AssertionError("No target type attribute found."));
    }
  }

  /**
   * Produces a single constant value in the {@code arith} dialect.
   *
   * <p>The constant is carried by the required {@code "value"} attribute, which must be a {@link
   * TypedAttribute}. The operation result type is taken directly from the attribute type.
   */
  final class ConstantOp extends ArithOp implements ArithOps, INoOperands, IHasResult {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull String getIdent() {
      return "arith.constant";
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return ignored -> true;
    }

    @Contract(pure = true)
    @Override
    public @NotNull List<NamedAttribute> getDefaultAttributes() {
      return List.of(new NamedAttribute("value", new IntegerAttribute(0, IntegerT.INT32)));
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    private ConstantOp() {}

    /**
     * Create a constant op whose value is given by the typed attribute.
     *
     * @param location the source location of this operation.
     * @param value the typed attribute holding the constant value and its type.
     */
    public ConstantOp(@NotNull Location location, @NotNull TypedAttribute value) {
      setOperation(true, Operation.Create(location, this, null, null, value.getType()));
      getAttributeMap().get("value").setAttribute(value);
    }

    /**
     * Create a string constant.
     *
     * @param location the source location of this operation.
     * @param value the string literal to embed.
     */
    public ConstantOp(@NotNull Location location, @NotNull String value) {
      this(location, new StringAttribute(value));
    }

    /**
     * Create a 32-bit integer constant.
     *
     * @param location the source location of this operation.
     * @param value the integer value to embed.
     */
    public ConstantOp(@NotNull Location location, int value) {
      this(location, new IntegerAttribute(value, IntegerT.INT32));
    }

    /**
     * Create a boolean ({@code int1}) constant.
     *
     * @param location the source location of this operation.
     * @param value the boolean value to embed.
     */
    public ConstantOp(@NotNull Location location, boolean value) {
      this(location, new IntegerAttribute(value ? 1 : 0, IntegerT.BOOL));
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the {@code "value"} typed attribute.
     *
     * @return the value attribute.
     * @throws AssertionError if the attribute is absent.
     */
    @Contract(pure = true)
    public @NotNull TypedAttribute getValueAttribute() {
      return getAttributeAs("value", TypedAttribute.class)
          .orElseThrow(() -> new AssertionError("No value attribute found."));
    }

    /**
     * Returns the type of the constant value.
     *
     * @return the value's type.
     */
    @Contract(pure = true)
    public @NotNull Type getValueType() {
      return getValueAttribute().getType();
    }

    /**
     * Returns the raw storage object of the constant value.
     *
     * @return the value's underlying storage object.
     */
    @Contract(pure = true)
    public Object getValueStorage() {
      return getValueAttribute().getStorage();
    }

    /**
     * Returns the SSA {@link Value} produced by this operation.
     *
     * @return the output value.
     * @throws AssertionError if the output is absent.
     */
    @Contract(pure = true)
    public @NotNull Value getValue() {
      return getOutputValue().orElseThrow(() -> new AssertionError("No output value found."));
    }
  }
}
