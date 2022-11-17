package parser.AST;

import java.util.ArrayList;

public class TypeSpecifierNode extends Node {
    public enum BuiltInType {
        NONE,
        QuestConfig
    }

    public enum SpecifierType {
        NONE,
        BuiltIn,
        IdForType
    }

    private final BuiltInType builtInType;
    private final SpecifierType specifierType;

    public TypeSpecifierNode(BuiltInType builtInType, SourceFileReference sourceFileReference) {
        super(Type.TypeSpecifier, sourceFileReference);
        this.builtInType = builtInType;
        this.specifierType = SpecifierType.BuiltIn;
    }

    public TypeSpecifierNode(Node idNodeAsType) {
        super(Type.TypeSpecifier, new ArrayList<>(1));
        this.children.add(idNodeAsType);
        this.builtInType = BuiltInType.NONE;
        this.specifierType = SpecifierType.IdForType;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
