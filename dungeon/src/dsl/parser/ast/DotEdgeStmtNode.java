package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/** WTF? . */
public class DotEdgeStmtNode extends Node {
  private final int lhsIdIdx = 0;

  private final int rhsStmtsStartIdx = 1;
  private int attrListIdx = -1;

  /**
   * Constructor. WTF? .
   *
   * @param idGroups A list of all {@link IdNode}s of the statement in order
   * @param attrList The {@link Node} corresponding to the attribute list of the statement (or
   *     'Node.NONE', if there is no attribute list)
   */
  public DotEdgeStmtNode(List<Node> idGroups, Node attrList) {
    super(Type.DotEdgeStmt, new ArrayList<>());
    idGroups.forEach(this::addChild);
    this.attrListIdx = this.getChildren().size();
    this.addChild(attrList);
  }

  /**
   * Returns a list of the stored DotIdList-Nodes, in order of definition.
   *
   * @return a list of the stored DotIdList-Nodes
   */
  public List<DotIdList> getIdLists() {
    return this.getChildren().subList(0, attrListIdx).stream()
        .map(node -> (DotIdList) node)
        .toList();
  }

  /**
   * WTF? .
   *
   * @return The {@link Node} corresponding to the attribute list of the statement (or 'Node.NONE',
   *     if there is no attribute list)
   */
  public Node getAttrListNode() {
    return this.getChild(attrListIdx);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public List<Node> getAttributes() {
    return this.getChild(attrListIdx).getChildren();
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
