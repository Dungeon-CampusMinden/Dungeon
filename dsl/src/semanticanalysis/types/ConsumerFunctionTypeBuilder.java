package semanticanalysis.types;

import interpreter.DSLInterpreter;
import parser.ast.FuncDefNode;
import runtime.IMemorySpace;
import runtime.RuntimeEnvironment;
import runtime.Value;
import semanticanalysis.FunctionSymbol;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Builder for a {@link FunctionType} for a callback defined by the {@link
 * java.util.function.Consumer} interface
 */
public class ConsumerFunctionTypeBuilder implements IFunctionTypeBuilder {
    // TODO: does not work
    class ConsumerCallbackAdapter {
        private RuntimeEnvironment rtEnv;
        private FunctionType functionType;
        private FuncDefNode funcDefNode;
        private IMemorySpace parentMemorySpace;
        private DSLInterpreter interpreter;

        public ConsumerCallbackAdapter(RuntimeEnvironment rtEnv, FunctionType functionType, FuncDefNode funcDefNode, IMemorySpace parentMemorySpace, DSLInterpreter interpreter) {
            this.rtEnv = rtEnv;
            this.functionType = functionType;
            this.funcDefNode = funcDefNode;
            this.parentMemorySpace = parentMemorySpace;
            this.interpreter = interpreter;
        }

        public Object call(Object param) {
            // cast parameter
            var paramObject = (Value)rtEnv.translateRuntimeObject(param, parentMemorySpace);
            var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);
            ArrayList<Value> params = new ArrayList<>();
            params.add(paramObject);
            var returnValue = (Value)interpreter.executeUserDefinedFunctionConcreteParameterValues((FunctionSymbol) functionSymbol, params);
            return returnValue.getInternalValue();
        }
    }
    public static ConsumerFunctionTypeBuilder instance = new ConsumerFunctionTypeBuilder();

    private ConsumerFunctionTypeBuilder() {}

    @Override
    public FunctionType buildFunctionType(Field field, TypeBuilder typeBuilder) {
        var genericType = field.getGenericType();

        var typeMap = typeBuilder.getJavaTypeToDSLTypeMap();

        var parameterizedType = (ParameterizedType) genericType;
        // the parameters will be the arguments for the function
        ArrayList<IType> parameterTypes =
                new ArrayList<>(parameterizedType.getActualTypeArguments().length);
        for (var parameterType : parameterizedType.getActualTypeArguments()) {
            IType dslType = TypeBuilder.getBasicDSLType((Class<?>) parameterType);
            if (null == dslType) {
                dslType = typeMap.get(parameterType);
            }
            if (null == dslType) {
                throw new RuntimeException("Type of parameter of Consumer could not be translated");
            } else {
                parameterTypes.add(dslType);
            }
        }
        return new FunctionType(BuiltInType.noType, parameterTypes);
    }

    @Override
    public Object buildCallbackAdapter(RuntimeEnvironment environment, FunctionType functionType, FuncDefNode funcDefNode, IMemorySpace parentMemorySpace, DSLInterpreter interpreter ) {
        return new ConsumerCallbackAdapter(environment, functionType, funcDefNode, parentMemorySpace, interpreter);
    }
}
