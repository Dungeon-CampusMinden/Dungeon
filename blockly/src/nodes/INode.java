package nodes;

import java.util.ArrayList;

public abstract class INode {
  public String type;

  public ArrayList<INode> nextNeighbourNodes = new ArrayList<INode>();

  public INode(String type) {
    this.type = type;
  }
}
