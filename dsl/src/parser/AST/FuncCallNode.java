package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class FuncCallNode extends Node {
    public final int idIdx = 0;
    public final int paramListIdx = 1;

    /**
     * Getter for the AstNode corresponding to the identifier of the function call
     *
     * @return AstNode corresponding to the identifier of the function call
     */
    public Node getId() {
        return this.getChild(idIdx);
    }

    /**
     * Getter for the name of the called function
     *
     * @return Name of the called function as String
     */
    public String getIdName() {
        return ((IdNode) this.getChild(idIdx)).getName();
    }

    /**
     * Getter for the AstNodes corresponding to the parameters of the function call
     *
     * @return List of the AstNodes corresponding to the parameters of the function call
     */
    public List<Node> getParameters() {
        return this.children.get(paramListIdx).getChildren();
    }

    /**
     * Constructor
     *
     * @param id The AstNode corresponding to the identifier of the called function
     * @param paramList The AstNode corresponding to the parameter list of the function call
     */
    public FuncCallNode(Node id, Node paramList) {
        super(Type.FuncCall, new ArrayList<>(paramList.getChildren().size() + 1));

        this.children.add(id);
        this.children.add(paramList);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
