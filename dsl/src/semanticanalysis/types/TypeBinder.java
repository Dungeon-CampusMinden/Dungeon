package semanticanalysis.types;

import parser.ast.*;

import runtime.IEvironment;

import semanticanalysis.Symbol;
import semanticanalysis.SymbolTable;

public class TypeBinder implements AstVisitor<Object> {

    private StringBuilder errorStringBuilder;
    private IEvironment environment;

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
            IEvironment environment, Node rootNode, StringBuilder errorStringBuilder) {
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
        symbolTable().addSymbolNodeRelation(newType, node);

        // visit all component definitions and get type and create new symbol in gameObject type
        for (var componentDef : node.getComponentDefinitionNodes()) {
            assert componentDef.type == Node.Type.AggregateValueDefinition;
            var compDefNode = (AggregateValueDefinitionNode) componentDef;

            var componentType = componentDef.accept(this);
            if (componentType != null) {
                String componentName = compDefNode.getIdName();
                var memberSymbol = new Symbol(componentName, newType, (IType) componentType);
                newType.bind(memberSymbol);
                symbolTable().addSymbolNodeRelation(memberSymbol, compDefNode);
            }
        }

        this.environment.loadTypes(newType);
        return newType;
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
    public Object visit(EdgeStmtNode node) {
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
