package runtime;

import interpreter.mockecs.*;

import org.junit.Test;

import semanticanalysis.Scope;

public class TestGameEnvironment {
    @Test(expected = RuntimeException.class)
    public void adaptedInstancingNameClash() {
        var env = new GameEnvironment();

        // ExternalTypeBuilderMultiParam and OtherExternalTypeBuilderMultiParam are using the same
        // name for type reference in the DSL typesystem (the name-attribute is used to force
        // OtherExternalType to be references as 'external_type`) -> this should trigger an
        // exception
        env.getTypeBuilder().registerTypeAdapter(ExternalTypeBuilderMultiParam.class, Scope.NULL);
        var adapterType = env.getTypeBuilder().createTypeFromClass(Scope.NULL, ExternalType.class);

        env.getTypeBuilder()
                .registerTypeAdapter(OtherExternalTypeBuilderMultiParam.class, Scope.NULL);
        var otherAdapterType =
                env.getTypeBuilder().createTypeFromClass(Scope.NULL, OtherExternalType.class);

        var externalComponentType =
                env.getTypeBuilder()
                        .createTypeFromClass(Scope.NULL, TestComponentWithExternalType.class);
        env.loadTypes(externalComponentType, adapterType, otherAdapterType);
    }
}
