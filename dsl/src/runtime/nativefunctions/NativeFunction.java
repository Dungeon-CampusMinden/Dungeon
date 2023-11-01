package runtime.nativefunctions;

import interpreter.DSLInterpreter;
import parser.ast.Node;
import runtime.SetValue;
import runtime.Value;
import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.ScopedSymbol;
import semanticanalysis.Symbol;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.IType;

import java.util.ArrayList;
import java.util.List;

public abstract class NativeFunction extends ScopedSymbol implements ICallable {
    protected NativeFunction(String name, IScope parentScope, FunctionType type) {
        super(name, parentScope, type);

        // create generically named parameter symbols
        for (int i = 0; i < type.getParameterTypes().size(); i++) {
            IType parameterType = type.getParameterTypes().get(i);
            String parameterName = "param" + i;
            Symbol parameterSymbol = new Symbol(parameterName, this, parameterType);
            this.bind(parameterSymbol);
        }
    }

    public void overwriteFunctionType(FunctionType type) {
        this.dataType = type;
    }

    protected List<Value> evaluateParameters(DSLInterpreter interpreter, List<Node> parameterNodes) {
        ArrayList<Value> parameterValues = new ArrayList<>(parameterNodes.size());
        for (Node node : parameterNodes) {
            Value taskValue = (Value) node.accept(interpreter);
            parameterValues.add(taskValue);
        }
        return parameterValues;
    }

    protected List<Object> translateValuesToObjects(DSLInterpreter interpreter, List<Value> values) {
        ArrayList<Object> objects = new ArrayList<>(values.size());
        var instantiator = interpreter.getRuntimeEnvironment().getTypeInstantiator();
        for (Value value : values) {
            Object object = instantiator.instantiate(value);
            objects.add(object);
        }
        return objects;
    }

    @Override
    public FunctionType getFunctionType() {
        return (FunctionType) this.getDataType();
    }
}
