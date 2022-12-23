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

package semanticAnalysis;

import parser.AST.AstVisitor;
import parser.AST.FuncCallNode;
import parser.AST.Node;
import parser.AST.ObjectDefNode;
import parser.AST.PropertyDefNode;

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
        var funcSymbol = this.symbolTable.globalScope.resolve(funcName);
        // TODO: handle null symbol

        assert funcSymbol.getSymbolType() == Symbol.Type.Scoped;

        // TODO: or link to ID?
        this.symbolTable.addSymbolNodeRelation(funcSymbol, funcCall);

        return null;
    }
}
