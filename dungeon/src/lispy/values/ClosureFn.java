package lispy.values;

import java.util.List;
import lispy.Interpreter;
import lispy.ast.Expr;

/**
 * User defined functions (in Lispy).
 *
 * @param name fn name
 * @param params list of param names
 * @param body expression (fn body)
 * @param closureEnv closure
 */
public record ClosureFn(String name, List<String> params, Expr body, Env closureEnv)
    implements FnVal {
  @Override
  public Value apply(List<Value> args) {
    if (args.size() != params.size())
      throw new RuntimeException(
          "arity mismatch when calling "
              + name
              + ": expected "
              + params.size()
              + ", got "
              + args.size());
    Env call = new Env(closureEnv);
    for (int i = 0; i < params.size(); i++) {
      call.define(params.get(i), args.get(i));
    }
    return Interpreter.evaluate(body, call);
  }
}
