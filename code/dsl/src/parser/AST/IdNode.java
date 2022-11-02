package parser.AST;

public class IdNode extends Node {
    private final String name;

    /**
     * Constructor
     *
     * @param name The literal value of the identifier
     * @param sourceFileReference Reference to the location of the identifier in the source file
     */
    public IdNode(String name, SourceFileReference sourceFileReference) {
        super(Type.Identifier, sourceFileReference);
        this.name = name;
    }

    /**
     * @return The name of the identifier
     */
    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
