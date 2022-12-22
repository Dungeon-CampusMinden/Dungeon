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

import dslToGame.QuestConfig;
import java.util.Stack;
// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport
import parser.AST.*;
// CHECKSTYLE:ON: AvoidStarImport
import runtime.nativeFunctions.NativePrint;
import semanticAnalysis.typebulder.TypeBuilder;
import semanticAnalysis.types.AggregateType;
import semanticAnalysis.types.BuiltInType;

// TODO: enable dynamic loading of data types (for better testability)
/** Creates a symbol table for an AST node for a DSL program */
// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class SymbolTableParser implements AstVisitor<Void> {
    Stack<IScope> scopeStack = new Stack<>();
    StringBuilder errorStringBuilder;
    TypeBuilder typeBuilder = new TypeBuilder();

    public class Result {
        public final SymbolTable symbolTable;
        public final boolean gotError;
        public final String errorString;

        /**
         * Constructor. If the errorString is empty, gotError will be set to false
         *
         * @param symbolTable the symbol table
         * @param errorString the errorString which was generated during semantic analysis
         */
        public Result(SymbolTable symbolTable, String errorString) {
            this.symbolTable = symbolTable;
            this.errorString = errorString;
            this.gotError = !errorString.equals("");
        }
    }

    /**
     * Helper method for getting the current scope (the top of the scopeStack)
     *
     * @return the top of the scopeStack
     */
    private IScope currentScope() {
        return scopeStack.peek();
    }

    /**
     * Helper method for getting the global scope (the bottom of the scopeStack)
     *
     * @return the bottom of the scopeStack
     */
    private IScope globalScope() {
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
    private boolean bind(Symbol symbol, Node nodeOfSymbol) {
        var currentScope = currentScope();
        if (!currentScope.bind(symbol)) {
            return false;
        } else {
            symbolTable.addSymbolNodeRelation(symbol, nodeOfSymbol);
            return true;
        }
    }

    private void setupNativeFunctions() {
        globalScope().bind(NativePrint.func);
    }

    private void setupBuiltinTypes() {
        // setup builtin simple types
        globalScope().bind(BuiltInType.intType);
        globalScope().bind(BuiltInType.stringType);
        globalScope().bind(BuiltInType.graphType);
        globalScope().bind(BuiltInType.funcType);

        // setup builtin aggregate types
        var questConfigType = typeBuilder.createTypeFromClass(globalScope(), QuestConfig.class);

        globalScope().bind(questConfigType);
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
        symbolTable = new SymbolTable(currentScope());

        setupBuiltinTypes();
        setupNativeFunctions();

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
                vb.bindVariables(symbolTable, currentScope(), node, errorStringBuilder);

                visitChildren(node);

                FunctionCallResolver fcr = new FunctionCallResolver();
                fcr.resolveFunctionCalls(symbolTable, node, errorStringBuilder);
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
        var symbol = currentScope().resolve(idName);
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
                // TODO: is this needed?
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
        var propertyIdName = node.getIdName();

        // TODO: ensure, that no other scopes than the scopes of the type are checked
        var propertySymbol = currentScope().resolve(propertyIdName);
        if (propertySymbol == Symbol.NULL) {
            errorStringBuilder.append(
                    "no property with name " + propertyIdName + " could be found");
        } else {
            // link the propertySymbol in the dataType to the astNode of this concrete property
            // definition
            this.symbolTable.addSymbolNodeRelation(propertySymbol, node.getIdNode());
        }

        var stmtNode = node.getStmtNode();
        stmtNode.accept(this);

        // TODO: check, if the types of the property and the stmt are compatible

        return null;
    }

    @Override
    public Void visit(ObjectDefNode node) {
        // resolve the type of the object definition and push it on the stack
        var typeName = node.getTypeSpecifierName();
        var typeSymbol = globalScope().resolve(typeName);

        // TODO: errorhandling
        if (typeSymbol == Symbol.NULL) {
            errorStringBuilder.append("Could not resolve type " + typeName);
        } else if (typeSymbol.getSymbolType() != Symbol.Type.Scoped) {
            errorStringBuilder.append("Type " + typeName + " is not scoped!");
        } else {
            scopeStack.push((AggregateType) typeSymbol);

            for (var propertyDef : node.getPropertyDefinitions()) {
                propertyDef.accept(this);
            }
            scopeStack.pop();
        }
        return null;
    }
}
