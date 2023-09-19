package semanticanalysis.types;

public interface IType {
    enum Kind {
        Basic,
        Aggregate,
        AggregateAdapted,
        FunctionType,
        SetType,
        ListType
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
