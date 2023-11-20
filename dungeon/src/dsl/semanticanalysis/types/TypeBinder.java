package dsl.semanticanalysis.types;

import dsl.parser.ast.*;
import dsl.runtime.IEnvironment;
import dsl.semanticanalysis.ScopedSymbol;
import dsl.semanticanalysis.Symbol;
import dsl.semanticanalysis.SymbolTable;

public class TypeBinder implements AstVisitor<Object> {

    private StringBuilder errorStringBuilder;
    private IEnvironment environment;

    private SymbolTable symbolTable() {
        return this.environment.getSymbolTable();
    }

    /**
     * Create new types for all game object definitions
     *
     * <p>//* @param symbolTable the symbol table in which to store the types
     *
     * @param rootNode the root node of the program to scan for types
     * @param errorStringBuilder a string builder to which errors will be appended
     */
    public void bindTypes(
            IEnvironment environment, Node rootNode, StringBuilder errorStringBuilder) {
        this.environment = environment;
        this.errorStringBuilder = errorStringBuilder;
        visitChildren(rootNode);
    }

    private Symbol resolveGlobal(String name) {
        return this.symbolTable().getGlobalScope().resolve(name);
    }

    @Override
    public Object visit(PrototypeDefinitionNode node) {
        // create new type with name of definition node
        var newTypeName = node.getIdName();
        if (resolveGlobal(newTypeName) != Symbol.NULL) {
            // TODO: reference file and location of definition
            this.errorStringBuilder.append(
                    "Symbol with name '" + newTypeName + "' already defined");
            // TODO: return explicit null-Type?
            return null;
        }
        var newType = new AggregateType(newTypeName, this.symbolTable().getGlobalScope());
        symbolTable().addSymbolNodeRelation(newType, node, true);

        // visit all component definitions and get type and create new symbol in gameObject type
        for (var componentDef : node.getComponentDefinitionNodes()) {
            assert componentDef.type == Node.Type.AggregateValueDefinition;
            var compDefNode = (AggregateValueDefinitionNode) componentDef;

            var componentType = componentDef.accept(this);
            if (componentType != null) {
                String componentName = compDefNode.getIdName();
                var memberSymbol = new Symbol(componentName, newType, (IType) componentType);
                newType.bind(memberSymbol);
                symbolTable().addSymbolNodeRelation(memberSymbol, compDefNode, true);
            }
        }

        this.environment.loadTypes(newType);
        return newType;
    }

    @Override
    public Object visit(ItemPrototypeDefinitionNode node) {
        // create new type with name of definition node and load it in environment
        var newTypeName = node.getIdName();
        if (resolveGlobal(newTypeName) != Symbol.NULL) {
            // TODO: reference file and location of definition
            this.errorStringBuilder.append(
                    "Symbol with name '" + newTypeName + "' already defined");
            // TODO: return explicit null-Type?
            return null;
        }

        var itemType = new AggregateType(newTypeName, this.symbolTable().getGlobalScope());
        symbolTable().addSymbolNodeRelation(itemType, node, true);

        Symbol questItemTypeSymbol = this.environment.resolveInGlobalScope("quest_item");
        if (Symbol.NULL == questItemTypeSymbol) {
            throw new RuntimeException("'quest_item' cannot be resolved in global scope!");
        }

        if (!(questItemTypeSymbol instanceof IType questItemType)) {
            throw new RuntimeException("Symbol with name 'quest_item' is no type!");
        }

        if (!(questItemType instanceof ScopedSymbol scopedQuestItemType)) {
            throw new RuntimeException("Symbol with name 'quest_item' is no scoped symbol!");
        }

        for (Node propertyDefinition : node.getPropertyDefinitionNodes()) {
            var propertyDefinitionNode = (PropertyDefNode) propertyDefinition;
            String propertyName = ((PropertyDefNode) propertyDefinition).getIdName();
            Symbol propertySymbol = scopedQuestItemType.resolve(propertyName, false);
            if (propertySymbol == Symbol.NULL) {
                throw new RuntimeException(
                        "Cannot resolve property of name '"
                                + propertyName
                                + "' in type '"
                                + questItemType
                                + "'");
            }

            // the itemType will be its own independent datatype, so create a new symbol for the
            // property definition
            // in the new itemType
            var memberSymbol = new Symbol(propertyName, itemType, propertySymbol.getDataType());
            itemType.bind(memberSymbol);
            symbolTable().addSymbolNodeRelation(memberSymbol, propertyDefinitionNode, true);
        }

        this.environment.loadTypes(itemType);
        return itemType;
    }

    @Override
    public Object visit(AggregateValueDefinitionNode node) {
        // resolve components name in global scope
        var componentName = node.getIdName();
        var typeSymbol = resolveGlobal(componentName);
        if (typeSymbol.equals(Symbol.NULL)) {
            this.errorStringBuilder.append(
                    "Could not resolve component name '" + componentName + "'");
            // TODO: return explicit null-Type?
            return null;
        }
        if (!(typeSymbol instanceof IType)) {
            this.errorStringBuilder.append("Symbol '" + componentName + "' is no type!");
            // TODO: return explicit null-Type?
            return null;
        }
        return typeSymbol;
    }

    // region ASTVisitor implementation for nodes unrelated to type binding
    @Override
    public Object visit(Node node) {
        return null;
    }

    @Override
    public Object visit(IdNode node) {
        return null;
    }

    @Override
    public Object visit(DecNumNode node) {
        return null;
    }

    @Override
    public Object visit(NumNode node) {
        return null;
    }

    @Override
    public Object visit(StringNode node) {
        return null;
    }

    @Override
    public Object visit(BinaryNode node) {
        return null;
    }

    @Override
    public Object visit(DotDefNode node) {
        return null;
    }

    @Override
    public Object visit(EdgeRhsNode node) {
        return null;
    }

    @Override
    public Object visit(DotEdgeStmtNode node) {
        return null;
    }

    @Override
    public Object visit(EdgeOpNode node) {
        return null;
    }

    @Override
    public Object visit(PropertyDefNode node) {
        return null;
    }

    @Override
    public Object visit(ObjectDefNode node) {
        return null;
    }

    @Override
    public Object visit(FuncCallNode node) {
        return null;
    }

    @Override
    public Object visit(FuncDefNode node) {
        return null;
    }

    @Override
    public Object visit(ParamDefNode node) {
        return null;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ConditionalStmtNodeIf node) {
        return null;
    }

    @Override
    public Object visit(ConditionalStmtNodeIfElse node) {
        return null;
    }

    @Override
    public Object visit(StmtBlockNode node) {
        return null;
    }

    @Override
    public Object visit(BoolNode node) {
        return null;
    }

    @Override
    public Object visit(MemberAccessNode node) {
        return null;
    }

    @Override
    public Object visit(LogicOrNode node) {
        return null;
    }

    @Override
    public Object visit(LogicAndNode node) {
        return null;
    }

    @Override
    public Object visit(EqualityNode node) {
        return null;
    }

    @Override
    public Object visit(ComparisonNode node) {
        return null;
    }

    @Override
    public Object visit(TermNode node) {
        return null;
    }

    @Override
    public Object visit(FactorNode node) {
        return null;
    }

    @Override
    public Object visit(UnaryNode node) {
        return null;
    }

    @Override
    public Object visit(AssignmentNode node) {
        return null;
    }

    @Override
    public Object visit(ListDefinitionNode node) {
        return null;
    }

    @Override
    public Object visit(SetDefinitionNode node) {
        return null;
    }

    // endregion
}
