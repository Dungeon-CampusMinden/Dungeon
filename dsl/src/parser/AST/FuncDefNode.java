package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class FuncDefNode extends Node {
    public final int idIdx = 0;
    public final int paramListIdx = 1;
    public final int retTypeIdx = 2;
    public final int stmtListIdx = 3;

    /**
     * Getter for the AstNode corresponding to the identifier of the defined function
     *
     * @return AstNode corresponding to the identifier of the defined function
     */
    public Node getId() {
        return this.getChild(idIdx);
    }

    /**
     * Getter for the name of the defined function
     *
     * @return Name of the defined function as String
     */
    public String getIdName() {
        return ((IdNode) this.getChild(idIdx)).getName();
    }

    /**
     * Getter for the AstNode corresponding to the return type of the function definition
     *
     * @return AstNode corresponding to the return type of the function definition
     */
    public Node getRetTypeId() {
        return this.getChild(retTypeIdx);
    }

    /**
     * Getter for the name of return type of the function definition
     *
     * @return Name of the return type as String
     */
    public String getRetTypeName() {
        return ((IdNode) this.getChild(retTypeIdx)).getName();
    }

    /**
     * Getter for the AstNodes corresponding to the stmts of the function definition
     *
     * @return List of the AstNodes corresponding to the stmts of the function definition
     */
    public List<Node> getStmts() {
        return this.children.get(stmtListIdx).getChildren();
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
    public FuncDefNode(Node id, Node paramList, Node retType, Node stmtList) {
        super(Type.FuncDef, new ArrayList<>(4));

        this.children.add(id);
        this.children.add(paramList);
        this.children.add(retType);
        this.children.add(stmtList);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
