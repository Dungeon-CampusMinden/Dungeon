package dgir.vm.dialect.str;

import dgir.core.ir.Operation;
import dgir.dialect.builtin.BuiltinTypes;
import dgir.dialect.str.StrOps;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

public sealed interface StrRunners {
  final class CharAtRunner extends OpRunner implements StrRunners {
    public CharAtRunner() {
      super(StrOps.CharAtOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      char result =
          state
              .getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class)
              .charAt(state.getValueAsOrThrow(op.getOperandValueOrThrow(1), Integer.class));
      state.setValueForOutput(
          op,
          ((BuiltinTypes.IntegerT) op.getOutputValueOrThrow().getType())
              .convertToValidNumber(result));
      return Action.Next();
    }
  }

  final class ConcatRunner extends OpRunner implements StrRunners {
    public ConcatRunner() {
      super(StrOps.ConcatOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      Object left = state.getValueOrThrow(op.getOperandValueOrThrow(0));
      Object right = state.getValueOrThrow(op.getOperandValueOrThrow(1));
      state.setValueForOutput(op, left.toString() + right.toString());
      return Action.Next();
    }
  }

  final class LengthRunner extends OpRunner implements StrRunners {
    public LengthRunner() {
      super(StrOps.LengthOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      state.setValueForOutput(
          op,
          ((BuiltinTypes.IntegerT) op.getOutputValueOrThrow().getType())
              .convertToValidNumber(value.length()));
      return Action.Next();
    }
  }

  final class ToStringRunner extends OpRunner implements StrRunners {
    public ToStringRunner() {
      super(StrOps.ToStringOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      Object operand = state.getValueOrThrow(op.getOperandValueOrThrow(0));
      state.setValueForOutput(op, operand.toString());
      return Action.Next();
    }
  }

  final class EqualsRunner extends OpRunner implements StrRunners {
    public EqualsRunner() {
      super(StrOps.EqualsOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      Object left = state.getValueOrThrow(op.getOperandValueOrThrow(0));
      Object right = state.getValueOrThrow(op.getOperandValueOrThrow(1));
      state.setValueForOutput(op, (byte) (left.equals(right) ? 1 : 0));
      return Action.Next();
    }
  }
}
