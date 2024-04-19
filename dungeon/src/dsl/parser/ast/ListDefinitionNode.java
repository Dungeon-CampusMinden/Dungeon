package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class ListDefinitionNode extends Node {

  /**
   * WTF? .
   *
   * @return foo
   */
  public ArrayList<Node> getEntries() {
    return this.getChild(0).getChildren();
  }

  /**
   * WTF? .
   *
   * @param entryList foo
   */
  public ListDefinitionNode(Node entryList) {
    super(Type.ListDefinitionNode, new ArrayList<>(1));
    this.addChild(entryList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
