package lispy.ast;

/**
 * Expression: string.
 *
 * @param value string
 */
public record StringLiteral(String value) implements Expr {}
