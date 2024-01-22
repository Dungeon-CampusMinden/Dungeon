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
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.instantiation.TypeInstantiator;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NativeInstantiate extends NativeFunction {
  // public static NativeInstantiate func = new NativeInstantiate(Scope.NULL);

  /**
   * Constructor
   *
   * @param parentScope parent scope of this function
   */
  public NativeInstantiate(IScope parentScope, IType entityType) {
    super("instantiate", parentScope, new FunctionType(entityType, PrototypeValue.PROTOTYPE));

    // bind parameters
    Symbol param = new Symbol("param", this, PrototypeValue.PROTOTYPE);
    this.bind(param);
  }

  @Override
  public Object call(DSLInterpreter interpreter, List<Node> parameters) {
    assert parameters != null && parameters.size() > 0;

    RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();
    Value param = (Value) parameters.get(0).accept(interpreter);

    if (param.getDataType() != PrototypeValue.PROTOTYPE) {
      throw new RuntimeException(
          "Wrong type ('"
              + param.getDataType().getName()
              + "') of parameter for call of instantiate()!");
    } else {
      var dslEntityInstance =
          (AggregateValue) interpreter.instantiateDSLValue((PrototypeValue) param);
      var entityType = (AggregateType) rtEnv.getGlobalScope().resolve("entity");
      var entityObject = interpreter.instantiateRuntimeValue(dslEntityInstance, entityType);

      TypeInstantiator instantiator = interpreter.getRuntimeEnvironment().getTypeInstantiator();

      String contextName = "entity";
      instantiator.pushContextMember(contextName, entityObject);

      List<Map.Entry<String, Value>> laterEntries = new ArrayList<>();

      for (var valueEntry : dslEntityInstance.getMemorySpace().getValueSet()) {
        if (valueEntry.getKey().equals(Value.THIS_NAME)) {
          continue;
        }
        Value memberValue = valueEntry.getValue();
        if (memberValue instanceof AggregateValue) {
          // TODO: temporary fix!!!
          if (memberValue.getDataType().getName().equals("ai_component")) {
            laterEntries.add(valueEntry);
            continue;
          }
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
