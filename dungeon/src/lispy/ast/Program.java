package lispy.ast;

import java.util.List;

/**
 * A programm represents also an AST node.
 *
 * @param expressions expressions
 */
public record Program(List<Expr> expressions) implements AST {
  /**
   * create a new programm expression.
   *
   * @param expressions expressions
   * @return program (containing expressions)
   */
  public static Program of(Expr... expressions) {
    return new Program(List.of(expressions));
  }
}
