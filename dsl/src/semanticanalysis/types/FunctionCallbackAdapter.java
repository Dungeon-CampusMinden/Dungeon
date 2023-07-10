package semanticanalysis.types;

import interpreter.DSLInterpreter;
import parser.ast.FuncDefNode;
import runtime.IMemorySpace;
import runtime.RuntimeEnvironment;
import runtime.Value;
import semanticanalysis.FunctionSymbol;

import java.util.ArrayList;

public class FunctionCallbackAdapter {
    private RuntimeEnvironment rtEnv;
    private FunctionType functionType;
    private FuncDefNode funcDefNode;
    private IMemorySpace parentMemorySpace;
    private DSLInterpreter interpreter;

    public FunctionCallbackAdapter(RuntimeEnvironment rtEnv, FunctionType functionType, FuncDefNode funcDefNode, IMemorySpace parentMemorySpace, DSLInterpreter interpreter) {
        this.rtEnv = rtEnv;
        this.functionType = functionType;
        this.funcDefNode = funcDefNode;
        this.parentMemorySpace = parentMemorySpace;
        this.interpreter = interpreter;
    }

    public Object accept(Object param) {
        // cast parameter
        var paramObject = (Value) rtEnv.translateRuntimeObject(param, parentMemorySpace);
        var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);
        ArrayList<Value> params = new ArrayList<>();
        params.add(paramObject);
        var returnValue = (Value) interpreter.executeUserDefinedFunctionConcreteParameterValues((FunctionSymbol) functionSymbol, params);
        return returnValue.getInternalValue();
    }
}
