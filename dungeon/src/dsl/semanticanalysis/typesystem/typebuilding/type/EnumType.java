package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;

/**
 * Represents an {@link Enum} in the dsl type system. The variants of the enum are stored as {@link
 * Symbol} in the scope of this type.
 */
public class EnumType extends ScopedSymbol implements IType {
  protected Class<? extends Enum<?>> originType;

  /**
   * Constructor.
   *
   * @param name The name of this {@link EnumType}.
   * @param parentScope The scope in which this type is declared.
   * @param originType The corresponding Java {@link Enum}-type of this {@link EnumType}
   */
  public EnumType(String name, IScope parentScope, Class<? extends Enum<?>> originType) {
    super(name, parentScope, null);
    this.originType = originType;
  }

  /**
   * @return The corresponding Java {@link Enum}-type of this {@link EnumType}.
   */
  public Class<? extends Enum<?>> getOriginType() {
    return originType;
  }

  @Override
  public Kind getTypeKind() {
    return Kind.EnumType;
  }
}
