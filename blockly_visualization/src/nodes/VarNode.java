package nodes;

public class VarNode extends INode {

    public String name;

    public int value;

    public VarNode(String type, String name, int value) {
        super(type);
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString(){
        StringBuilder retVal = new StringBuilder();

        retVal.append(name);

        for (INode nextNeighbourNode : nextNeighbourNodes) {
            retVal.append(nextNeighbourNode.toString());
        }

        return retVal.toString();
    }

}
