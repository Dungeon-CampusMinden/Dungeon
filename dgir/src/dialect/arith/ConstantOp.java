package dialect.arith;

import core.ir.*;
import core.traits.INoOperands;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Produces a single constant value in the {@code arith} dialect.
 *
 * <p>The constant is carried by the required {@code "value"} attribute, which must be a {@link
 * TypedAttribute}. The operation result type is taken directly from the attribute type.
 *
 * <p>MLIR reference: {@code arith.constant}
 *
 * <pre>{@code
 * %c = arith.constant value = 42 : int32
 * }</pre>
 */
public final class ConstantOp extends ArithOp implements Arith, INoOperands {

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
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public ConstantOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a constant op whose value is given by the typed attribute.
   *
   * @param location the source location of this operation.
   * @param value the typed attribute holding the constant value and its type.
   */
  public ConstantOp(@NotNull SourceLocation location, @NotNull TypedAttribute value) {
    setOperation(true, Operation.Create(location, this, null, null, value.getType()));
    getAttributes().get("value").setAttribute(value);
  }

  /**
   * Create a string constant.
   *
   * @param location the source location of this operation.
   * @param value the string literal to embed.
   */
  public ConstantOp(@NotNull SourceLocation location, @NotNull String value) {
    this(location, new StringAttribute(value));
  }

  /**
   * Create a 32-bit integer constant.
   *
   * @param location the source location of this operation.
   * @param value the integer value to embed.
   */
  public ConstantOp(@NotNull SourceLocation location, int value) {
    this(location, new IntegerAttribute(value, IntegerT.INT32));
  }

  /**
   * Create a boolean ({@code int1}) constant.
   *
   * @param location the source location of this operation.
   * @param value the boolean value to embed.
   */
  public ConstantOp(@NotNull SourceLocation location, boolean value) {
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
    return getAttribute(TypedAttribute.class, "value")
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
