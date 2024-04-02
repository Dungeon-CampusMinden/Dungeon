package dsl.runtime;

import dsl.interpreter.mockecs.*;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.scope.Scope;
import org.junit.Test;

public class TestGameEnvironment {
  @Test(expected = RuntimeException.class)
  public void adaptedInstancingNameClash() {
    var env = new GameEnvironment();

    // ExternalTypeBuilderMultiParam and OtherExternalTypeBuilderMultiParam are using the same
    // name for type reference in the DSL typesystem (the name-attribute is used to force
    // OtherExternalType to be references as 'external_type`) -> this should trigger an
    // exception
    env.typeBuilder().registerTypeAdapter(ExternalTypeBuilderMultiParam.class, Scope.NULL);
    var adapterType =
        env.typeBuilder().createDSLTypeForJavaTypeInScope(Scope.NULL, ExternalType.class);

    env.typeBuilder().registerTypeAdapter(OtherExternalTypeBuilderMultiParam.class, Scope.NULL);
    var otherAdapterType =
        env.typeBuilder().createDSLTypeForJavaTypeInScope(Scope.NULL, OtherExternalType.class);

    var externalComponentType =
        env.typeBuilder()
            .createDSLTypeForJavaTypeInScope(Scope.NULL, TestComponentWithExternalType.class);
    env.loadTypes(externalComponentType, adapterType, otherAdapterType);
  }
}
