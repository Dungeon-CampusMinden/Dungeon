package dsl.parser.ast;

import java.util.List;

/** WTF? . */
public class DotIdList extends Node {

  /**
   * Return the stored IdNodes of this DotIdList-Node, in order of definition. Will cast each stored
   * Node to IdNode.
   *
   * @return a List of IdNodes defined in this DotIdList-Node
   */
  public List<IdNode> getIdNodes() {
    return this.getChildren().stream().map(node -> (IdNode) node).toList();
  }

  /**
   * WTF? .
   *
   * @param idNodes foo
   */
  public DotIdList(List<Node> idNodes) {
    super(Type.DotIdList);
    idNodes.forEach(this::addChild);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
