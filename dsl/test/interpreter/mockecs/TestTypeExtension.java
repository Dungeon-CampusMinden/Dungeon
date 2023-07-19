package interpreter.mockecs;

import semanticanalysis.types.typeextension.DSLExtensionMethod;
import semanticanalysis.types.typeextension.DSLExtensionProperty;
import semanticanalysis.types.typeextension.DSLTypeExtension;

@DSLTypeExtension(type=Entity.class)
public class TestTypeExtension {
    @DSLExtensionMethod
    String giveMeYourName(Entity instance) {
        return instance.getName();
    }

    @DSLExtensionProperty
    TestComponent2 testComponent2(Entity instance) {
        for (var component : instance.components) {
            if (component instanceof TestComponent2) {
                return (TestComponent2)component;
            }
        }
        return null;
    }
}
