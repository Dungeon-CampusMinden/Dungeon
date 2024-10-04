package nodes;

/** This class hold the name and the value of an integer variable. */
public class VarNode extends INode {

  /** Name of the variable. */
  public String name;

  /** Integer value of the variable. */
  public int value;

  /**
   * Create a new variable node. Set the general type of the node to "var".
   *
   * @param name Name of the variable.
   * @param value Integer value of the variable.
   */
  public VarNode(String name, int value) {
    super("var");
    this.name = name;
    this.value = value;
  }

  @Override
  public String toString() {
    return name;
  }
}
