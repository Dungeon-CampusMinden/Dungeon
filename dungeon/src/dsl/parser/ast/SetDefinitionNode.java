package dsl.parser.ast;

import java.util.ArrayList;

public class SetDefinitionNode extends Node {
  public ArrayList<Node> getEntries() {
    return this.getChild(0).getChildren();
  }

  public SetDefinitionNode(Node entryList) {
    super(Type.SetDefinitionNode, new ArrayList<>(1));
    this.addChild(entryList);
  }

  public SetDefinitionNode() {
    super(Type.SetDefinitionNode);
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
