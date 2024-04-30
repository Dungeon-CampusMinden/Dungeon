package dslinterop.dslnativefunction;

import core.Component;
import core.Entity;
import dsl.interpreter.DSLInterpreter;
import dsl.parser.ast.Node;
import dsl.runtime.callable.ICallable;
import dsl.runtime.callable.NativeFunction;
import dsl.runtime.environment.RuntimeEnvironment;
import dsl.runtime.value.AggregateValue;
import dsl.runtime.value.PrototypeValue;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.typesystem.instantiation.TypeInstantiator;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** WTF? . */
public class NativeInstantiateNamed extends NativeFunction {
  /** Instance of this class. */
  public static NativeInstantiateNamed func = new NativeInstantiateNamed(Scope.NULL);

  /**
   * Constructor.
   *
   * @param parentScope parent scope of this function
   */
  private NativeInstantiateNamed(IScope parentScope) {
    super(
        "instantiate_named",
        parentScope,
        new FunctionType(BuiltInType.noType, PrototypeValue.PROTOTYPE, BuiltInType.stringType));
  }

  @Override
  public Object call(DSLInterpreter interpreter, List<Node> parameters) {
    assert parameters != null && parameters.size() > 0;

    RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();

    var parameterValues = interpreter.evaluateNodes(parameters);
    Value prototypeValue = parameterValues.get(0);
    Value nameValue = parameterValues.get(1);

    if (prototypeValue.getDataType() != PrototypeValue.PROTOTYPE) {
      throw new RuntimeException(
          "Wrong type ('"
              + prototypeValue.getDataType().getName()
              + "') of parameter for call of instantiate()!");
    } else {
      var dslEntityInstance =
          (AggregateValue) interpreter.instantiateDSLValue((PrototypeValue) prototypeValue);
      var entityType = (AggregateType) rtEnv.getGlobalScope().resolve("entity");
      var entityObject =
          (core.Entity) interpreter.instantiateRuntimeValue(dslEntityInstance, entityType);
      entityObject.name(nameValue.toString());

      TypeInstantiator instantiator = interpreter.getRuntimeEnvironment().getTypeInstantiator();

      String contextName = "entity";
      instantiator.pushContextMember(contextName, entityObject);

      List<Map.Entry<String, Value>> laterEntries = new ArrayList<>();

      for (var valueEntry : dslEntityInstance.getMemorySpace().getValueSet()) {
        if (valueEntry.getKey().equals(Value.THIS_NAME)) {
          continue;
        }

        Value memberValue = valueEntry.getValue();
        // TODO: temporary fix!!!
        if (memberValue.getDataType().getName().equals("ai_component")) {
          laterEntries.add(valueEntry);
          continue;
        }

        if (memberValue instanceof AggregateValue) {
          // TODO: this is needed, because Prototype does not extend AggregateType
          // currently,
          //  which should be fixed
          AggregateType membersOriginalType =
              interpreter.getOriginalTypeOfPrototype((PrototypeValue) memberValue.getDataType());

          // instantiate object as a new java Object
          Object memberObject =
              interpreter.instantiateRuntimeValue(
                  (AggregateValue) memberValue, membersOriginalType);
          try {
            Component component = (Component) memberObject;
            Entity entity = (Entity) entityObject;
            entity.add(component);
          } catch (ClassCastException ex) {
            //
          }
        }
      }

      for (var entry : laterEntries) {
        AggregateValue memberValue = (AggregateValue) entry.getValue();
        AggregateType membersOriginalType =
            interpreter.getOriginalTypeOfPrototype((PrototypeValue) memberValue.getDataType());

        // instantiate object as a new java Object
        Object memberObject =
            interpreter.instantiateRuntimeValue((AggregateValue) memberValue, membersOriginalType);
        try {
          Component component = (Component) memberObject;
          Entity entity = (Entity) entityObject;
          entity.add(component);
        } catch (ClassCastException ex) {
          //
        }
      }

      instantiator.removeContextMember(contextName);

      return rtEnv.translateRuntimeObject(entityObject, interpreter.getCurrentMemorySpace());
    }
  }

  @Override
  public ICallable.Type getCallableType() {
    return ICallable.Type.Native;
  }
}
