package nodes;

public class ExprNode extends INode {
  public INode leftNode;

  public INode rightNode;

  public String operator;
  public boolean parentheseExpr;

  public ExprNode(String type, INode left, INode right, String operator) {
    super(type);
    this.operator = operator;
    this.leftNode = left;
    this.rightNode = right;
  }

  @Override
  public String toString() {
    StringBuilder retVal;
    if (type.equals("return")) {
      retVal = new StringBuilder("Return " + leftNode.toString());
    } else {
      if (rightNode == null) {
        // We got a unary or not operation
        retVal = new StringBuilder(operator + " " + leftNode.toString());
      } else {
        // We got left and right node
        retVal = new StringBuilder(leftNode.toString() + " " + operator + " " + rightNode.toString());
      }
    }

    for (INode nextNeighbourNode : nextNeighbourNodes) {
      retVal.append(nextNeighbourNode.toString());
    }

    return retVal.toString();
  }

}
