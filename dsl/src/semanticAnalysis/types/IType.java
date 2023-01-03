package semanticAnalysis.types;

public interface IType {
    enum Kind {
        Basic,
        Aggregate
    }

    /**
     * Getter for the type name
     *
     * @return the name of the type
     */
    String getName();

    /** */
    Kind getTypeKind();
}
