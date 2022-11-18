/*
 * MIT License
 *
 * Copyright (c) 2022 Malte Reinsch, Florian Warzecha, Sebastian Steinmeyer, BC George, Carsten Gips
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package symboltable;

import java.util.Stack;
import parser.AST.*;

/** Creates a symbol table for an AST node for a DSL program */
public class SymbolTableParser implements AstVisitor<Void> {
    Stack<IScope> scopeStack = new Stack<>();
    StringBuilder errorStringBuilder;

    public class Result {
        public final SymbolTable symbolTable;
        public final boolean gotError;
        public final String errorString;

        public Result(SymbolTable symbolTable, String errorString) {
            this.symbolTable = symbolTable;
            this.errorString = errorString;
            this.gotError = !errorString.equals("");
        }
    }

    private IScope CurrentScope() {
        return scopeStack.peek();
    }

    private IScope GlobalScope() {
        return scopeStack.get(0);
    }

    private SymbolTable symbolTable;

    /**
     * Bind a symbol in the current scope and create an association between symbol and AST node
     *
     * @param symbol The symbol to bind
     * @param nodeOfSymbol The corresponding AST node
     * @return True, if the symbol was not bound in current scope, or false otherwise
     */
    private boolean Bind(Symbol symbol, Node nodeOfSymbol) {
        var currentScope = CurrentScope();
        if (!currentScope.Bind(symbol)) {
            return false;
        } else {
            symbolTable.addSymbolNodeRelation(symbol, nodeOfSymbol);
            return true;
        }
    }

    /**
     * Visit children node in node, create symbol table and resolve function calls
     *
     * @param node The node to walk
     * @return The symbol table for given node
     */
    public Result walk(Node node) {
        errorStringBuilder = new StringBuilder();
        scopeStack = new Stack<>();

        // push global scope
        scopeStack.push(new Scope());
        symbolTable = new SymbolTable(CurrentScope());

        node.accept(this);

        return new Result(symbolTable, errorStringBuilder.toString());
    }

    @Override
    public Void visit(Node node) {
        switch (node.type) {
            case Program:
                // First, bind all object definitions / variable assignments to enable object
                // references before
                // definition
                VariableBinder vb = new VariableBinder();
                vb.BindVariables(symbolTable, CurrentScope(), node, errorStringBuilder);

                visitChildren(node);
                break;
            case PropertyDefinitionList:
                visitChildren(node);
            default:
                break;
        }
        return null;
    }

    // if we reach this point in the walk, the given IdNode
    // is part of an expression and therefore needs to be resolved in the current scope.
    // Binding of the names of objects etc. is done selectively in Visit-methods for object
    // definitions
    @Override
    public Void visit(IdNode node) {
        var idName = node.getName();
        var symbol = CurrentScope().Resolve(idName);
        if (null == symbol) {
            errorStringBuilder.append(
                    "Reference of undefined identifier: "
                            + node.getName()
                            + " at: "
                            + node.getSourceFileReference()
                            + "\n");
        } else {
            var astNodeForSymbol = symbolTable.getCreationAstNode(symbol);
            var symDefLineNumber = astNodeForSymbol.getSourceFileReference().getLine();

            if (symDefLineNumber > node.getSourceFileReference().getLine()) {
                errorStringBuilder.append(
                        "Reference to variable '"
                                + idName
                                + "' in "
                                + node.getSourceFileReference()
                                + " before assignment in "
                                + astNodeForSymbol.getSourceFileReference()
                                + "\n");
                return null;
            }
            symbolTable.addSymbolNodeRelation(symbol, node);
        }
        return null;
    }

    @Override
    public Void visit(BinaryNode node) {
        for (var child : node.getChildren()) {
            child.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(PropertyDefNode node) {
        // TODO: check, if the property is accessible in the object -> datatype

        var stmtNode = node.getStmtNode();
        stmtNode.accept(this);

        return null;
    }

    @Override
    public Void visit(ObjectDefNode node) {
        for (var propertyDef : node.getPropertyDefinitions()) {
            propertyDef.accept(this);
        }
        return null;
    }
}
