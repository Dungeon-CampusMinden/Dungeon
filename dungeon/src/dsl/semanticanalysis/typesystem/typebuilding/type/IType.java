package dsl.semanticanalysis.typesystem.typebuilding.type;

/** IType. */
public interface IType {
  /** The kind of the type. */
  enum Kind {
    /** Basic type. */
    Basic,
    /** Aggregate type. */
    Aggregate,
    /** Aggregate adapted type. */
    AggregateAdapted,
    /** Function type. */
    FunctionType,
    /** Enum type. */
    SetType,
    /** Enum type. */
    ListType,
    /** Enum type. */
    MapType,
    /** Enum type. */
    EnumType
  }

  /**
   * Getter for the type name.
   *
   * @return the name of the type
   */
  String getName();

  /**
   * Get the kind of the type.
   *
   * @return the kind of the type.
   */
  Kind getTypeKind();
}
