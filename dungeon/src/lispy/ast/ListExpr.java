package lispy.ast;

import java.util.List;

/**
 * Expression: lists.
 *
 * @param elements expressions
 */
public record ListExpr(List<Expr> elements) implements Expr {
  /**
   * create a new list expression.
   *
   * @param elements expressions
   * @return list expression
   */
  public static ListExpr of(Expr... elements) {
    return new ListExpr(List.of(elements));
  }
}
