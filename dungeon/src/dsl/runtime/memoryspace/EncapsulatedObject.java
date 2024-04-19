package dsl.runtime.memoryspace;

import dsl.runtime.value.AggregatePropertyValue;
import dsl.runtime.value.EncapsulatedField;
import dsl.runtime.value.PropertyValue;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.symbol.PropertySymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** WTF? . */
public class EncapsulatedObject extends Value implements IMemorySpace {
  private IMemorySpace parent;
  private AggregateType type;
  private Value thisValue = NONE;

  // TODO: should probably abstract all that away in a TypeFactory, which
  //  handles creation of encapsulated objects and other stuff
  private IEnvironment environment;
  private HashMap<String, Value> objectCache;

  /**
   * Constructor. Creates a new EncapsulatedObject with the given {@link AggregateType}.
   *
   * @param innerObject the object to encapsulate
   * @param type {@link AggregateType} of the Value represented by the new EncapsulatedObject (used
   *     for resolving member access)
   * @param environment the {@link IEnvironment} to use for resolving member access
   */
  public EncapsulatedObject(Object innerObject, AggregateType type, IEnvironment environment) {
    super(type, innerObject);

    this.type = type;
    this.environment = environment;
    this.objectCache = new HashMap<>();
  }

  @Override
  public boolean bindValue(String name, Value value) {
    if (name.equals(THIS_NAME)) {
      thisValue = value;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Value resolve(String name) {
    Value returnValue = NONE;
    if (objectCache.containsKey(name)) {
      return objectCache.get(name);
    }

    if (name.equals(THIS_NAME)) {
      return thisValue;
    }

    // lookup name
    Field correspondingField = this.type.getTypeMemberToField().getOrDefault(name, null);
    if (correspondingField != null) {
      // read field value
      correspondingField.setAccessible(true);
      try {
        var fieldValue = correspondingField.get(this.getInternalValue());

        // handle null
        if (fieldValue == null) {
          return NONE;
        }

        IType memberDSLType = null;
        if (this.dataType instanceof AggregateType aggregateType) {
          Symbol memberSymbol = aggregateType.resolve(name);
          memberDSLType = memberSymbol.getDataType();
        }
        if (memberDSLType == null) {
          memberDSLType = this.environment.getDSLTypeForClass(fieldValue.getClass());
        }

        // convert the read field value to a DSL 'Value'
        // this may require recursive creation of encapsulated objects,
        // if the field is a component for example

        if (memberDSLType != BuiltInType.noType) {
          switch (memberDSLType.getTypeKind()) {
            case Basic:
              // create encapsulated value (because the field is a POD-field, or
              // "basic type") -> linking the value to the field is only required
              // for setting the internal value
              // NOTE: this behaviour differs from the default translation of the
              // RuntimeObjectTranslator, because we know in this case, that the
              // resolved name is a member of the underlying object
              returnValue = new EncapsulatedField(memberDSLType, correspondingField, this.object);
              break;
            case AggregateAdapted:
            case Aggregate:
              returnValue =
                  environment
                      .getRuntimeObjectTranslator()
                      .translateRuntimeObject(fieldValue, this, this.environment);
              break;
            case FunctionType:
              returnValue = new EncapsulatedField(memberDSLType, correspondingField, this.object);
              break;
          }
          // cache it
          this.objectCache.put(name, returnValue);
        }
      } catch (IllegalAccessException e) {
        // TODO: handle
      }
    } else {
      // it may be a property
      Symbol symbol = type.resolve(name);
      if (symbol instanceof PropertySymbol propertySymbol) {
        if (symbol.getDataType().getTypeKind().equals(IType.Kind.Aggregate)
            || symbol.getDataType().getTypeKind().equals(IType.Kind.AggregateAdapted)) {
          returnValue =
              new AggregatePropertyValue(
                  symbol.getDataType(),
                  (IDSLExtensionProperty<Object, Object>) propertySymbol.getProperty(),
                  this.object,
                  MemorySpace.NONE,
                  this.environment);
        } else {
          returnValue =
              new PropertyValue(
                  symbol.getDataType(),
                  (IDSLExtensionProperty<Object, Object>) propertySymbol.getProperty(),
                  this.object);
        }
      }
    }
    return returnValue;
  }

  @Override
  public Value resolve(String name, boolean resolveInParent) {
    return resolve(name);
  }

  @Override
  public void delete(String name) {
    if (name.equals(THIS_NAME)) {
      thisValue = NONE;
    } else {
      throw new UnsupportedOperationException(
          "Deleting a value from an Encapsulated Object is not supported!");
    }
  }

  // TODO: define the semantics for this based on, if the value is a POD type or
  //  a complex type -> what happens, if we want to set a component of an
  //  entity or a complex datatype of a component (e.g. Point)?!
  //  (will be done in https://github.com/Dungeon-CampusMinden/Dungeon/issues/156)
  @Override
  public boolean setValue(String name, Value value) {
    // handle this value
    if (name.equals(THIS_NAME)) {
      thisValue = value;
    }

    Field correspondingField = this.type.getTypeMemberToField().getOrDefault(name, null);
    if (correspondingField == null) {
      return false;
    } else {
      // TODO: this should only be possible for PODs
      // read field value
      correspondingField.setAccessible(true);
      try {
        correspondingField.set(this.getInternalValue(), value.getInternalValue());
      } catch (IllegalAccessException e) {
        // TODO: handle
        return false;
      }
    }
    return false;
  }

  @Override
  public Set<Map.Entry<String, Value>> getValueSet() {
    // TODO
    return null;
  }
}
