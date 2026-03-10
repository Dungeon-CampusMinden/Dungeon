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
import org.jetbrains.annotations.NotNull;

import java.util.List;
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

  final class StringConcatOp extends StrOps.StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "string_concat";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        StringConcatOp stringConcatOp = operation.as(StringConcatOp.class).orElseThrow();
        if (!stringConcatOp.getResultType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Result type must be string");
          return false;
        }
        if (!stringConcatOp.getLhs().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("LHS operand must be string");
          return false;
        }
        if (!(stringConcatOp.getRhs().getType() instanceof StrTypes.StrType)
            || !(stringConcatOp.getRhs().getType() instanceof BuiltinTypes.BuiltinType)) {
          operation.emitError(
              "RHS operand must be a string or builtin type. Got "
                  + stringConcatOp.getRhs().getType()
                  + " instead");
          return false;
        }
        return true;
      };
    }

    private StringConcatOp() {}

    public StringConcatOp(@NotNull Location location, @NotNull Value left, @NotNull Value right) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(left, right), null, StrTypes.StringT.INSTANCE));
    }
  }

  final class StringLengthOp extends StrOps.StrOp implements StrOps, ISingleOperand, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "string_length";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        StringLengthOp stringLengthOp = operation.as(StringLengthOp.class).orElseThrow();
        if (!stringLengthOp.getResultType().equals(BuiltinTypes.IntegerT.INT32)) {
          operation.emitError("Result type must be int");
          return false;
        }
        if (!stringLengthOp.getOperand().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("Operand must be string");
          return false;
        }
        return true;
      };
    }

    private StringLengthOp() {}

    public StringLengthOp(@NotNull Location location, @NotNull Value operand) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(operand), null, BuiltinTypes.IntegerT.INT32));
    }
  }

  final class CharAtOp extends StrOps.StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "char_at";
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

  final class StringEqualsOp extends StrOps.StrOp implements StrOps, IBinaryOperands, IHasResult {
    @Override
    public @NotNull String getIdent() {
      return "string_equals";
    }

    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return operation -> {
        StringEqualsOp stringEqualsOp = operation.as(StringEqualsOp.class).orElseThrow();
        if (!stringEqualsOp.getResultType().equals(BuiltinTypes.IntegerT.BOOL)) {
          operation.emitError("Result type must be bool");
          return false;
        }
        if (!stringEqualsOp.getLhs().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("LHS operand must be string");
          return false;
        }
        if (!stringEqualsOp.getRhs().getType().equals(StrTypes.StringT.INSTANCE)) {
          operation.emitError("RHS operand must be string");
          return false;
        }
        return true;
      };
    }

    private StringEqualsOp() {}

    public StringEqualsOp(@NotNull Location location, @NotNull Value left, @NotNull Value right) {
      setOperation(
          true,
          Operation.Create(location, this, List.of(left, right), null, BuiltinTypes.IntegerT.BOOL));
    }
  }
}
