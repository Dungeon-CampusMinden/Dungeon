package nodes;

public class BaseNode extends INode {

    public Types baseType;
    public boolean boolVal;
    public int intVal;
    public String stringVal;

    public BaseNode(String type, Types baseType) {
        super(type);
        this.baseType = baseType;
    }
    @Override
    public String toString(){
        StringBuilder retVal = new StringBuilder();
        switch (baseType) {
            case INTEGER:
                retVal = new StringBuilder(Integer.toString(intVal));
                break;
            case BOOLEAN:
                if (boolVal){
                    retVal = new StringBuilder("True");
                } else {
                    retVal = new StringBuilder("False");
                }
                break;
            default:
                System.out.println("Unsupported base type.");
                break;
        }

        for (INode nextNeighbourNode : nextNeighbourNodes) {
            retVal.append(nextNeighbourNode.toString());
        }

        return retVal.toString();
    }
}
