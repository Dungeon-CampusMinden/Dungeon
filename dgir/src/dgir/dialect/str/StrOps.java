package dgir.dialect.str;

import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.core.traits.IBinaryOperands;
import dgir.core.traits.IHasResult;
import dgir.core.traits.ISingleOperand;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public sealed interface StrOps {
  abstract class StrOp extends Op {
    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return StrDialect.class;
    }

    @Override
    public @NotNull String getNamespace() {
      return "str";
    }
  }

  final class ToStringOp extends StrOp implements StrOps, ISingleOperand, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.to_string";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        ToStringOp toStringOp = operation.as(ToStringOp.class).orElseThrow();
        if (!toStringOp.getResultType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Result type must be string");
          return false;
        }
        if (!(toStringOp.getOperand().getType() instanceof StrTypes.StrType)
            && !(toStringOp.getOperand().getType() instanceof BuiltinTypes.BuiltinType)) {
          operation.emitError(
              "Operand must be a string or builtin type. Got "
                  + toStringOp.getOperand().getType()
                  + " instead");
          return false;
        }
        return true;
      };
    }

    private ToStringOp() {}

    public ToStringOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(operand), null, StrTypes.StringT.INSTANCE));
    }
  }

  final class ConcatOp extends StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.concat";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        ConcatOp concatOp = operation.as(ConcatOp.class).orElseThrow();
        if (!concatOp.getResultType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Result type must be string");
          return false;
        }
        if (!(concatOp.getLhs().getType() instanceof StrTypes.StrType)
            && !(concatOp.getLhs().getType() instanceof BuiltinTypes.BuiltinType)) {
          operation.emitError(
              "LHS operand must be a string or builtin type. Got "
                  + concatOp.getLhs().getType()
                  + " instead");
          return false;
        }
        if (!(concatOp.getRhs().getType() instanceof StrTypes.StrType)
            && !(concatOp.getRhs().getType() instanceof BuiltinTypes.BuiltinType)) {
          operation.emitError(
              "RHS operand must be a string or builtin type. Got "
                  + concatOp.getRhs().getType()
                  + " instead");
          return false;
        }
        return true;
      };
    }

    private ConcatOp() {}

    public ConcatOp(@NotNull Location location, @NotNull Value left, @NotNull Value right) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(left, right), null, StrTypes.StringT.INSTANCE));
    }
  }

  final class LengthOp extends StrOp implements StrOps, ISingleOperand, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.length";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        LengthOp lengthOp = operation.as(LengthOp.class).orElseThrow();
        if (!lengthOp.getResultType().equals(BuiltinTypes.IntegerT.INT32)) {
          operation.emitError("Result type must be int");
          return false;
        }
        if (!lengthOp.getOperand().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Operand must be string");
          return false;
        }
        return true;
      };
    }

    private LengthOp() {}

    public LengthOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(operand), null, BuiltinTypes.IntegerT.INT32));
    }
  }

  final class CharAtOp extends StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.char_at";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        CharAtOp charAtOp = operation.as(CharAtOp.class).orElseThrow();
        if (!charAtOp.getResultType().equals(BuiltinTypes.IntegerT.UINT16)) {
          operation.emitError("Result type must be uint16");
          return false;
        }
        if (!charAtOp.getLhs().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("LHS operand must be string");
          return false;
        }
        if (!charAtOp.getRhs().getType().equals(BuiltinTypes.IntegerT.INT32)) {
          operation.emitError("RHS operand must be int");
          return false;
        }
        return true;
      };
    }

    private CharAtOp() {}

    public CharAtOp(@NotNull Location location, @NotNull Value string, @NotNull Value index) {
      setOperation(
          true,
          Operation.Create(
              location, this, List.of(string, index), null, BuiltinTypes.IntegerT.UINT16));
    }
  }

  final class EqualsOp extends StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.equals";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        EqualsOp equalsOp = operation.as(EqualsOp.class).orElseThrow();
        if (!equalsOp.getResultType().equals(BuiltinTypes.IntegerT.BOOL)) {
          operation.emitError("Result type must be bool");
          return false;
        }
        if (!equalsOp.getLhs().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("LHS operand must be string");
          return false;
        }
        if (!equalsOp.getRhs().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("RHS operand must be string");
          return false;
        }
        return true;
      };
    }

    private EqualsOp() {}

    public EqualsOp(@NotNull Location location, @NotNull Value left, @NotNull Value right) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(left, right), null, BuiltinTypes.IntegerT.BOOL));
    }
  }

  final class IsEmptyOp extends StrOp implements StrOps, ISingleOperand, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.is_empty";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        IsEmptyOp isEmptyOp = operation.as(IsEmptyOp.class).orElseThrow();
        if (!isEmptyOp.getResultType().equals(BuiltinTypes.IntegerT.BOOL)) {
          operation.emitError("Result type must be bool");
          return false;
        }
        if (!isEmptyOp.getOperand().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Operand must be string");
          return false;
        }
        return true;
      };
    }

    private IsEmptyOp() {}

    public IsEmptyOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(operand), null, BuiltinTypes.IntegerT.BOOL));
    }
  }

  final class ToLowerCaseOp extends StrOp implements StrOps, ISingleOperand, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.to_lower_case";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        ToLowerCaseOp op = operation.as(ToLowerCaseOp.class).orElseThrow();
        if (!op.getResultType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Result type must be string");
          return false;
        }
        if (!op.getOperand().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Operand must be string");
          return false;
        }
        return true;
      };
    }

    private ToLowerCaseOp() {}

    public ToLowerCaseOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(operand), null, StrTypes.StringT.INSTANCE));
    }
  }

  final class ToUpperCaseOp extends StrOp implements StrOps, ISingleOperand, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.to_upper_case";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        ToUpperCaseOp op = operation.as(ToUpperCaseOp.class).orElseThrow();
        if (!op.getResultType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Result type must be string");
          return false;
        }
        if (!op.getOperand().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Operand must be string");
          return false;
        }
        return true;
      };
    }

    private ToUpperCaseOp() {}

    public ToUpperCaseOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(operand), null, StrTypes.StringT.INSTANCE));
    }
  }

  final class TrimOp extends StrOp implements StrOps, ISingleOperand, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.trim";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        TrimOp op = operation.as(TrimOp.class).orElseThrow();
        if (!op.getResultType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Result type must be string");
          return false;
        }
        if (!op.getOperand().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Operand must be string");
          return false;
        }
        return true;
      };
    }

    private TrimOp() {}

    public TrimOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(operand), null, StrTypes.StringT.INSTANCE));
    }
  }

  final class SubstringOp extends StrOp implements StrOps, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.substring";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        SubstringOp op = operation.as(SubstringOp.class).orElseThrow();
        if (!op.getResultType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Result type must be string");
          return false;
        }
        if (operation.getOperands().size() < 2 || operation.getOperands().size() > 3) {
          operation.emitError("Operation must have at least two operands and at most three");
          return false;
        }
        if (!op.getOperandValue(0).orElseThrow().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("LHS operand (string) must be string");
          return false;
        }
        if (!op.getOperandValue(1).orElseThrow().getType().equals(BuiltinTypes.IntegerT.INT32)) {
          operation.emitError("RHS operand (beginIndex) must be int");
          return false;
        }
        if (operation.getOperands().size() == 3
            && !op.getOperandValue(2).orElseThrow().getType().equals(BuiltinTypes.IntegerT.INT32)) {
          operation.emitError("Optional third operand (endIndex) must be int");
          return false;
        }
        return true;
      };
    }

    private SubstringOp() {}

    public SubstringOp(
        @NotNull Location location, @NotNull Value string, @NotNull Value beginIndex) {
      setOperation(
          true,
          Operation.Create(
              location, this, List.of(string, beginIndex), null, StrTypes.StringT.INSTANCE));
    }

    public SubstringOp(
        @NotNull Location location,
        @NotNull Value string,
        @NotNull Value beginIndex,
        @NotNull Value endIndex) {
      setOperation(
          true,
          Operation.Create(
              location,
              this,
              List.of(string, beginIndex, endIndex),
              null,
              StrTypes.StringT.INSTANCE));
    }

    @Contract(pure = true)
    public @NotNull Value getString() {
      return getOperandValue(0).orElseThrow();
    }

    @Contract(pure = true)
    public @NotNull Value getBeginIndex() {
      return getOperandValue(1).orElseThrow();
    }

    @Contract(pure = true)
    public @NotNull Optional<Value> getEndIndex() {
      return getOperandValue(2);
    }
  }

  static boolean checkStrictBinaryStringOp(IBinaryOperands binaryOperands) {
    if (!binaryOperands.getLhs().getType().equals(StrTypes.StringT.INSTANCE)) {
      binaryOperands.getOperation().emitError("LHS operand (string) must be string");
      return false;
    }
    if (!binaryOperands.getRhs().getType().equals(StrTypes.StringT.INSTANCE)) {
      binaryOperands.getOperation().emitError("RHS operand must be string");
      return false;
    }
    return true;
  }

  /**
   * {@code str.starts_with} — returns {@code bool} indicating whether the string starts with the
   * prefix.
   */
  final class StartsWithOp extends StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.starts_with";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        StartsWithOp op = operation.as(StartsWithOp.class).orElseThrow();
        if (!op.getResultType().equals(BuiltinTypes.IntegerT.BOOL)) {
          operation.emitError("Result type must be bool");
          return false;
        }
        return checkStrictBinaryStringOp(op);
      };
    }

    private StartsWithOp() {}

    public StartsWithOp(@NotNull Location location, @NotNull Value string, @NotNull Value prefix) {
      setOperation(
          true,
          Operation.Create(
              location, this, List.of(string, prefix), null, BuiltinTypes.IntegerT.BOOL));
    }
  }

  /**
   * {@code str.ends_with} — returns {@code bool} indicating whether the string ends with the
   * suffix.
   */
  final class EndsWithOp extends StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.ends_with";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        EndsWithOp op = operation.as(EndsWithOp.class).orElseThrow();
        return checkStrictBinaryStringOp(op);
      };
    }

    private EndsWithOp() {}

    public EndsWithOp(@NotNull Location location, @NotNull Value string, @NotNull Value suffix) {
      setOperation(
          true,
          Operation.Create(
              location, this, List.of(string, suffix), null, BuiltinTypes.IntegerT.BOOL));
    }
  }

  /**
   * {@code str.index_of} — returns the index ({@code int32}) of the first occurrence of the
   * substring (operand 1) within the string (operand 0), or {@code -1} if not found.
   */
  final class IndexOfOp extends StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.index_of";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        IndexOfOp op = operation.as(IndexOfOp.class).orElseThrow();
        if (!op.getResultType().equals(BuiltinTypes.IntegerT.INT32)) {
          operation.emitError("Result type must be int");
          return false;
        }
        return checkStrictBinaryStringOp(op);
      };
    }

    private IndexOfOp() {}

    public IndexOfOp(@NotNull Location location, @NotNull Value string, @NotNull Value substring) {
      setOperation(
          true,
          Operation.Create(
              location, this, List.of(string, substring), null, BuiltinTypes.IntegerT.INT32));
    }
  }

  /**
   * {@code str.last_index_of} — returns the index ({@code int32}) of the last occurrence of the
   * substring (operand 1) within the string (operand 0), or {@code -1} if not found.
   */
  final class LastIndexOfOp extends StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "str.last_index_of";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        LastIndexOfOp op = operation.as(LastIndexOfOp.class).orElseThrow();
        if (!op.getResultType().equals(BuiltinTypes.IntegerT.INT32)) {
          operation.emitError("Result type must be int");
          return false;
        }
        return checkStrictBinaryStringOp(op);
      };
    }

    private LastIndexOfOp() {}

    public LastIndexOfOp(
        @NotNull Location location, @NotNull Value string, @NotNull Value substring) {
      setOperation(
          true,
          Operation.Create(
              location, this, List.of(string, substring), null, BuiltinTypes.IntegerT.INT32));
    }
  }
}
