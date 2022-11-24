package symboltable;

import interpreter.DSLInterpreter;
import parser.AST.Node;

import java.util.List;

// TODO:
//  how to hook in implementation? -> siehe ASTdriven vorlesung
public class FunctionSymbol extends ScopedSymbol implements ICallable {

    private final Node astRootNode;

    /**
     *
     * @param astRootNode
     */
    public FunctionSymbol(String name, IScope parentScope, Node astRootNode, IType retType) {
        super(name, parentScope, retType);

        this.astRootNode = astRootNode;
    }

    // TODO: pass AstVisitor or Interpreter?
    //  Wie mit Argumenten umgehen? kann ja nicht einfach an visit-methode übergeben werden..
    //  vielleicht von außen auf Parameter-Stack von Interpreter draufschieben bzw. auf Parameter-Stack
    //  von "Runtime"->Zustand da drin speichern
    // TODO: wo sollen die nativen Funktionen leben? Am sinnvollsten wäre das, wenn das in Runtime
    //  implementiert wäre
    @Override
    public Object call(DSLInterpreter interpreter, List<Object> parameters) {
        // sketch:
        // for (var param : parameters ) {
        // interpreter.push_param(param);
        // }
        // this.astRootNode.accept(interpreter);
        // TODO: handle return value
        return null;
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.UserDefined;
    }
}
