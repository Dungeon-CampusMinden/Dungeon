/*
 * MIT License
 *
 * Copyright (c) 2022 Malte Reinsch, Florian Warzecha, Sebastian Steinmeyer, BC George, Carsten Gips
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package semanticanalysis;

import parser.ast.*;

import runtime.nativefunctions.NativeFunction;

public class FunctionCallResolver implements AstVisitor<Void> {
    SymbolTable symbolTable;
    StringBuilder errorStringBuilder;

    /**
     * Visit all relevant children of the rootNode and try to resolve function calls, which will add
     * a relation between the function call ASTNode and the function symbol (the definition)
     *
     * @param symbolTable the symbol table to use for resolving function names
     * @param rootNode the root node of the program in which to resolve function calls
     * @param errorStringBuilder string builder to append error strings to
     */
    public void resolveFunctionCalls(
            SymbolTable symbolTable, Node rootNode, StringBuilder errorStringBuilder) {

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
    public Void visit(ParamDefNode node) {
        node.getIdNode().accept(this);
        return null;
    }

    @Override
    public Void visit(FuncDefNode node) {
        for (var parameter : node.getParameters()) {
            parameter.accept(this);
        }
        node.getStmtBlock().accept(this);
        return null;
    }

    @Override
    public Void visit(PrototypeDefinitionNode node) {
        var componentDefinitionNode = node.getComponentDefinitionListNode();
        visitChildren(componentDefinitionNode);
        return null;
    }

    @Override
    public Void visit(AggregateValueDefinitionNode node) {
        var propertyDefinitionNode = node.getPropertyDefinitionListNode();
        visitChildren(propertyDefinitionNode);
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
    public Void visit(ReturnStmtNode node) {
        node.getInnerStmtNode().accept(this);
        return null;
    }

    @Override
    public Void visit(StmtBlockNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(FuncCallNode funcCall) {
        // resolve function definition in global scope
        String funcName = funcCall.getIdName();
        var funcSymbol = this.symbolTable.globalScope.resolve(funcName);
        // TODO: handle null symbol

        assert funcSymbol.getSymbolType() == Symbol.Type.Scoped;

        this.symbolTable.addSymbolNodeRelation(funcSymbol, funcCall);

        for (var parameter : funcCall.getParameters()) {
            parameter.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(IdNode node) {
        var funcSymbol = this.symbolTable.globalScope.resolve(node.getName());
        if (funcSymbol.getSymbolType() == Symbol.Type.Scoped
                && (funcSymbol instanceof FunctionSymbol || funcSymbol instanceof NativeFunction)) {
            this.symbolTable.addSymbolNodeRelation(funcSymbol, node);
        }

        return null;
    }

    @Override
    public Void visit(ConditionalStmtNodeIf node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(ConditionalStmtNodeIfElse node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(BinaryNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(MemberAccessNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(LogicOrNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(LogicAndNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(EqualityNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(ComparisonNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(TermNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(FactorNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(UnaryNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(AssignmentNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(ListDefinitionNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(SetDefinitionNode node) {
        visitChildren(node);
        return null;
    }

    // region ASTVisitor implementation for nodes unrelated to function call resolution
    @Override
    public Void visit(DecNumNode node) {
        return null;
    }

    @Override
    public Void visit(NumNode node) {
        return null;
    }

    @Override
    public Void visit(StringNode node) {
        return null;
    }

    @Override
    public Void visit(DotDefNode node) {
        return null;
    }

    @Override
    public Void visit(EdgeRhsNode node) {
        return null;
    }

    @Override
    public Void visit(EdgeStmtNode node) {
        return null;
    }

    @Override
    public Void visit(EdgeOpNode node) {
        return null;
    }

    @Override
    public Void visit(BoolNode node) {
        return null;
    }
    // endregion
}
