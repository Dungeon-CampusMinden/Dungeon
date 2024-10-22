package nodes;

/** Inherit from this class to store important values when visiting a node in the parsed tree. */
public abstract class INode {
  /** Type of the node. */
  public String type;

  /**
   * Create a new node with the given type.
   *
   * @param type Type of the node.
   */
  public INode(String type) {
    this.type = type;
  }
}
