package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class PropertyDefNode extends Node {
  /** WTF? . */
  public final int idIdx = 0;

  /** WTF? . */
  public final int stmtIdx = 1;

  /**
   * Getter for the AstNode corresponding to the identifier of the property definition.
   *
   * @return the AstNode corresponding to the identifier (lhs) of the property definition
   */
  public Node getIdNode() {
    return this.getChild(idIdx);
  }

  /**
   * Getter for the AstNode corresponding to the statement (rhs) of the property definition.
   *
   * @return foo
   */
  public Node getStmtNode() {
    return this.getChild(stmtIdx);
  }

  /**
   * Getter for the identifier of the property definition (lhs).
   *
   * @return identifier of the property definition (lhs) as String
   */
  public String getIdName() {
    return ((IdNode) this.getIdNode()).getName();
  }

  /**
   * Constructor. WTF? .
   *
   * @param id AstNode corresponding to the identifier (lhs)
   * @param stmt AstNode corresponding to the statement (rhs)
   */
  public PropertyDefNode(Node id, Node stmt) {
    super(Type.PropertyDefinition, new ArrayList<>(2));
    this.addChild(id);
    this.addChild(stmt);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
