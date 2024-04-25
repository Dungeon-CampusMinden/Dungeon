package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class ParamDefNode extends Node {
  /** WTF? . */
  public final int typeIdIdx = 0;

  /** WTF? . */
  public final int idIdx = 1;

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getTypeIdNode() {
    return getChild(typeIdIdx);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getTypeName() {
    return ((IdNode) getTypeIdNode()).getName();
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public String getIdName() {
    return ((IdNode) getIdNode()).getName();
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Node getIdNode() {
    return getChild(idIdx);
  }

  /**
   * WTF? .
   *
   * @param typeIdNode foo
   * @param idNode foo
   */
  public ParamDefNode(Node typeIdNode, Node idNode) {
    super(Type.ParamDef, new ArrayList<>(2));
    this.addChild(typeIdNode);
    this.addChild(idNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
