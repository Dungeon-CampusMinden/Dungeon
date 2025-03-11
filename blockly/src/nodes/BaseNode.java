package nodes;

/** This class holds a base value. A base value can either be a boolean or an integer. */
public class BaseNode extends INode {
  /** Type of the base node. Can either be boolean or integer. */
  public Types baseType;

  /** Boolean value of the node. */
  public boolean boolVal;

  /** Integer value of the node. */
  public int intVal;

  /** String value of the node. */
  public String strVal;

  /**
   * Create a new base node. The general type of the node will be set to "base". The base type can
   * either be integer or boolean.
   *
   * @param baseType Specific type of the base node. Either integer or boolean.
   */
  public BaseNode(Types baseType) {
    super("base");
    this.baseType = baseType;
  }

  @Override
  public String toString() {
    switch (baseType) {
      case INTEGER:
        return Integer.toString(intVal);
      case BOOLEAN:
        if (boolVal) {
          return "True";
        } else {
          return "False";
        }
      case STRING:
        return strVal;
      default:
        System.out.println("Unsupported base type.");
        return "ERROR_WRONG_TYPE";
    }
  }
}
