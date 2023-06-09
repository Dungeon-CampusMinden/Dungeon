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
// CHECKSTYLE:ON: AvoidStarImport

import runtime.IEvironment;
import runtime.nativefunctions.NativeFunction;

import semanticanalysis.types.*;

import java.util.ArrayList;
import java.util.Stack;
// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport

/** Creates a symbol table for an AST node for a DSL program */
// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class SemanticAnalyzer implements AstVisitor<Void> {
    private SymbolTable symbolTable;
    private IEvironment environment;
    Stack<IScope> scopeStack = new Stack<>();
    StringBuilder errorStringBuilder = new StringBuilder();
    private boolean setup = false;

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

    public IEvironment getEnvironment() {
        return this.environment;
    }

    /**
     * Setup environment for semantic analysis (setup builtin types and native functions); use an
     * externally provided symbol table, which will be used and extended during semantic analysis
     *
     * @param environment environment to use for setup of built in types and native functions
     */
    public void setup(IEvironment environment) {
        if (setup) {
            return;
        }

        this.errorStringBuilder = new StringBuilder();
        this.scopeStack = new Stack<>();

        this.scopeStack.push(environment.getGlobalScope());
        this.symbolTable = environment.getSymbolTable();
        this.environment = environment;

        // ensure, that all FunctionTypes of the native functions are correctly bound
        // in the symbolTable and remove redundancies
        var globalScope = this.symbolTable.globalScope;
        for (var func : environment.getFunctions()) {
            if (func instanceof NativeFunction) {
                var funcType = (FunctionType) func.getDataType();
                var funcTypeSymbol = globalScope.resolve(funcType.getName());
                if (funcTypeSymbol.equals(Symbol.NULL)) {
                    globalScope.bind(funcType);
                } else if (funcType.hashCode() != funcTypeSymbol.hashCode()) {
                    // use the funcType already in symbolTable
                    ((NativeFunction) func).overwriteFunctionType((FunctionType) funcTypeSymbol);
                }
            }
        }

        this.setup = true;
    }

    /**
     * Built in types (such as all BuiltIns and built in AggregateTypes such as quest_config) have
     * no parent Scope. This leads to situations, in which the topmost scope is such an aggregate
     * type. Resolving a reference to a global variable does not work in this context, so this
     * method implements a manual stack walk of the scope stack to deal with this case.
     *
     * @param name the name of the symbol to resolve
     * @return the resolved symbol or Symbol.NULL, if the name could not be resolved
     */
    private Symbol resolve(String name) {
        // if the type of the current scope is an AggregateType, first resolve in this type
        // and then resolve in the enclosing scope
        var symbol = Symbol.NULL;
        var stackIterator = this.scopeStack.listIterator(scopeStack.size());
        IScope scope;

        while (symbol.equals(Symbol.NULL) && stackIterator.hasPrevious()) {
            scope = stackIterator.previous();
            symbol = scope.resolve(name);
        }
        return symbol;
    }

    /**
     * Visit children node in node, create symbol table and resolve function calls
     *
     * @param node The node to walk
     * @return The symbol table for given node
     */
    public Result walk(Node node) {
        if (!setup) {
            errorStringBuilder.append("Symbol table parser was not setup with an environment");
            return new Result(symbolTable, errorStringBuilder.toString());
        }
        node.accept(this);

        return new Result(symbolTable, errorStringBuilder.toString());
    }

    @Override
    public Void visit(Node node) {
        switch (node.type) {
            case Program:
                // bind all type definitions
                TypeBinder tb = new TypeBinder();
                tb.bindTypes(environment, node, errorStringBuilder);

                // bind all object definitions / variable assignments to enable object
                // references before
                // definition
                VariableBinder vb = new VariableBinder();
                vb.bindVariables(symbolTable, globalScope(), node, errorStringBuilder);

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
        var symbol = resolve(idName);
        if (symbol.equals(Symbol.NULL)) {
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
    public Void visit(EntityTypeDefinitionNode node) {
        // resolve datatype of definition
        var typeName = node.getIdName();
        var typeSymbol = this.globalScope().resolve(typeName);
        if (typeSymbol.equals(Symbol.NULL) || typeSymbol == null) {
            errorStringBuilder.append("Could not resolve type " + typeName);
        } else {
            scopeStack.push((AggregateType) typeSymbol);
            for (var componentDef : node.getComponentDefinitionNodes()) {
                componentDef.accept(this);
            }
            scopeStack.pop();
        }
        return null;
    }

    // TODO: Symbols for members?
    @Override
    public Void visit(AggregateValueDefinitionNode node) {
        // push datatype of component
        // resolve in current scope, which will be datatype of game object definition
        IType membersType;
        String valueName = node.getIdName();

        // TODO: for an anonymous object (inline defined aggregate value), the memberSymbol will be
        // Symbol.NULL
        //  could just resolve the name as a datatype and resolve any member in it..
        //  but this is not really clean. Should make this explicit at a higher level

        // get the type of the aggregate value
        var memberSymbol = currentScope().resolve(valueName);
        if (memberSymbol == Symbol.NULL) {
            var type = this.environment.getGlobalScope().resolve(valueName);
            assert type instanceof AggregateType;
            membersType = (IType) type;
        } else {
            membersType = memberSymbol.getDataType();
        }

        // TODO: errorhandling
        if (membersType == Symbol.NULL || membersType == null) {
            errorStringBuilder.append("Could not resolve type " + "TODO");
        } else {
            // visit all property-definitions of the aggregate value definition
            scopeStack.push((AggregateType) membersType);
            for (var propertyDef : node.getPropertyDefinitionNodes()) {
                propertyDef.accept(this);
            }
            scopeStack.pop();
        }

        return null;
    }

    @Override
    public Void visit(PropertyDefNode node) {
        var propertyIdName = node.getIdName();

        // the current scope will be the type of the object definition
        var propertySymbol = currentScope().resolve(propertyIdName);
        if (propertySymbol == Symbol.NULL) {
            errorStringBuilder.append(
                    "no property with name " + propertyIdName + " could be found");
        } else {
            // link the propertySymbol in the dataType to the astNode of this concrete property
            // definition
            // this.symbolTable.addSymbolNodeRelation(propertySymbol, node.getIdNode());
            this.symbolTable.addSymbolNodeRelation(propertySymbol, node);
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

    private IType resolveType(String name) {
        var resolvedType = globalScope().resolve(name);
        if (resolvedType == Symbol.NULL) {
            errorStringBuilder.append("Type '" + name + "' could not be resolved");
        } else if (!(resolvedType instanceof IType)) {
            errorStringBuilder.append("Symbol of name '" + name + "' is no type");
        } else {
            return (IType) resolvedType;
        }
        return null;
    }

    @Override
    public Void visit(ParamDefNode node) {
        // current scope should be a function definition
        var resolvedParameter = currentScope().resolve(node.getIdName());
        if (resolvedParameter != Symbol.NULL) {
            errorStringBuilder.append(
                    "Parameter with name " + node.getIdName() + " was already defined");
        } else {
            // resolve parameters datatype
            IType parameterType = resolveType(node.getTypeName());

            Symbol parameterSymbol = new Symbol(node.getIdName(), currentScope(), parameterType);
            currentScope().bind(parameterSymbol);
        }
        return null;
    }

    @Override
    public Void visit(FuncCallNode node) {
        // we do not resolve the call itself here, only the parameters
        for (var parameter : node.getParameters()) {
            parameter.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FuncDefNode node) {
        // check, if symbol with the name was already bound
        var funcName = node.getIdName();
        var resolved = globalScope().resolve(funcName);
        if (resolved != Symbol.NULL) {
            errorStringBuilder.append(
                    "Identifier with name " + funcName + " is already bound in global scope!");
        } else {
            // resolve return value (if one was defined)
            IType returnType = BuiltInType.noType;
            if (node.getRetTypeId() != Node.NONE) {
                String returnTypeName = node.getRetTypeName();
                returnType = resolveType(returnTypeName);
            }

            // get types of parameters
            ArrayList<IType> parameterTypes = new ArrayList<>(node.getParameters().size());
            for (var paramDefNode : node.getParameters()) {
                var paramTypeName = ((ParamDefNode) paramDefNode).getTypeName();
                IType paramType = resolveType(paramTypeName);
                parameterTypes.add(paramType);
            }

            // create function signature type (as needed)
            String functionTypeName = FunctionType.calculateTypeName(returnType, parameterTypes);
            Symbol functionTypeSymbol = globalScope().resolve(functionTypeName);
            FunctionType functionType;

            if (functionTypeSymbol != Symbol.NULL) {
                functionType = (FunctionType) functionTypeSymbol;
            } else {
                functionType = new FunctionType(returnType, parameterTypes);
                globalScope().bind(functionType);
            }

            // create new function symbol
            var funcSymbol = new FunctionSymbol(funcName, globalScope(), node, functionType);
            globalScope().bind(funcSymbol);
            scopeStack.push(funcSymbol);

            // bind parameters
            for (var paramDefNode : node.getParameters()) {
                paramDefNode.accept(this);
            }

            // visit all stmts
            for (var stmt : node.getStmts()) {
                stmt.accept(this);
            }

            scopeStack.pop();
        }
        return null;
    }

    @Override
    public Void visit(ReturnStmtNode node) {
        node.getInnerStmtNode().accept(this);
        return null;
    }
}
