package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.semanticanalysis.scope.IScope;
import java.lang.reflect.Method;

public class AggregateTypeAdapter extends AggregateType {

  final Method builderMethod;

  public Method builderMethod() {
    return builderMethod;
  }

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
