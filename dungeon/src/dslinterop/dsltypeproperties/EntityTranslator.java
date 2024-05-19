package dslinterop.dsltypeproperties;

import core.Entity;
import dsl.runtime.interop.IObjectToValueTranslator;
import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.value.AggregateValue;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;

/** This class translates an {@link Entity}-Object into a DSL Value for. */
public class EntityTranslator implements IObjectToValueTranslator {
  /** The only instance of this class. */
  public static EntityTranslator instance = new EntityTranslator();

  private EntityTranslator() {}

  /**
   * Iterates over all components of the passed Entity, resolves the name of the component in passed
   * IEnvironment, translates each component (for which the type can be resolved) into a DSL-Value
   * and binds the Component-Value in the MemorySpace of the Entity-Value.
   *
   * @param object The {@link Entity} instance to translate
   * @param parentMemorySpace The {@link IMemorySpace} in which the translated value should be
   *     created
   * @param environment The {@link IEnvironment}, which will be used to resolve types
   * @return The translated value
   */
  @Override
  public Value translate(Object object, IMemorySpace parentMemorySpace, IEnvironment environment) {
    var entity = (Entity) object;
    // get datatype for entity
    var entityType = environment.getGlobalScope().resolve("entity");

    if (!(entityType instanceof AggregateType)) {
      throw new RuntimeException("The resolved symbol for 'entity' is not an AggregateType!");
    } else {
      // create aggregateValue for entity
      var value = new AggregateValue((AggregateType) entityType, parentMemorySpace, entity);

      entity
          .componentStream()
          .forEach(
              (component) -> {
                var aggregateMemberValue =
                    environment
                        .getRuntimeObjectTranslator()
                        .translateRuntimeObject(component, value.getMemorySpace(), environment);

                if (aggregateMemberValue != Value.NONE) {
                  String componentDSLName = TypeBuilder.getDSLTypeName(component.getClass());
                  value.getMemorySpace().bindValue(componentDSLName, aggregateMemberValue);
                }
              });
      return value;
    }
  }
}
