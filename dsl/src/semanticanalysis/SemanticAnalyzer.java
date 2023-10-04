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
        setup(environment, false);
    }

    /**
     * Setup environment for semantic analysis (setup builtin types and native functions); use an
     * externally provided symbol table, which will be used and extended during semantic analysis
     *
     * @param environment environment to use for setup of built in types and native functions
     * @param force force the initialization of this {@link SemanticAnalyzer} with the given {@link
     *     IEvironment} (ignore and overwrite previous initializations).
     */
    public void setup(IEvironment environment, boolean force) {
        if (!force && setup) {
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

                FunctionDefinitionBinder fdb = new FunctionDefinitionBinder();
                fdb.bindFunctionDefinitions(symbolTable, node);

                visitChildren(node);

                break;
            case PropertyDefinitionList:
                visitChildren(node);
            case ParamList:
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
            symbolTable.addSymbolNodeRelation(symbol, node, false);
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
    public Void visit(PrototypeDefinitionNode node) {
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
            this.symbolTable.addSymbolNodeRelation(propertySymbol, node, true);
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

    @Override
    public Void visit(ParamDefNode node) {
        // is handled in FunctionDefinitionBinder
        return null;
    }

    @Override
    public Void visit(FuncCallNode node) {
        Node parentNode = node.getParent();
        if (parentNode.type.equals(Node.Type.MemberAccess)) {
            // symbol will be resolved in the visit-implementation of MemberAccessNode, as it
            // requires
            // resolving in the datatype of the preceding member-access expression
        } else {
            Symbol funcSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
            if (funcSymbol == Symbol.NULL) {
                String funcName = node.getIdName();
                funcSymbol = currentScope().resolve(funcName, true);
                if (funcSymbol.equals(Symbol.NULL)) {
                    throw new RuntimeException(
                            "Function with name " + funcName + " could not be resolved!");
                }

                if (!(funcSymbol instanceof ICallable)) {
                    throw new RuntimeException(
                            "Symbol with name " + funcName + " is not callable!");
                }

                this.symbolTable.addSymbolNodeRelation(funcSymbol, node, false);
            }
        }

        for (var parameter : node.getParameters()) {
            parameter.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FuncDefNode node) {
        var funcName = node.getIdName();
        Symbol resolved = globalScope().resolve(funcName);
        if (resolved == Symbol.NULL) {
            errorStringBuilder.append(
                    "Could not resolve Identifier with name " + funcName + " in global scope!");
        } else {
            FunctionSymbol funcSymbol = (FunctionSymbol) resolved;
            scopeStack.push(funcSymbol);

            // visit statements
            node.getStmtBlock().accept(this);

            // create symbol table entry
            symbolTable.addSymbolNodeRelation(funcSymbol, node, false);

            scopeStack.pop();
        }
        // }
        return null;
    }

    @Override
    public Void visit(ReturnStmtNode node) {
        node.getInnerStmtNode().accept(this);
        return null;
    }

    @Override
    public Void visit(StmtBlockNode node) {
        var blockScope = new Scope(scopeStack.peek());
        scopeStack.push(blockScope);
        for (var stmt : node.getStmts()) {
            stmt.accept(this);
        }
        scopeStack.pop();
        return null;
    }

    @Override
    public Void visit(ConditionalStmtNodeIf node) {
        node.getCondition().accept(this);

        // if the statement is not a block (i.e. there is only one statement in the if-statements
        // body),
        // we need to create a new scope here (because it won't be created in a block-statement)
        if (!node.getIfStmt().type.equals(Node.Type.Block)) {
            var scope = new Scope(scopeStack.peek());
            scopeStack.push(scope);
            node.getIfStmt().accept(this);
            scopeStack.pop();
        } else {
            node.getIfStmt().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ConditionalStmtNodeIfElse node) {
        node.getCondition().accept(this);

        // if the statements are not blocks (i.e. there is only one statement in the if-statements
        // body),
        // we need to create new scopes here (because it won't be created in block-statements)
        if (!node.getIfStmt().type.equals(Node.Type.Block)) {
            var ifScope = new Scope(scopeStack.peek());
            scopeStack.push(ifScope);
            node.getIfStmt().accept(this);
            scopeStack.pop();
        } else {
            node.getIfStmt().accept(this);
        }

        if (!node.getElseStmt().type.equals(Node.Type.Block)) {
            var elseScope = new Scope(scopeStack.peek());
            scopeStack.push(elseScope);
            node.getElseStmt().accept(this);
            scopeStack.pop();
        } else {
            node.getElseStmt().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(MemberAccessNode node) {
        Node currentNode = node;
        Node lhs = Node.NONE;
        Node rhs = Node.NONE;
        IType lhsDataType = BuiltInType.noType;
        IScope scopeToUse = this.currentScope();

        while (currentNode.type.equals(Node.Type.MemberAccess)) {
            lhs = ((MemberAccessNode) currentNode).getLhs();
            rhs = ((MemberAccessNode) currentNode).getRhs();

            // resolve name of lhs in scope
            // lhsDataType = BuiltInType.noType;
            if (lhs.type.equals(Node.Type.Identifier)) {
                String nameToResolve = ((IdNode) lhs).getName();
                Symbol symbol = scopeToUse.resolve(nameToResolve);
                lhsDataType = symbol.getDataType();

                symbolTable.addSymbolNodeRelation(symbol, lhs, false);
            } else if (lhs.type.equals(Node.Type.FuncCall)) {
                // visit function call itself (resolve parameters etc.)
                lhs.accept(this);

                // resolve function definition
                String functionName = ((FuncCallNode) lhs).getIdName();
                Symbol resolvedFunction = scopeToUse.resolve(functionName);
                ICallable callable = (ICallable) resolvedFunction;
                FunctionType functionType = callable.getFunctionType();
                lhsDataType = functionType.getReturnType();

                symbolTable.addSymbolNodeRelation(resolvedFunction, lhs, false);
            }

            currentNode = rhs;

            if (!(lhsDataType instanceof ScopedSymbol lhsTypeScopedSymbol)) {
                throw new RuntimeException(
                        "Datatype "
                                + lhsDataType.getName()
                                + " of lhs in member access is no scoped symbol!");
            }
            scopeToUse = lhsTypeScopedSymbol;
        }

        // if we arrive here, we have got two options:
        // 1. we resolve an IdNode at the rhs of the MemberAccessNode
        // 2. we resolve an FuncCallNode at the rhs of the MemberAccessNode
        if (rhs.type.equals(Node.Type.Identifier)) {
            // push lhsDataType on stack
            scopeStack.push(scopeToUse);
            rhs.accept(this);
            scopeStack.pop();
        } else if (rhs.type.equals(Node.Type.FuncCall)) {
            // resolve function name in scope to use
            String funcName = ((FuncCallNode) rhs).getIdName();
            Symbol funcSymbol = scopeToUse.resolve(funcName, true);
            if (funcSymbol.equals(Symbol.NULL)) {
                throw new RuntimeException(
                        "Function with name " + funcName + " could not be resolved!");
            }

            if (!(funcSymbol instanceof ICallable)) {
                throw new RuntimeException("Symbol with name " + funcName + " is not callable!");
            }
            this.symbolTable.addSymbolNodeRelation(funcSymbol, rhs, false);

            rhs.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(LogicOrNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(LogicAndNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(EqualityNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(ComparisonNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(TermNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(FactorNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(UnaryNode node) {
        // TODO: implement
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(AssignmentNode node) {
        // TODO: typechcking

        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(ListTypeIdentifierNode node) {
        String typeName = node.getName();
        Symbol resolvedType = this.environment.resolveInGlobalScope(typeName);

        // construct a new ListType for the node, if it was not previously created
        if (resolvedType == Symbol.NULL) {
            // create inner type node
            IdNode innerTypeNode = node.getInnerTypeNode();
            if (innerTypeNode.type != Node.Type.Identifier) {
                innerTypeNode.accept(this);
            }
            var innerType = (IType) this.environment.resolveInGlobalScope(innerTypeNode.getName());
            ListType listType = new ListType(innerType, this.globalScope());
            this.globalScope().bind(listType);
        }
        return null;
    }

    @Override
    public Void visit(SetTypeIdentifierNode node) {
        String typeName = node.getName();
        Symbol resolvedType = this.environment.resolveInGlobalScope(typeName);

        // construct a new ListType for the node, if it was not previously created
        if (resolvedType == Symbol.NULL) {
            // create inner type node
            IdNode innerTypeNode = node.getInnerTypeNode();
            if (innerTypeNode.type != Node.Type.Identifier) {
                innerTypeNode.accept(this);
            }
            var innerType = (IType) this.environment.resolveInGlobalScope(innerTypeNode.getName());
            SetType setType = new SetType(innerType, this.globalScope());
            this.globalScope().bind(setType);
        }
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

    @Override
    public Void visit(VarDeclNode node) {
        String name = ((IdNode) node.getIdentifier()).getName();

        // check if a variable is already defined in the current scope
        Symbol resolvedName = this.currentScope().resolve(name, false);
        if (!resolvedName.equals(Symbol.NULL)) {
            throw new RuntimeException("Redefinition of variable '" + name + "'");
        }

        // create new symbol for the variable
        // get the type of the variable
        if (node.getDeclType().equals(VarDeclNode.DeclType.assignmentDecl)) {
            throw new RuntimeException("Inference of variable type currently not supported!");
        }

        // resolve the type name
        IdNode typeDeclNode = (IdNode) node.getRhs();
        String typeName = typeDeclNode.getName();
        if (!typeDeclNode.type.equals(Node.Type.Identifier)) {
            // list or set type -> create type
            typeDeclNode.accept(this);
        }

        Symbol typeSymbol = this.globalScope().resolve(typeName);
        if (!(typeSymbol instanceof IType variableType)) {
            throw new RuntimeException("Type of name '" + typeName + "' cannot be resolved!");
        }

        // create variable symbol
        Symbol variableSymbol = new Symbol(name, this.currentScope(), variableType);
        this.currentScope().bind(variableSymbol);
        this.symbolTable.addSymbolNodeRelation(variableSymbol, node, true);

        return null;
    }

    // region ASTVisitor implementation for nodes unrelated to semantic analysis
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
        // TODO: should check, that all identifiers actually refer to
        //  task definitions -> should be implemented with type checking
        for (Node stmt : node.getStmtNodes()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DotNodeStmtNode node) {
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(DotIdList node) {
        // visit all stored IdNodes
        visitChildren(node);
        return null;
    }

    @Override
    public Void visit(EdgeRhsNode node) {
        return null;
    }

    @Override
    public Void visit(DotEdgeStmtNode node) {
        for (Node id : node.getIdLists()) {
            id.accept(this);
        }
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
