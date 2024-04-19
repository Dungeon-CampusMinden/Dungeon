package dsl.semanticanalysis.symbol;

import dsl.annotation.DSLTypeProperty;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

/** This Symbol enables the usage {@link IDSLExtensionProperty} in {@link AggregateType}s. */
public class PropertySymbol extends Symbol {
  private IDSLExtensionProperty<?, ?> property;
  private final boolean settable;
  private final boolean gettable;

  /**
   * WTF? .
   *
   * @return foo
   */
  public boolean isSettable() {
    return settable;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public boolean isGettable() {
    return gettable;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public IDSLExtensionProperty<?, ?> getProperty() {
    return property;
  }

  /**
   * WTF? .
   *
   * @param symbolName foo
   * @param parentScope foo
   * @param dataType foo
   * @param property foo
   */
  public PropertySymbol(
      String symbolName, IScope parentScope, IType dataType, IDSLExtensionProperty<?, ?> property) {
    super(symbolName, parentScope, dataType);
    this.property = property;

    var annotation = property.getClass().getAnnotation(DSLTypeProperty.class);
    this.settable = annotation.isSettable();
    this.gettable = annotation.isGettable();
  }
}
