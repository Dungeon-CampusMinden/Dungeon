package runtime.nativefunctions;

import interpreter.DSLInterpreter;

import parser.ast.Node;

import runtime.Value;

import semanticanalysis.ICallable;
import semanticanalysis.IScope;
import semanticanalysis.Symbol;
import semanticanalysis.types.FunctionType;
import semanticanalysis.types.IDSLExtensionMethod;

import java.util.List;

public class ExtensionMethod extends Symbol implements ICallable {
    private final IDSLExtensionMethod<Object, Object> extensionMethod;

    public ExtensionMethod(
            String name,
            IScope parentScope,
            FunctionType functionType,
            IDSLExtensionMethod<Object, Object> callable) {
        super(name, parentScope, functionType);
        this.extensionMethod = callable;
    }

    @Override
    public Object call(DSLInterpreter interperter, List<Node> parameters) {
        // resolve "THIS_VALUE"
        Value instance = interperter.getCurrentMemorySpace().resolve(Value.THIS_NAME);
        Object instanceObject = instance.getInternalValue();

        // interpret parameters and extract internal values
        List<Object> parameterObjects =
                parameters.stream()
                        .map(p -> p.accept(interperter))
                        .map(o -> ((Value) o).getInternalValue())
                        .toList();

        return this.extensionMethod.call(interperter, instanceObject, parameterObjects);
    }

    @Override
    public ICallable.Type getCallableType() {
        return null;
    }

    @Override
    public FunctionType getFunctionType() {
        return null;
    }
}
