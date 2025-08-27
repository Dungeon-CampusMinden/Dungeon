package lispy.ast;

/** Base class for expressions. */
public sealed interface Expr extends AST
    permits NumberLiteral, StringLiteral, BoolLiteral, SymbolExpr, ListExpr {}
