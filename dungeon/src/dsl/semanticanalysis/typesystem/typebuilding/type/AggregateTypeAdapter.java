package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.semanticanalysis.scope.IScope;
import java.lang.reflect.Method;

/** AggregateTypeAdapter. */
public class AggregateTypeAdapter extends AggregateType {

  final Method builderMethod;

  /**
   * A method that returns the builderMethod.
   *
   * @return the builderMethod
   */
  public Method builderMethod() {
    return builderMethod;
  }

  /**
   * Constructor.
   *
   * @param name foo
   * @param parentScope foo
   * @param originType foo
   * @param builderMethod foo
   */
  @SuppressWarnings("unchecked")
  public AggregateTypeAdapter(
      String name, IScope parentScope, Class<?> originType, Method builderMethod) {
    super(name, parentScope, originType);
    this.builderMethod = builderMethod;
  }

  @Override
  public Kind getTypeKind() {
    return Kind.AggregateAdapted;
  }
}
