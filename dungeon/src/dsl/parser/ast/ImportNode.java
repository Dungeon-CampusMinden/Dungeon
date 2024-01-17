package dsl.parser.ast;

public class ImportNode extends Node {

  public static final int pathIdx = 0;
  public static final int idIdx = 1;
  public static final int asIdIdx = 2;

  public enum Type {
    unnamed,
    named
  }

  protected final Type type;

  public Type importType() {
    return type;
  }

  public Node pathNode() {
    return this.getChild(pathIdx);
  }

  public Node idNode() {
    return this.getChild(idIdx);
  }

  public Node asIdNode() {
    return this.getChild(asIdIdx);
  }

  public ImportNode(Node pathNode, Node idNode) {
    super(Node.Type.ImportNode);
    this.addChild(pathNode);
    this.addChild(idNode);
    // no "as" ID node
    this.addChild(Node.NONE);
    this.type = Type.unnamed;
  }

  public ImportNode(Node pathNode, Node idNode, Node asIdNode) {
    super(Node.Type.ImportNode);
    this.addChild(pathNode);
    this.addChild(idNode);
    this.addChild(asIdNode);
    this.type = Type.named;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
