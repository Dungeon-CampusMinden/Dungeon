package nodes;

/**
 * Start node of the ast. Holds the first node. Call teh function getBoolValue to retrieve the result of a condition.
 */
public class StartNode extends INode {

  /**
   * First node of the ast. Holds the result of the condition.
   */
  public INode firstNode;

  /**
   * Create a new start node. Sets the general type of the node to "start".
   *
   * @param firstNode First node of the ast.
   */
  public StartNode(INode firstNode) {
    super("start");
    this.firstNode = firstNode;
  }

  /**
   * Get the boolean value of the first node in the ast. This node holds the result of the condition.
   *
   * @return Returns the result of the condition.
   */
  public boolean getBoolValue() {
    if (!firstNode.type.equals("base")) {
      System.out.println("Something went wrong, we do not have a base type node");
      return false;
    }
    return ((BaseNode) firstNode).boolVal;
  }
}
