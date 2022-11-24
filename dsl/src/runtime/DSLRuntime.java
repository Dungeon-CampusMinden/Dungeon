package runtime;

import symboltable.ICallable;
import symboltable.SymbolTable;

import java.util.Stack;

public class DSLRuntime {
    private final Stack<MemorySpace> memoryStack;
    private MemorySpace globalSpace;

    // TODO: add entry-point for game-object traversal
    public DSLRuntime() {
        memoryStack = new Stack<>();
        globalSpace = new MemorySpace();
        memoryStack.push(globalSpace);
    }

    // TODO: how to handle globally defined objects?
    //  statisch alles auswerten, was geht? und dann erst auswerten, wenn abgefragt (lazyeval?)
    //  wie wird order of operation vorgegeben? einfach von oben nach unten? oder nach referenz von
    //  objekt?
    public void initialize(SymbolTable symbolTable) {
        for (var symbol: symbolTable.GetGlobalScope().GetSymbols()) {
            if (symbol instanceof ICallable) {
                var callableType = ((ICallable)symbol).getCallableType();
                if (callableType == ICallable.Type.Native) {
                    this.globalSpace.bindFromSymbol(symbol);
                } else if (callableType == ICallable.Type.UserDefined) {
                    // TODO: if userDefined -> reference AST -> how to?
                    //  subclass of value?
                }
            }
        }
    }
}
