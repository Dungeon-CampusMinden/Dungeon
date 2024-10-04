package nodes;

public class StartNode extends INode {

  public INode firstNode;

  public StartNode(String type, INode firstNode) {
    super(type);
    this.firstNode = firstNode;
  }

  public boolean getBoolValue() {
    if (!firstNode.type.equals("base")) {
      System.out.println("Something went wrong, we do not have a base type node");
      return false;
    }
    return ((BaseNode) firstNode).boolVal;
  }

}
