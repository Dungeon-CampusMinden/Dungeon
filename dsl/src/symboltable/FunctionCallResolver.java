package symboltable;

import parser.AST.*;

public class FunctionCallResolver implements AstVisitor<Void> {
    SymbolTable symbolTable;
    StringBuilder errorStringBuilder;

    public void resolveFunctionCalls(
        SymbolTable symbolTable,
        Node rootNode,
        StringBuilder errorStringBuilder) {

        this.symbolTable = symbolTable;
        this.errorStringBuilder = errorStringBuilder;
        rootNode.accept(this);
    }

    @Override
    public Void visit(Node node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(ObjectDefNode node) {
        for (var def : node.getPropertyDefinitions()) {
            def.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(PropertyDefNode node) {
        node.getStmtNode().accept(this);
        return null;
    }

    @Override
    public Void visit(FuncCallNode funcCall) {
        // resolve function definition in global scope
        String funcName = funcCall.getIdName();
        var funcSymbol = this.symbolTable.globalScope.Resolve(funcName);
        // TODO: handle null symbol

        assert funcSymbol.getSymbolType() == Symbol.Type.Scoped;

        // TODO: or link to ID?
        this.symbolTable.addSymbolNodeRelation(funcSymbol, funcCall);

        return null;
    }
}
