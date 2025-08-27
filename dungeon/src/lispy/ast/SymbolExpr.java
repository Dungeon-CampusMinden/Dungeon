package lispy.ast;

/**
 * Expression: symbol.
 *
 * @param name string
 */
public record SymbolExpr(String name) implements Expr {}
