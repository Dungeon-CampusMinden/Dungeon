package dsl.interpreter.mockecs;

import dsl.runtime.interop.IObjectToValueTranslator;
import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.value.AggregateValue;
import dsl.runtime.value.EncapsulatedField;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.Field;

/** WTF? . */
public class MockEntityTranslator implements IObjectToValueTranslator {
  /** WTF? . */
  public static MockEntityTranslator instance = new MockEntityTranslator();

  private MockEntityTranslator() {}

  @Override
  public Value translate(Object object, IMemorySpace parentMemorySpace, IEnvironment environment) {
    Entity entity = (Entity) object;
    // get datatype for entity
    var entityType = environment.getGlobalScope().resolve("entity");

    if (!(entityType instanceof AggregateType)) {
      throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
    } else {
      // create aggregateValue for entity
      var value = new AggregateValue((AggregateType) entityType, parentMemorySpace, entity);
      for (var component : entity.components) {
        var aggregateMemberValue =
            environment
                .getRuntimeObjectTranslator()
                .translateRuntimeObject(component, value.getMemorySpace(), environment);
        if (aggregateMemberValue != Value.NONE) {
          String componentDSLName = TypeBuilder.getDSLTypeName(component.getClass());
          value.getMemorySpace().bindValue(componentDSLName, aggregateMemberValue);
        }
      }
      try {
        Field field = Entity.class.getDeclaredField("idx");
        String fieldTypeName = TypeBuilder.getDSLTypeName(field.getType());
        IType type = (IType) environment.resolveInGlobalScope(fieldTypeName);
        EncapsulatedField encapsulatedField = new EncapsulatedField(type, field, object);
        value.getMemorySpace().bindValue("idx", encapsulatedField);
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      }
      return value;
    }
  }
}
