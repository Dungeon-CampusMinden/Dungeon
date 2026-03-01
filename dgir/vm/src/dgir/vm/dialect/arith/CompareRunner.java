package dgir.vm.dialect.arith;

import core.ir.Operation;
import core.ir.Type;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.NotNull;


import static dialect.arith.ArithOps.*;

public class CompareRunner extends OpRunner {
  public CompareRunner() {
    super(CompareOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    CompareOp compareOp = op.as(CompareOp.class).orElseThrow();
    var lhsValue = NumericUtils.getNumber(state, compareOp.getLhs());
    var rhsValue = NumericUtils.getNumber(state, compareOp.getRhs());
    var lhsType = compareOp.getLhs().getType();
    var rhsType = compareOp.getRhs().getType();

    int cmp = compare(lhsValue, rhsValue, lhsType, rhsType);
    boolean result =
        switch (compareOp.getCompMode()) {
          case EQ -> cmp == 0;
          case NE -> cmp != 0;
          case LT -> cmp < 0;
          case LE -> cmp <= 0;
          case GT -> cmp > 0;
          case GE -> cmp >= 0;
        };

    state.setValueForOutput(op, IntegerT.BOOL.convertToValidNumber(result ? 1 : 0));
    return Action.Next();
  }


  /**
   * Compare two numeric values according to their types. If either type is a float, both values are
   * compared as doubles. Otherwise, they are compared as longs.
   */
  static int compare(
    @NotNull Number lhs, @NotNull Number rhs, @NotNull Type lhsType, @NotNull Type rhsType) {
    if (lhsType instanceof FloatT || rhsType instanceof FloatT) {
      double left = lhs.doubleValue();
      double right = rhs.doubleValue();
      return Double.compare(left, right);
    }
    long left = lhs.longValue();
    long right = rhs.longValue();
    return Long.compare(left, right);
  }
}

