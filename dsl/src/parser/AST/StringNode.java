package parser.AST;

public class StringNode extends Node {
    private final String value;

    /**
     * Constructor
     *
     * @param value The literal value of the string
     * @param sourceFileReference Reference to the location of the identifier in the source file
     */
    public StringNode(String value, SourceFileReference sourceFileReference) {
        super(Type.StringLiteral, sourceFileReference);
        this.value = value;
    }

    /**
     * @return The value of the string
     */
    public String getValue() {
        return value;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
