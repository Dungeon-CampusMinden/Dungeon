package dgir.dialect.func;

import dgir.core.Dialect;
import dgir.core.SymbolTable;
import dgir.core.Utils;
import dgir.core.debug.Location;
import dgir.core.ir.*;
import dgir.core.traits.*;
import dgir.dialect.builtin.BuiltinAttrs;
import dgir.dialect.str.StrAttrs;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static dgir.dialect.func.FuncTypes.FuncType;

/**
 * Sealed marker interface for all operations in the {@link FuncDialect}.
 *
 * <p>Every concrete op must both extend {@link FuncBaseOp} and implement this interface so that
 * {@link Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface FuncOps {
  /**
   * Abstract base class for all operations in the {@code func} dialect.
   *
   * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
   * implement {@link FuncOps} to be enumerated by {@link FuncDialect}.
   */
  abstract class FuncBaseOp extends Op {

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    FuncBaseOp() {
      super();
    }

    // =========================================================================
    // Op Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return FuncDialect.class;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getNamespace() {
      return "func";
    }
  }

  /**
   * Calls a named function in the {@code func} dialect.
   *
   * <p>The callee is referenced by name via the {@code "callee"} {@link
   * BuiltinAttrs.SymbolRefAttribute}. At verification time the symbol is resolved in the nearest
   * enclosing {@link SymbolTable} and the operand/result types are checked against the callee's
   * {@link FuncType}.
   *
   * <p>MLIR reference: {@code func.call}
   *
   * <pre>{@code
   * %result = func.call @add(%a, %b) : (int32, int32) -> int32
   * }</pre>
   */
  final class CallOp extends FuncBaseOp implements FuncOps, ISymbolUser {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull String getIdent() {
      return "func.call";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return operation -> {
        CallOp callOp = operation.as(CallOp.class).orElseThrow();

        // Make sure that the callee function exists in the nearest symbol table
        Optional<FuncOp> callee =
            SymbolTable.lookupSymbolInNearestTableAsOp(operation, callOp.getCallee(), FuncOp.class);
        if (callee.isEmpty()) {
          operation.emitError("Could not find function " + callOp.getCallee());
          return false;
        }

        // Make sure that the function type matches the call site
        var calleeType = callee.get().getType();
        var funcType = callOp.getFunctionType();
        if (!calleeType.equals(funcType)) {
          callOp
              .getOperation()
              .emitError(
                  "Function type does not match call site type for function "
                      + callOp.getCallee()
                      + ": "
                      + calleeType.getParameterizedIdent()
                      + " != "
                      + funcType.getParameterizedIdent());
          return false;
        }
        return true;
      };
    }

    @Contract(pure = true)
    @Override
    public @NotNull List<NamedAttribute> getDefaultAttributes() {
      return List.of(
          new NamedAttribute(getCalleeAttributeName(), new BuiltinAttrs.SymbolRefAttribute("foo")));
    }

    /**
     * Returns the attribute name used to store the callee symbol reference.
     *
     * @return {@code "callee"}
     */
    @Contract(pure = true)
    public static @NotNull String getCalleeAttributeName() {
      return "callee";
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    private CallOp() {}

    /**
     * Create a call with an explicit operand list and callee type.
     *
     * @param location the source location of this operation.
     * @param name the symbol name of the function to call.
     * @param operands the argument values.
     * @param calleeType the function signature used to determine the result type.
     */
    public CallOp(
        @NotNull Location location,
        @NotNull String name,
        @NotNull List<Value> operands,
        @NotNull FuncType calleeType) {
      setOperation(Operation.Create(location, this, operands, null, calleeType.getOutput()));
      setCallee(name);
    }

    /**
     * Create a call with an explicit operand list and return type.
     *
     * @param location the source location of this operation.
     * @param name the symbol name of the function to call.
     * @param operands the argument values.
     * @param returnType the return type of the call
     */
    public CallOp(
        @NotNull Location location,
        @NotNull String name,
        @NotNull List<Value> operands,
        @Nullable Type returnType) {
      setOperation(Operation.Create(location, this, operands, null, returnType));
      setCallee(name);
    }

    /**
     * Create a call using varargs syntax.
     *
     * @param location the source location of this operation.
     * @param name the symbol name of the function to call.
     * @param calleeType the function signature used to determine the result type.
     * @param operands the argument values (varargs).
     */
    public CallOp(
        @NotNull Location location,
        @NotNull String name,
        @NotNull FuncType calleeType,
        Value... operands) {
      this(location, name, List.of(operands), calleeType);
    }

    /**
     * Create a call to a specific {@link FuncOp} with an explicit operand list.
     *
     * @param location the source location of this operation.
     * @param funcOp the function to call.
     * @param operands the argument values.
     */
    public CallOp(
        @NotNull Location location, @NotNull FuncOp funcOp, @NotNull List<Value> operands) {
      setOperation(Operation.Create(location, this, operands, null, funcOp.getType().getOutput()));
      setCallee(funcOp.getFuncName());
    }

    /**
     * Create a call to a specific {@link FuncOp} using varargs syntax.
     *
     * @param location the source location of this operation.
     * @param funcOp the function to call.
     * @param operands the argument values (varargs).
     */
    public CallOp(@NotNull Location location, @NotNull FuncOp funcOp, Value... operands) {
      this(location, funcOp, List.of(operands));
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the symbol name of the callee function.
     *
     * @return the callee symbol name.
     */
    @Contract(pure = true)
    public @NotNull String getCallee() {
      return Objects.requireNonNull(
          getAttributeAs(getCalleeAttributeName(), BuiltinAttrs.SymbolRefAttribute.class)
              .orElseThrow(() -> new AssertionError("No callee attribute found"))
              .getStorage(),
          "Callee symbol name must not be null");
    }

    private void setCallee(@NotNull String name) {
      getSymbolRefAttribute().setValue(name);
    }

    /**
     * Get the function type that results from this call's operands and output.
     *
     * @return The function type inferred from the operands and the operation result.
     */
    @Contract(pure = true)
    public @NotNull FuncType getFunctionType() {
      List<Type> inputTypes =
          getOperands().stream().map(ValueOperand::getType).map(type -> type.orElse(null)).toList();
      Type outputType = getOutput().map(OperationResult::getType).orElse(null);
      return FuncType.of(inputTypes, outputType);
    }

    @Contract(pure = true)
    @Override
    public @NotNull BuiltinAttrs.SymbolRefAttribute getSymbolRefAttribute() {
      return getAttributeAs(getCalleeAttributeName(), BuiltinAttrs.SymbolRefAttribute.class)
          .orElseThrow(() -> new RuntimeException("No symbol attribute found"));
    }
  }

  /**
   * Declares a named function with a body region in the {@code func} dialect.
   *
   * <p>A {@code func.func} op carries two mandatory attributes:
   *
   * <ul>
   *   <li>{@link SymbolTable#getSymbolAttributeName()} — the function's symbol name (e.g. {@code
   *       "main"}).
   *   <li>{@code "type"} — a {@link BuiltinAttrs.TypeAttribute} wrapping the function's {@link
   *       FuncType}.
   * </ul>
   *
   * <p>The op contributes exactly one region that holds the function body. It implements {@link
   * ISymbol} so its name can be looked up via {@link SymbolTable}, and {@link IIsolatedFromAbove}
   * to prevent the body from capturing values defined outside the function.
   *
   * <p>MLIR reference: {@code func.func}
   *
   * <pre>{@code
   * func.func @add(%a: int32, %b: int32) -> int32 {
   *   ...
   * }
   * }</pre>
   */
  final class FuncOp extends FuncBaseOp
      implements FuncOps, ImplicitTerminator, ISymbol, IIsolatedFromAbove, IGlobal, ISingleRegion {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull String getIdent() {
      return "func.func";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return ignored -> true;
    }

    @Contract(pure = true)
    @Override
    public @NotNull List<NamedAttribute> getDefaultAttributes() {
      return List.of(
          new NamedAttribute(
              SymbolTable.getSymbolAttributeName(), new StrAttrs.StringAttribute("foo")),
          new NamedAttribute("type", new BuiltinAttrs.TypeAttribute(FuncType.empty())));
    }

    @Override
    public @NotNull Constructor<? extends ITerminator> getImplicitTerminatorType() {
      return new ReturnOp()
          .getLocationConstructor()
          .orElseThrow(
              () ->
                  new AssertionError(
                      "FuncOp's implicit terminator must have a location constructor"));
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    private FuncOp() {}

    /**
     * Create a function with the given name and a default (no-arg, void) {@link FuncType}.
     *
     * @param location the source location of this operation.
     * @param name the symbol name of the function.
     */
    public FuncOp(@NotNull Location location, @NotNull String name) {
      this(location, name, FuncType.empty());
    }

    /**
     * Create a function with the given name and explicit type.
     *
     * @param location the source location of this operation.
     * @param name the symbol name of the function.
     * @param type the function signature.
     */
    public FuncOp(@NotNull Location location, @NotNull String name, @NotNull FuncType type) {
      setOperation(
          true, Operation.Create(location, this, null, null, type.getOutput(), type.getInputs()));
      getFuncNameAttribute().setValue(name);
      getTypeAttribute().setType(type);
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the {@link StrAttrs.StringAttribute} that holds the function's symbol name.
     *
     * @return the symbol name attribute.
     * @throws RuntimeException if the attribute is absent.
     */
    @Contract(pure = true)
    public @NotNull StrAttrs.StringAttribute getFuncNameAttribute() {
      return getAttributeAs(SymbolTable.getSymbolAttributeName(), StrAttrs.StringAttribute.class)
          .orElseThrow(() -> new RuntimeException("Symbol attribute not found"));
    }

    /**
     * Returns the function's symbol name as a plain string.
     *
     * @return the function name.
     */
    @Contract(pure = true)
    public @NotNull String getFuncName() {
      return getFuncNameAttribute().getValue();
    }

    /**
     * Returns the {@link BuiltinAttrs.TypeAttribute} that carries the function's {@link FuncType}.
     *
     * @return the type attribute.
     * @throws RuntimeException if the attribute is absent.
     */
    @Contract(pure = true)
    public @NotNull BuiltinAttrs.TypeAttribute getTypeAttribute() {
      return getOperation()
          .getAttributeAs("type", BuiltinAttrs.TypeAttribute.class)
          .orElseThrow(() -> new RuntimeException("Type attribute not found"));
    }

    /**
     * Returns the function's {@link FuncType} (inputs + output).
     *
     * @return the function type.
     */
    @Contract(pure = true)
    public @NotNull FuncType getType() {
      return (FuncType) getTypeAttribute().getStorage();
    }
  }

  /**
   * Returns from a {@link FuncOp}, optionally carrying a single return value.
   *
   * <p>This is a terminator: it must be the last operation in the function body's exit block. If
   * the enclosing {@link FuncOp} has a non-void output type, exactly one operand must be provided
   * and its type must match the function output type.
   *
   * <p>MLIR reference: {@code func.return}
   *
   * <pre>{@code
   * func.return %result : int32
   * // or, for void functions:
   * func.return
   * }</pre>
   */
  final class ReturnOp extends FuncBaseOp
      implements FuncOps, ITerminator, IZeroOrOneOperand, ISpecificParentOp {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull String getIdent() {
      return "func.return";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return operation -> {
        ReturnOp returnOp = operation.as(ReturnOp.class).orElseThrow();

        Optional<Operation> parentOp = operation.getParentOperation();
        if (parentOp.isEmpty()) {
          operation.emitError("Return operation must be nested in a function");
          return false;
        }
        FuncOp parentFuncOp = parentOp.get().as(FuncOp.class).orElseThrow();
        // Ensure that the return op's operand type matches the function output type
        if (returnOp.getOperandType().isPresent()) {
          var returnType =
              returnOp
                  .getOperandType()
                  .get()
                  .orElseThrow(() -> new RuntimeException("Return op operand value is not set."));
          var funcType = parentFuncOp.getType();
          if (!returnType.equals(funcType.getOutput())) {
            operation.emitError(
                "Return type "
                    + returnType.getParameterizedIdent()
                    + " does not match function return type "
                    + (funcType.getOutput() != null
                        ? funcType.getOutput().getParameterizedIdent()
                        : null));
            return false;
          }
        }
        return true;
      };
    }

    @Override
    public @NotNull Optional<Constructor<? extends ITerminator>> getLocationConstructor() {
      try {
        return Optional.of(ReturnOp.class.getConstructor(Location.class));
      } catch (NoSuchMethodException e) {
        throw new AssertionError(
            "Terminator "
                + ReturnOp.class
                + "does not define a public constructor that takes only a location as parameter.",
            e);
      }
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    private ReturnOp() {
      executeIfRegistered(
          ReturnOp.class,
          () -> setOperation(false, Operation.Create(Location.UNKNOWN, this, null, null, null)));
    }

    /**
     * Create a void return op.
     *
     * @param location the source location of this operation.
     */
    public ReturnOp(@NotNull Location location) {
      setOperation(false, Operation.Create(location, this, null, null, null));
    }

    /**
     * Create a return op that yields the given value.
     *
     * @param location the source location of this operation.
     * @param operand the value to return from the enclosing function.
     */
    public ReturnOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(Operation.Create(location, this, List.of(operand), null, null));
    }

    // =========================================================================
    // Functions
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
      return List.of(FuncOp.class);
    }

    /**
     * Returns the value being returned, or empty if this is a void return.
     *
     * @return the optional return value.
     */
    @Contract(pure = true)
    public @NotNull Optional<Value> getReturnValue() {
      return getOperand().flatMap(value -> value);
    }
  }

  /** Creates a constant function reference. Can be used for indirect function calls. */
  final class ConstantOp extends FuncBaseOp implements FuncOps, INoOperands, IHasResult {

    @Override
    public @NotNull String getIdent() {
      return "func.constant";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        if (!(operation.getOutputValueOrThrow().getType() instanceof FuncType)) {
          operation.emitError("Constant function must return a function type.");
          return false;
        }
        return true;
      };
    }

    @Override
    public @NotNull @Unmodifiable List<@NotNull NamedAttribute> getDefaultAttributes() {
      return List.of(new NamedAttribute("callee", new BuiltinAttrs.SymbolRefAttribute("foo")));
    }

    private ConstantOp() {}

    public ConstantOp(
        @NotNull Location location, @NotNull String name, @NotNull FuncType funcType) {
      setOperation(Operation.Create(location, this, null, null, funcType));
      getAttributeAs("callee", BuiltinAttrs.SymbolRefAttribute.class).orElseThrow().setValue(name);
    }

    public ConstantOp(@NotNull Location location, @NotNull FuncOp funcOp) {
      this(location, funcOp.getFuncName(), funcOp.getType());
    }
  }

  final class CallIndirectOp extends FuncBaseOp implements FuncOps {
    @Override
    public @NotNull String getIdent() {
      return "func.call_indirect";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        CallIndirectOp callIndirectOp = operation.as(CallIndirectOp.class).orElseThrow();
        // Make sure the first argument is a value of function type
        if (callIndirectOp.getOperands().isEmpty()) {
          operation.emitError(
              "Indirect call must have at least one operand (the function to call)");
          return false;
        }
        var targetOperand = callIndirectOp.getOperandValue(0).orElseThrow();
        if (!(targetOperand.getType() instanceof FuncType funcType)) {
          operation.emitError("Indirect call target value must hold a function type");
          return false;
        }
        // Make sure that the function type matches the call site (i.e. the operand types and result
        // type match the function type)
        if (!callIndirectOp.getSignature().equals(funcType)) {
          operation.emitError(
              "Function type does not match call site type for indirect call: "
                  + funcType.getParameterizedIdent()
                  + " != "
                  + callIndirectOp.getSignature().getParameterizedIdent());
          return false;
        }
        return true;
      };
    }

    private CallIndirectOp() {}

    public CallIndirectOp(
        @NotNull Location location, @NotNull Value target, @NotNull List<Value> operands) {
      if (!(target.getType() instanceof FuncType funcType)) {
        throw new IllegalArgumentException("Target value must have a function type");
      }
      List<Value> operandsWithTarget = new ArrayList<>(operands);
      operandsWithTarget.addFirst(target);
      setOperation(
          Operation.Create(location, this, operandsWithTarget, null, funcType.getOutput()));
    }

    public @NotNull FuncType getSignature() {
      List<Type> inputTypes =
          getOperands().stream()
              .skip(1)
              .map(ValueOperand::getType)
              .map(type -> type.orElse(null))
              .toList();
      Type outputType = getOutput().map(OperationResult::getType).orElse(null);
      return FuncType.of(inputTypes, outputType);
    }
  }
}
