package dgir.vm.dialect.str;

import dgir.core.ir.Operation;
import dgir.dialect.builtin.BuiltinTypes;
import dgir.dialect.str.StrOps;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

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

  final class IsEmptyRunner extends OpRunner implements StrRunners {
    public IsEmptyRunner() {
      super(StrOps.IsEmptyOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      state.setValueForOutput(op, (byte) (value.isEmpty() ? 1 : 0));
      return Action.Next();
    }
  }

  final class ToLowerCaseRunner extends OpRunner implements StrRunners {
    public ToLowerCaseRunner() {
      super(StrOps.ToLowerCaseOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      state.setValueForOutput(op, value.toLowerCase(Locale.ROOT));
      return Action.Next();
    }
  }

  final class ToUpperCaseRunner extends OpRunner implements StrRunners {
    public ToUpperCaseRunner() {
      super(StrOps.ToUpperCaseOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      state.setValueForOutput(op, value.toUpperCase(Locale.ROOT));
      return Action.Next();
    }
  }

  final class TrimRunner extends OpRunner implements StrRunners {
    public TrimRunner() {
      super(StrOps.TrimOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      state.setValueForOutput(op, value.trim());
      return Action.Next();
    }
  }

  final class SubstringRunner extends OpRunner implements StrRunners {
    public SubstringRunner() {
      super(StrOps.SubstringOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      int beginIndex = state.getValueAsOrThrow(op.getOperandValueOrThrow(1), Integer.class);
      if (op.getOperands().size() == 3) {
        int endIndex = state.getValueAsOrThrow(op.getOperandValueOrThrow(2), Integer.class);
        state.setValueForOutput(op, value.substring(beginIndex, endIndex));
      } else {
        state.setValueForOutput(op, value.substring(beginIndex));
      }
      return Action.Next();
    }
  }

  final class StartsWithRunner extends OpRunner implements StrRunners {
    public StartsWithRunner() {
      super(StrOps.StartsWithOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      String prefix = state.getValueAsOrThrow(op.getOperandValueOrThrow(1), String.class);
      state.setValueForOutput(op, (byte) (value.startsWith(prefix) ? 1 : 0));
      return Action.Next();
    }
  }

  final class EndsWithRunner extends OpRunner implements StrRunners {
    public EndsWithRunner() {
      super(StrOps.EndsWithOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      String suffix = state.getValueAsOrThrow(op.getOperandValueOrThrow(1), String.class);
      state.setValueForOutput(op, (byte) (value.endsWith(suffix) ? 1 : 0));
      return Action.Next();
    }
  }

  final class IndexOfRunner extends OpRunner implements StrRunners {
    public IndexOfRunner() {
      super(StrOps.IndexOfOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      String substring = state.getValueAsOrThrow(op.getOperandValueOrThrow(1), String.class);
      state.setValueForOutput(
          op,
          ((BuiltinTypes.IntegerT) op.getOutputValueOrThrow().getType())
              .convertToValidNumber(value.indexOf(substring)));
      return Action.Next();
    }
  }

  final class LastIndexOfRunner extends OpRunner implements StrRunners {
    public LastIndexOfRunner() {
      super(StrOps.LastIndexOfOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      String value = state.getValueAsOrThrow(op.getOperandValueOrThrow(0), String.class);
      String substring = state.getValueAsOrThrow(op.getOperandValueOrThrow(1), String.class);
      state.setValueForOutput(
          op,
          ((BuiltinTypes.IntegerT) op.getOutputValueOrThrow().getType())
              .convertToValidNumber(value.lastIndexOf(substring)));
      return Action.Next();
    }
  }
}
