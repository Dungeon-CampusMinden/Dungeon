package dsl.semanticanalysis.typesystem.type;

public interface IType {
    enum Kind {
        Basic,
        Aggregate,
        AggregateAdapted,
        FunctionType,
        SetType,
        ListType,
        MapType,
        EnumType
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
