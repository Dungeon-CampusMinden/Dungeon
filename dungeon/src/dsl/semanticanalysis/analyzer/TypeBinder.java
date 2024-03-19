package dsl.semanticanalysis.analyzer;

import dsl.parser.ast.*;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;

public class TypeBinder implements AstVisitor<Object> {

  private StringBuilder errorStringBuilder;
  // private IEnvironment environment;
  private IScope scope;
  private SymbolTable symbolTable;

  private SymbolTable symbolTable() {
    return this.symbolTable;
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
      IEnvironment environment, IScope scope, Node rootNode, StringBuilder errorStringBuilder) {
    // this.environment = environment;
    this.symbolTable = environment.getSymbolTable();
    this.scope = scope;
    this.errorStringBuilder = errorStringBuilder;
    visitChildren(rootNode);
  }

  private Symbol resolveGlobal(String name) {
    return this.symbolTable().globalScope().resolve(name);
  }

  @Override
  public Object visit(PrototypeDefinitionNode node) {
    // create new type with name of definition node
    var newTypeName = node.getIdName();
    if (this.scope.resolve(newTypeName) != Symbol.NULL) {
      // TODO: reference file and location of definition
      this.errorStringBuilder.append("Symbol with name '" + newTypeName + "' already defined");
      // TODO: return explicit null-Type?
      return null;
    }
    var newType = new AggregateType(newTypeName, this.scope);
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

    this.scope.bind(newType);
    return newType;
  }

  @Override
  public Object visit(ItemPrototypeDefinitionNode node) {
    // create new type with name of definition node and load it in environment
    var newTypeName = node.getIdName();
    if (resolveGlobal(newTypeName) != Symbol.NULL) {
      // TODO: reference file and location of definition
      this.errorStringBuilder.append("Symbol with name '" + newTypeName + "' already defined");
      // TODO: return explicit null-Type?
      return null;
    }

    var itemType = new AggregateType(newTypeName, this.scope);
    symbolTable().addSymbolNodeRelation(itemType, node, true);

    Symbol questItemTypeSymbol = this.scope.resolve("quest_item");
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

    // this.environment.loadTypes(itemType);
    this.scope.bind(itemType);
    return itemType;
  }

  @Override
  public Object visit(AggregateValueDefinitionNode node) {
    // resolve components name in global scope
    var componentName = node.getIdName();
    var typeSymbol = resolveGlobal(componentName);
    if (typeSymbol.equals(Symbol.NULL)) {
      this.errorStringBuilder.append("Could not resolve component name '" + componentName + "'");
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

  @Override
  public Void visit(ListTypeIdentifierNode node) {
    String typeName = node.getName();
    IScope scopeToResolveTypeIn = this.scope;
    Symbol resolvedType = scopeToResolveTypeIn.resolve(typeName);

    // construct a new ListType for the node, if it was not previously created
    if (resolvedType == Symbol.NULL) {
      // create inner type node
      IdNode innerTypeNode = node.getInnerTypeNode();
      if (innerTypeNode.type != Node.Type.Identifier) {
        innerTypeNode.accept(this);
      }
      var innerType = (IType) scopeToResolveTypeIn.resolve(innerTypeNode.getName());
      Symbol listTypeSymbol = scopeToResolveTypeIn.resolve(ListType.getListTypeName(innerType));
      if (listTypeSymbol.equals(Symbol.NULL)) {
        IScope globalScope = this.symbolTable.globalScope();
        ListType listType = new ListType(innerType, globalScope);
        globalScope.bind(listType);
      }
    }
    return null;
  }

  @Override
  public Void visit(SetTypeIdentifierNode node) {
    String typeName = node.getName();
    IScope scopeToResolveTypeIn = this.scope;
    Symbol resolvedType = scopeToResolveTypeIn.resolve(typeName);

    // construct a new ListType for the node, if it was not previously created
    if (resolvedType == Symbol.NULL) {
      // create inner type node
      IdNode innerTypeNode = node.getInnerTypeNode();
      if (innerTypeNode.type != Node.Type.Identifier) {
        innerTypeNode.accept(this);
      }
      var innerType = (IType) scopeToResolveTypeIn.resolve(innerTypeNode.getName());
      Symbol setTypeSymbol = scopeToResolveTypeIn.resolve(SetType.getSetTypeName(innerType));
      if (setTypeSymbol.equals(Symbol.NULL)) {
        IScope globalScope = symbolTable.globalScope();
        SetType setType = new SetType(innerType, globalScope);
        globalScope.bind(setType);
      }
    }
    return null;
  }

  @Override
  public Void visit(MapTypeIdentifierNode node) {
    String typeName = node.getName();
    IScope scopeToResolveTypeIn = this.scope;
    Symbol resolvedType = scopeToResolveTypeIn.resolve(typeName);

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

      var keyType = scopeToResolveTypeIn.resolveType(keyTypeNode.getName());
      var elementType = scopeToResolveTypeIn.resolveType(elementTypeNode.getName());
      IScope globalScope = this.symbolTable.globalScope();
      MapType setType = new MapType(keyType, elementType, globalScope);
      globalScope.bind(setType);
    }
    return null;
  }

  @Override
  public Object visit(Node node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(ForLoopStmtNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(BinaryNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(FuncDefNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(ParamDefNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(ReturnStmtNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(ConditionalStmtNodeIf node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(ConditionalStmtNodeIfElse node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(StmtBlockNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(AssignmentNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(LoopStmtNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(WhileLoopStmtNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(CountingLoopStmtNode node) {
    visitChildren(node);
    return null;
  }

  @Override
  public Object visit(VarDeclNode node) {
    visitChildren(node);
    return null;
  }

  // region ASTVisitor implementation for nodes unrelated to type binding

  @Override
  public Object visit(ImportNode node) {
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
  public Object visit(ListDefinitionNode node) {
    return null;
  }

  @Override
  public Object visit(SetDefinitionNode node) {
    return null;
  }

  // endregion
}
