package dsl.runtime;

import dsl.interpreter.mockecs.*;
import dsl.semanticanalysis.environment.GameEnvironment;
import dsl.semanticanalysis.scope.Scope;
import org.junit.Test;

/** WTF? . */
public class TestGameEnvironment {
  /** WTF? . */
  @Test(expected = RuntimeException.class)
  public void adaptedInstancingNameClash() {
    var env = new GameEnvironment();

    // ExternalTypeBuilderMultiParam and OtherExternalTypeBuilderMultiParam are using the same
    // name for type reference in the DSL typesystem (the name-attribute is used to force
    // OtherExternalType to be references as 'external_type`) -> this should trigger an
    // exception
    env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilderMultiParam.class, Scope.NULL);
    var adapterType =
        env.getTypeBuilder().createDSLTypeForJavaTypeInScope(Scope.NULL, ExternalType.class);

    env.getTypeBuilder().registerTypeAdapter(OtherExternalTypeBuilderMultiParam.class, Scope.NULL);
    var otherAdapterType =
        env.getTypeBuilder().createDSLTypeForJavaTypeInScope(Scope.NULL, OtherExternalType.class);

    var externalComponentType =
        env.getTypeBuilder()
            .createDSLTypeForJavaTypeInScope(Scope.NULL, TestComponentWithExternalType.class);
    env.loadTypes(externalComponentType, adapterType, otherAdapterType);
  }
}
