package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class ConditionalStmtNodeIf extends Node {
  /** WTF? . */
  public final int conditionIdx = 0;

  /** WTF? . */
  public final int stmtIfIdx = 1;

  /**
   * Getter for the AstNode corresponding to the identifier of the defined function.
   *
   * @return AstNode corresponding to the identifier of the defined function
   */
  public Node getCondition() {
    return this.getChild(conditionIdx);
  }

  /**
   * Getter for the AstNode corresponding to the stmtBlock of the function definition.
   *
   * @return AstNode corresponding to the stmtBlock of the function definition
   */
  public Node getIfStmt() {
    return this.getChild(stmtIfIdx);
  }

  /**
   * Constructor. WTF? .
   *
   * @param condition The AstNode corresponding to the condition
   * @param stmt The AstNode corresponding to the stmt
   */
  public ConditionalStmtNodeIf(Node condition, Node stmt) {
    super(Type.ConditionalStmtIf, new ArrayList<>(2));

    this.addChild(condition);
    this.addChild(stmt);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
