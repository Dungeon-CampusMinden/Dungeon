package dsl.parser.ast;

import java.util.ArrayList;

/** WTF? . */
public class SetDefinitionNode extends Node {
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
  public SetDefinitionNode(Node entryList) {
    super(Type.SetDefinitionNode, new ArrayList<>(1));
    this.addChild(entryList);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
