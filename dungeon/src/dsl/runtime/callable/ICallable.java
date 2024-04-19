package dsl.runtime.callable;

import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.List;

/** WTF. ? */
public interface ICallable {
  /** Type of the callable. */
  enum Type {
    /** Native callable type. */
    Native,
    /** User defined callable type. */
    UserDefined
  }

  /**
   * Call this ICallable with a given interpreter and given parameters.
   *
   * @param interperter a {@link DSLInterpreter}, which is used for performing the actual function
   *     call
   * @param parameters List of ASTNodes, corresponding to the parameters of the function call
   * @return The return value of the function call
   */
  Object call(DSLInterpreter interperter, List<Node> parameters);

  /**
   * Get the type of the callable.
   *
   * @return Type of the callable.
   */
  Type getCallableType();

  /**
   * Get the type of the function.
   *
   * @return The type of the function.
   */
  FunctionType getFunctionType();
}
