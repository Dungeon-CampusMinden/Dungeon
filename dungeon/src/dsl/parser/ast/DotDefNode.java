package dsl.parser.ast;

import java.util.ArrayList;
import java.util.List;

/** WTF? . */
public class DotDefNode extends Node {
  private final int idNodeIdx = 0;
  private final int dotStmtStartIdx = 1;

  /**
   * WTF? .
   *
   * @return the IdNode corresponding to the identifier of the graph
   */
  public Node getIdNode() {
    return this.getChild(idNodeIdx);
  }

  /**
   * WTF? .
   *
   * @return the identifier of the graph as a String
   */
  public String getGraphId() {
    return ((IdNode) getIdNode()).getName();
  }

  /**
   * WTF? .
   *
   * @return all dot statements in the graph definition as a list
   */
  public List<Node> getStmtNodes() {
    return this.getChildren().subList(dotStmtStartIdx, this.getChildren().size());
  }

  /**
   * Constructor. WTF? .
   *
   * @param graphId The IdNode corresponding to the identifier of the graph
   * @param dotStmts A list of all dot statements in the definition
   */
  public DotDefNode(Node graphId, ArrayList<Node> dotStmts) {
    super(Node.Type.DotDefinition, new ArrayList<>(dotStmts.size() + 1));
    this.addChild(graphId);
    dotStmts.forEach(this::addChild);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
