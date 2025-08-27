package lispy.ast;

/**
 * Expressions: numbers.
 *
 * @param value value (int)
 */
public record NumberLiteral(int value) implements Expr {}
