package dslnativefunction;

import interpreter.DSLInterpreter;
import parser.ast.Node;
import runtime.AggregateValue;
import runtime.Prototype;
import runtime.RuntimeEnvironment;
import runtime.Value;
import runtime.nativefunctions.NativeFunction;
import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.Scope;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.FunctionType;

import java.util.List;

public class NativeBuildQuestItem extends NativeFunction {
    public static NativeBuildQuestItem func = new NativeBuildQuestItem(Scope.NULL);

    /**
     * Constructor
     *
     * @param parentScope parent scope of this function
     */
    private NativeBuildQuestItem(IScope parentScope) {
        super(
            "build_quest_item",
            parentScope,
            new FunctionType(BuiltInType.noType, Prototype.ITEM_PROTOTYPE));

        // bind parameters
        Symbol param = new Symbol("param", this, Prototype.ITEM_PROTOTYPE);
        this.bind(param);
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
        assert parameters != null && parameters.size() > 0;

        RuntimeEnvironment rtEnv = interpreter.getRuntimeEnvironment();
        Value param = (Value) parameters.get(0).accept(interpreter);
        if (param.getDataType() != Prototype.ITEM_PROTOTYPE) {
            throw new RuntimeException(
                "Wrong type ('"
                    + param.getDataType().getName()
                    + "') of parameter for call of build_quest_item()!");
        } else {
            // TODO: make this work
            var dslItemInstance =
                (AggregateValue) interpreter.instantiateDSLValue((Prototype) param);
            var questItemType = (AggregateType) rtEnv.getGlobalScope().resolve("quest_item");
            var questItemObject = interpreter.instantiateRuntimeValue(dslItemInstance, questItemType);

            return rtEnv.translateRuntimeObject(questItemObject, interpreter.getCurrentMemorySpace());
        }
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.Native;
    }
}
