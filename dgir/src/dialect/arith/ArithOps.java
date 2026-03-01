package dialect.arith;

import core.Dialect;
import core.debug.Location;
import core.ir.*;
import core.traits.IBinaryOperands;
import core.traits.IHasResult;
import core.traits.INoOperands;
import core.traits.ISingleOperand;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.attributes.TypeAttribute;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

import static dialect.arith.ArithAttr.BinModeAttr;
import static dialect.arith.ArithAttr.CompModeAttr;

/**
 * Sealed marker interface for all operations in the {@link ArithDialect}.
 *
 * <p>Every concrete op must both extend {@link ArithOp} and implement this interface so that {@link
 * core.Utils.Dialect#allOps} can discover it automatically via reflection.
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

  /** Base class for binary numeric operations in the {@code arith} dialect. */
  abstract class BinaryNumericOp extends ArithOp implements IBinaryOperands {

    /** Default constructor used during dialect registration. */
    BinaryNumericOp() {
      super();
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return BinaryNumericOp::verifyBinaryNumericOperands;
    }

    protected static boolean verifyBinaryNumericOperands(@NotNull Operation operation) {
      IBinaryOperands binaryOperands =
          operation
              .asTrait(IBinaryOperands.class)
              .orElseThrow(
                  () ->
                      new AssertionError(
                          "Operation does not implement IBinaryOperands: " + operation));
      Type lhsType = binaryOperands.getLhs().getType();
      Type rhsType = binaryOperands.getRhs().getType();
      if (!isNumeric(lhsType) || !isNumeric(rhsType)) {
        operation.emitError("Operands must be numeric");
        return false;
      }
      return true;
    }

    protected static boolean isNumeric(@NotNull Type type) {
      return type instanceof IntegerT || type instanceof FloatT;
    }

    protected static @NotNull Type getDominantType(@NotNull Type lhsType, @NotNull Type rhsType) {
      if (!isNumeric(lhsType) || !isNumeric(rhsType)) {
        throw new IllegalArgumentException("Dominant type requires numeric operands");
      }

      if (lhsType instanceof FloatT || rhsType instanceof FloatT) {
        int lhsFloatWidth = lhsType instanceof FloatT floatT ? floatT.getWidth() : 0;
        int rhsFloatWidth = rhsType instanceof FloatT floatT ? floatT.getWidth() : 0;
        int lhsIntWidth = lhsType instanceof IntegerT intT ? intT.getWidth() : 0;
        int rhsIntWidth = rhsType instanceof IntegerT intT ? intT.getWidth() : 0;
        int desiredWidth =
            Math.max(Math.max(lhsFloatWidth, rhsFloatWidth), Math.max(lhsIntWidth, rhsIntWidth));
        return desiredWidth > 32 ? FloatT.FLOAT64 : FloatT.FLOAT32;
      }

      int lhsWidth = ((IntegerT) lhsType).getWidth();
      int rhsWidth = ((IntegerT) rhsType).getWidth();
      return integerTypeByWidth(Math.max(lhsWidth, rhsWidth));
    }

    protected static @NotNull IntegerT integerTypeByWidth(int width) {
      return switch (width) {
        case 1 -> IntegerT.INT1;
        case 8 -> IntegerT.INT8;
        case 16 -> IntegerT.INT16;
        case 32 -> IntegerT.INT32;
        case 64 -> IntegerT.INT64;
        default -> new IntegerT(width);
      };
    }
  }

  /** Base class for binary numeric ops that return the dominant operand type. */
  abstract class BinaryNumericResultOp extends BinaryNumericOp implements IHasResult {
    /** Default constructor used during dialect registration. */
    BinaryNumericResultOp() {
      super();
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return operation -> {
        if (!verifyBinaryNumericOperands(operation)) {
          return false;
        }
        var binaryOp = operation.asTrait(IBinaryOperands.class).orElseThrow();
        if (operation.getOutput().isEmpty()) {
          operation.emitError("Operation must have an output");
          return false;
        }
        var lhsType = binaryOp.getLhs().getType();
        var rhsType = binaryOp.getRhs().getType();
        var expectedType = getDominantType(lhsType, rhsType);
        var actualType = operation.getOutput().map(OperationResult::getType).orElseThrow();
        if (!actualType.equals(expectedType)) {
          operation.emitError("Result type must be the dominant operand type");
          return false;
        }
        return true;
      };
    }
  }

  /**
   * Unified binary numeric operation for the {@code arith} dialect.
   *
   * <p>MLIR reference: {@code arith.bin}
   */
  final class BinaryOp extends BinaryNumericResultOp implements ArithOps {

    @Override
    public @NotNull String getIdent() {
      return "arith.bin";
    }

    @Override
    public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
      return List.of(new NamedAttribute("binMode", new BinModeAttr(BinModeAttr.Mode.ADD)));
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return operation -> {
        if (!super.getVerifier().apply(operation)) {
          return false;
        }
        if (operation.getAttribute(BinModeAttr.class, "binMode").isEmpty()) {
          operation.emitError("Binary operation must define a binMode attribute");
          return false;
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
     * @param mode the binary operation kind.
     */
    public BinaryOp(
      @NotNull Location loc,
      @NotNull Value lhs,
      @NotNull Value rhs,
      @NotNull ArithAttr.BinModeAttr.Mode mode) {
      setOperation(
        Operation.Create(loc, this, List.of(lhs, rhs), null, getDominantType(lhs.getType(), rhs.getType())));
      setAttribute("binMode", new BinModeAttr(mode));
    }

    public @NotNull ArithAttr.BinModeAttr.Mode getMode() {
      return getAttribute("binMode", BinModeAttr.class)
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
        if (!BinaryNumericOp.isNumeric(castOp.getOperandType())) {
          castOp.emitError("Cast operand must be numeric");
          return false;
        }
        Type targetType = castOp.getTargetType();
        if (!BinaryNumericOp.isNumeric(targetType)) {
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
      return getAttribute("to", TypeAttribute.class)
          .map(TypeAttribute::getType)
          .orElseThrow(() -> new AssertionError("No target type attribute found."));
    }
  }

  final class CompareOp extends BinaryNumericOp implements ArithOps, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "arith.comp";
    }

    @Override
    public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
      return List.of(new NamedAttribute("compMode", new CompModeAttr(CompModeAttr.Mode.EQ)));
    }

    private CompareOp() {}

    public CompareOp(
        @NotNull Location loc,
        @NotNull Value lhs,
        @NotNull Value rhs,
        @NotNull ArithAttr.CompModeAttr.Mode mode) {
      setOperation(Operation.Create(loc, this, List.of(lhs, rhs), null, IntegerT.BOOL));
      setAttribute("compMode", new CompModeAttr(mode));
    }

    public @NotNull ArithAttr.CompModeAttr.Mode getCompMode() {
      return getAttribute("compMode", CompModeAttr.class)
          .map(CompModeAttr::getMode)
          .orElseThrow(() -> new AssertionError("No compMode attribute found."));
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return operation -> {
        CompareOp compareOp = operation.as(CompareOp.class).orElseThrow();
        if (!verifyBinaryNumericOperands(operation)) {
          return false;
        }
        if (!compareOp.getResultType().equals(IntegerT.BOOL)) {
          operation.emitError("Compare result type must be bool");
          return false;
        }
        return true;
      };
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
      return List.of(new NamedAttribute("value", null));
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
      getAttributes().get("value").setAttribute(value);
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
      return getAttribute("value", TypedAttribute.class)
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
