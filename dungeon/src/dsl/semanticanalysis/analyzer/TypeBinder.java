package dsl.semanticanalysis.analyzer;

import dsl.parser.ast.*;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;
import java.util.Stack;

public class TypeBinder implements AstVisitor<Object> {

  private StringBuilder errorStringBuilder;
  // private IEnvironment environment;
  private IScope scope;
  private SymbolTable symbolTable;
  private TypeFactory typeFactory;
  private ScopedSymbol questItemTypeSymbol;

  private Stack<ScopedSymbol> createdTypeStack = new Stack<>();

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
    this.typeFactory = environment.typeFactory();
    this.scope = scope;
    this.questItemTypeSymbol = null;
    this.errorStringBuilder = errorStringBuilder;
    visitChildren(rootNode);
  }

  private Symbol resolveGlobal(String name) {
    return this.symbolTable().globalScope().resolve(name);
  }

  @Override
  public Object visit(PrototypeDefinitionNode node) {
    if (node.hasErrorRecord() || node.hasErrorChild()) {
      return null;
    }

    // create new type with name of definition node
    var newTypeName = node.getIdName();
    if (this.scope.resolve(newTypeName) != Symbol.NULL) {
      // TODO: reference file and location of definition
      this.errorStringBuilder.append("Symbol with name '" + newTypeName + "' already defined");
      // TODO: return explicit null-Type?
      return null;
    }

    var newType = this.typeFactory.aggregateType(newTypeName, this.scope);
    symbolTable().addSymbolNodeRelation(newType, node, true);

    this.createdTypeStack.push(newType);
    // visit all component definitions and get type and create new symbol in gameObject type
    for (var componentDef : node.getComponentDefinitionNodes()) {
      componentDef.accept(this);
    }
    this.createdTypeStack.pop();

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

    var itemType = this.typeFactory.aggregateType(newTypeName, this.scope);
    symbolTable().addSymbolNodeRelation(itemType, node, true);
    this.createdTypeStack.push(itemType);

    for (var propertyDefinition : node.getPropertyDefinitionNodes()) {
      propertyDefinition.accept(this);
    }

    this.createdTypeStack.pop();
    this.scope.bind(itemType);
    return itemType;
  }

  @Override
  public Object visit(PropertyDefNode node) {
    if (node.hasErrorRecord() || node.hasErrorChild()) {
      return null;
    }

    var listNode = node.getParent();
    if (listNode != null && listNode.type != Node.Type.PropertyDefinitionList) {
      return null;
    }
    var parentOfList = listNode.getParent();

    // we expect to enter propertyDefNodes in the context of the TypeBinder only in
    // ItemTypeDefinition nodes,
    // so we need to check, if the node is from such a node
    if (parentOfList != null && parentOfList.type != Node.Type.ItemPrototypeDefinition) {
      return null;
    }

    ScopedSymbol questItemTypeSymbol = this.getQuestItemTypeSymbol();
    ScopedSymbol currentType = (ScopedSymbol) this.createdTypeStack.peek();
    String propertyName = node.getIdName();
    Symbol propertySymbol = questItemTypeSymbol.resolve(propertyName, false);
    if (propertySymbol == Symbol.NULL) {
      throw new RuntimeException(
          "Cannot resolve property of name '"
              + propertyName
              + "' in type '"
              + questItemTypeSymbol
              + "'");
    }

    // the itemType will be its own independent datatype, so create a new symbol for the
    // property definition
    // in the new itemType
    var memberSymbol = new Symbol(propertyName, currentType, propertySymbol.getDataType());
    currentType.bind(memberSymbol);
    symbolTable().addSymbolNodeRelation(memberSymbol, node, true);
    return null;
  }

  private ScopedSymbol getQuestItemTypeSymbol() {
    if (this.questItemTypeSymbol == null) {
      Symbol symbol = this.scope.resolve("quest_item");
      if (Symbol.NULL == symbol) {
        throw new RuntimeException("'quest_item' cannot be resolved in global scope!");
      }

      if (!(symbol instanceof IType questItemType)) {
        throw new RuntimeException("Symbol with name 'quest_item' is no type!");
      }

      if (!(questItemType instanceof ScopedSymbol scopedQuestItemType)) {
        throw new RuntimeException("Symbol with name 'quest_item' is no scoped symbol!");
      }
      this.questItemTypeSymbol = scopedQuestItemType;
    }
    return this.questItemTypeSymbol;
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

    var currentlyCreatedType = this.createdTypeStack.peek();
    var memberSymbol = new Symbol(componentName, currentlyCreatedType, (IType) typeSymbol);
    currentlyCreatedType.bind(memberSymbol);
    symbolTable().addSymbolNodeRelation(memberSymbol, node, true);
    return memberSymbol;
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
        ListType listType = this.typeFactory.listType(innerType, globalScope);
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
        SetType setType = typeFactory.setType(innerType, globalScope);
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
      MapType setType = this.typeFactory.mapType(keyType, elementType, globalScope);
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

  @Override
  public void visitChildren(Node node) {
    if (!node.hasErrorChild() && !node.hasErrorRecord()) {
      for (var child : node.getChildren()) {
        if (child.hasErrorRecord() || child.hasErrorChild()) {
          continue;
        }
        child.accept(this);
      }
    }
  }
}
