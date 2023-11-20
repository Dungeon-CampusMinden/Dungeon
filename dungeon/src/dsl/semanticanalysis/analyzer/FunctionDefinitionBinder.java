package dsl.semanticanalysis.analyzer;

import dsl.parser.ast.*;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.types.*;
import dsl.semanticanalysis.types.IType;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Implements a semantic analysis pass, in which all function definitions are bound as {@link
 * FunctionSymbol} in the global scope of a {@link SymbolTable}
 */
public class FunctionDefinitionBinder implements AstVisitor<Void> {
    private SymbolTable symbolTable;
    Stack<IScope> scopeStack = new Stack<>();

    /**
     * Visit all function definitions in the passed programRootNode and bind them as {@link
     * FunctionSymbol} in the global {@link IScope} of the passed {@link SymbolTable}
     *
     * @param symbolTable the symboltable to bind the function definitions in
     * @param programRootNode the root {@link Node} of the program containing function definitions
     */
    public void bindFunctionDefinitions(SymbolTable symbolTable, Node programRootNode) {
        this.symbolTable = symbolTable;
        this.scopeStack = new Stack<>();

        programRootNode.accept(this);
    }

    @Override
    public Void visit(Node node) {
        switch (node.type) {
            case Program:
                visitChildren(node);
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Void visit(IdNode node) {
        return null;
    }

    @Override
    public Void visit(DotDefNode node) {
        return null;
    }

    @Override
    public Void visit(ObjectDefNode node) {
        return null;
    }

    @Override
    public Void visit(PrototypeDefinitionNode node) {
        return null;
    }

    @Override
    public Void visit(ItemPrototypeDefinitionNode node) {
        return null;
    }

    @Override
    public Void visit(FuncDefNode node) {
        // check, if symbol with the name was already bound
        var funcName = node.getIdName();
        var globalScope = symbolTable.globalScope();
        var resolved = globalScope.resolve(funcName);
        if (resolved != Symbol.NULL) {
            throw new RuntimeException(
                    "Identifier with name " + funcName + " is already bound in global scope!");
        } else {
            // resolve return value (if one was defined)
            IType returnType = BuiltInType.noType;
            Node returnTypeIdNode = node.getRetTypeId();
            if (returnTypeIdNode != Node.NONE) {
                if (returnTypeIdNode.type != Node.Type.Identifier) {
                    // the type is either a list type or set type, which may
                    // require type creation
                    returnTypeIdNode.accept(this);
                }

                String returnTypeName = node.getRetTypeName();
                returnType = globalScope.resolveType(returnTypeName);
                if (returnType == null) {
                    throw new RuntimeException(
                            "Could not resolve return type "
                                    + returnTypeName
                                    + " of function "
                                    + funcName);
                }
                symbolTable.addSymbolNodeRelation((Symbol) returnType, returnTypeIdNode, false);
            }

            // get types of parameters
            ArrayList<IType> parameterTypes = new ArrayList<>(node.getParameters().size());
            for (Node paramDefNode : node.getParameters()) {
                // if the parameters type is a list or set type, the datatype must be created
                IdNode paramIdNode = (IdNode) ((ParamDefNode) paramDefNode).getTypeIdNode();
                paramIdNode.accept(this);

                var paramTypeName = ((ParamDefNode) paramDefNode).getTypeName();
                IType paramType = globalScope.resolveType(paramTypeName);
                parameterTypes.add(paramType);

                symbolTable.addSymbolNodeRelation((Symbol) paramType, paramIdNode, false);
            }

            // create function signature type (as needed)
            String functionTypeName = FunctionType.calculateTypeName(returnType, parameterTypes);
            Symbol functionTypeSymbol = globalScope.resolve(functionTypeName);
            FunctionType functionType;

            if (functionTypeSymbol != Symbol.NULL) {
                functionType = (FunctionType) functionTypeSymbol;
            } else {
                functionType = new FunctionType(returnType, parameterTypes);
                globalScope.bind(functionType);
            }

            // create new function symbol
            var funcSymbol = new FunctionSymbol(funcName, globalScope, node, functionType);
            globalScope.bind(funcSymbol);
            scopeStack.push(funcSymbol);

            // bind parameters
            for (var paramDefNode : node.getParameters()) {
                paramDefNode.accept(this);
            }

            // create symbol table entry
            symbolTable.addSymbolNodeRelation(funcSymbol, node, true);

            scopeStack.pop();
        }
        return null;
    }

    @Override
    public Void visit(ParamDefNode node) {
        // current scope should be a function definition
        IScope currentScope = scopeStack.peek();
        String parameterName = node.getIdName();
        Node parameterIdNode = node.getIdNode();
        var resolvedParameter = currentScope.resolve(parameterName, false);
        if (resolvedParameter != Symbol.NULL) {
            throw new RuntimeException(
                    "Parameter with name " + node.getIdName() + " was already defined");
        } else {
            // resolve parameters datatype
            IType parameterType = this.symbolTable.globalScope().resolveType(node.getTypeName());

            Symbol parameterSymbol = new Symbol(parameterName, currentScope, parameterType);
            currentScope.bind(parameterSymbol);

            symbolTable.addSymbolNodeRelation(parameterSymbol, parameterIdNode, true);
        }
        return null;
    }

    // TODO: this should probably be done in TypeBinder
    //  (see: https://github.com/Programmiermethoden/Dungeon/issues/931)
    @Override
    public Void visit(ListTypeIdentifierNode node) {
        IScope globalScope = this.symbolTable.globalScope();
        String typeName = node.getName();
        Symbol resolvedType = globalScope.resolve(typeName);

        // construct a new ListType for the node, if it was not previously created
        if (resolvedType == Symbol.NULL) {
            // create inner type node
            IdNode innerTypeNode = node.getInnerTypeNode();
            if (innerTypeNode.type != Node.Type.Identifier) {
                innerTypeNode.accept(this);
            }
            var innerType = globalScope.resolveType(innerTypeNode.getName());
            Symbol listTypeSymbol = globalScope.resolve(ListType.getListTypeName(innerType));
            if (listTypeSymbol.equals(Symbol.NULL)) {
                ListType listType = new ListType(innerType, globalScope);
                globalScope.bind(listType);
            }
        }
        return null;
    }

    // TODO: this should probably be done in TypeBinder
    //  (see: https://github.com/Programmiermethoden/Dungeon/issues/931)
    @Override
    public Void visit(SetTypeIdentifierNode node) {
        IScope globalScope = this.symbolTable.globalScope();
        String typeName = node.getName();
        Symbol resolvedType = globalScope.resolve(typeName);

        // construct a new ListType for the node, if it was not previously created
        if (resolvedType == Symbol.NULL) {
            // create inner type node
            IdNode innerTypeNode = node.getInnerTypeNode();
            if (innerTypeNode.type != Node.Type.Identifier) {
                innerTypeNode.accept(this);
            }
            var innerType = globalScope.resolveType(innerTypeNode.getName());
            Symbol setTypeSymbol = globalScope.resolve(SetType.getSetTypeName(innerType));
            if (setTypeSymbol.equals(Symbol.NULL)) {
                SetType setType = new SetType(innerType, globalScope);
                globalScope.bind(setType);
            }
        }
        return null;
    }

    @Override
    public Void visit(MapTypeIdentifierNode node) {
        IScope globalScope = this.symbolTable.globalScope();
        String typeName = node.getName();
        Symbol resolvedType = globalScope.resolve(typeName);

        // construct a new MapType for the node, if it was not previously created
        if (resolvedType == Symbol.NULL) {
            // create key type
            IdNode keyTypeNode = node.getKeyTypeNode();
            if (keyTypeNode.type != Node.Type.Identifier) {
                keyTypeNode.accept(this);
            }

            // create element type
            IdNode elementTypeNode = node.getElementTypeNode();
            if (elementTypeNode.type != Node.Type.Identifier) {
                elementTypeNode.accept(this);
            }

            var keyType = globalScope.resolveType(keyTypeNode.getName());
            var elementType = globalScope.resolveType(elementTypeNode.getName());
            MapType setType = new MapType(keyType, elementType, globalScope);
            globalScope.bind(setType);
        }
        return null;
    }
}
