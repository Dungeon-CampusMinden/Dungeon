package lispy.ast;

/**
 * Expression: bool.
 *
 * @param value bool
 */
public record BoolLiteral(boolean value) implements Expr {}
