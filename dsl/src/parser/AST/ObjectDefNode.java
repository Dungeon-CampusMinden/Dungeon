package parser.AST;

import java.util.ArrayList;
import java.util.List;

public class ObjectDefNode extends Node {
    public final int typeSpecifierIdx = 0;
    public final int idIdx = 1;
    public final int propertyDefListIdx = 2;

    // TODO: is this still needed? This is likely to be revised once
    //  a proper type system with typechecking is implemented
    public enum Type {
        NONE,
        GrammarBuiltInType,
        IdForType
    }

    private final Type type;

    /**
     * Getter for the definition type
     *
     * @return the definition type of this definition
     */
    public Type getDefinitionType() {
        return this.type;
    }

    /**
     * Getter for the AstNode corresponding to the type specifier
     *
     * @return the AstNode corresponding to the type specifier
     */
    public Node getTypeSpecifier() {
        return this.getChild(typeSpecifierIdx);
    }

    /**
     * Getter for the name of the type specifier
     *
     * @return name of the type specifier as String
     */
    public String getTypeSpecifierName() {
        var typeSpecifier = this.getChild(typeSpecifierIdx);
        return ((IdNode) typeSpecifier).getName();
    }

    /**
     * Getter for the AstNode corresponding to the name of the object definition
     *
     * @return AstNode corresponding to the name of the object definition
     */
    public Node getId() {
        return this.getChild(idIdx);
    }

    /**
     * Getter for the name of the object definition as String
     *
     * @return the name of the object definition
     */
    public String getIdName() {
        return ((IdNode) this.getChild(idIdx)).getName();
    }

    /**
     * Getter for a List of AstNodes corresponding to property definitions inside the object
     * definition
     *
     * @return A List of AstNodes corresponding to property definitions
     */
    public List<Node> getPropertyDefinitions() {
        return this.children.get(propertyDefListIdx).getChildren();
    }

    /**
     * Constructor
     *
     * @param typeSpecifier The AstNode corresponding to the type specifier
     * @param id The AstNode corresponding to the identifier
     * @param propertyDefList The AstNode corresponding to the property definition list
     * @param type The {@link Type} of object definition
     */
    public ObjectDefNode(Node typeSpecifier, Node id, Node propertyDefList, Type type) {
        super(
                Node.Type.ObjectDefinition,
                new ArrayList<>(propertyDefList.getChildren().size() + 2));
        this.type = type;

        this.children.add(typeSpecifier);
        this.children.add(id);
        this.children.add(propertyDefList);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
