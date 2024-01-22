package dsl.semanticanalysis.analyzer;

import dsl.parser.ast.*;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import dsl.semanticanalysis.typesystem.typebuilding.type.ListType;
import dsl.semanticanalysis.typesystem.typebuilding.type.SetType;

public class TypeInferrer implements AstVisitor<IType> {
  SymbolTable symbolTable;

  public TypeInferrer(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  IType inferType(Node node) {
    return node.accept(this);
  }

  @Override
  public IType visit(Node node) {
    // TODO: which NodeTypes need to be supported here?
    return AstVisitor.super.visit(node);
  }

  @Override
  public IType visit(IdNode node) {
    Symbol symbol = this.symbolTable.getSymbolsForAstNode(node).getFirst();
    return symbol.getDataType();
  }

  @Override
  public IType visit(DecNumNode node) {
    return BuiltInType.floatType;
  }

  @Override
  public IType visit(NumNode node) {
    return BuiltInType.intType;
  }

  @Override
  public IType visit(StringNode node) {
    return BuiltInType.stringType;
  }

  @Override
  public IType visit(FuncCallNode node) {
    // resolve func symbol
    ICallable callable = (ICallable) this.symbolTable.getSymbolsForAstNode(node).getFirst();
    return callable.getFunctionType().getReturnType();
  }

  @Override
  public IType visit(ReturnStmtNode node) {
    return node.getInnerStmtNode().accept(this);
  }

  @Override
  public IType visit(BoolNode node) {
    return BuiltInType.boolType;
  }

  @Override
  public IType visit(MemberAccessNode node) {
    Node rhsNode = node.getRhs();
    while (rhsNode.type.equals(Node.Type.MemberAccess)) {
      rhsNode = ((MemberAccessNode) rhsNode).getRhs();
    }

    Symbol rhsSymbol = this.symbolTable.getSymbolsForAstNode(rhsNode).getFirst();
    if (rhsSymbol.equals(Symbol.NULL)) {
      return BuiltInType.noType;
    }
    return rhsSymbol.getDataType();
  }

  @Override
  public IType visit(LogicOrNode node) {
    return BuiltInType.boolType;
  }

  @Override
  public IType visit(LogicAndNode node) {
    return BuiltInType.boolType;
  }

  @Override
  public IType visit(EqualityNode node) {
    return BuiltInType.boolType;
  }

  @Override
  public IType visit(ComparisonNode node) {
    return BuiltInType.boolType;
  }

  @Override
  public IType visit(TermNode node) {
    // get lhs and rhs type
    IType lhsType = node.getLhs().accept(this);
    IType rhsType = node.getRhs().accept(this);
    assert lhsType == rhsType;
    return lhsType;
  }

  @Override
  public IType visit(FactorNode node) {
    // get lhs and rhs type
    IType lhsType = node.getLhs().accept(this);
    IType rhsType = node.getRhs().accept(this);
    assert lhsType == rhsType;
    return lhsType;
  }

  @Override
  public IType visit(UnaryNode node) {
    return switch (node.getUnaryType()) {
      case not -> BuiltInType.boolType;
      case minus -> node.getInnerNode().accept(this);
    };
  }

  @Override
  public IType visit(AssignmentNode node) {
    return node.getRhs().accept(this);
  }

  @Override
  public IType visit(ListDefinitionNode node) {
    if (node.getEntries().isEmpty()) {
      throw new RuntimeException("Can't infer type of empty list definition!");
    }
    var firstEntry = node.getEntries().getFirst();
    IType innerType = firstEntry.accept(this);
    String listTypeName = ListType.getListTypeName(innerType);
    Symbol resolvedSymbol = this.symbolTable.globalScope().resolve(listTypeName);
    ListType listType;
    if (resolvedSymbol.equals(Symbol.NULL)) {
      IScope globalScope = symbolTable.globalScope();
      listType = new ListType(innerType, globalScope);
      globalScope.bind(listType);
    } else {
      listType = (ListType) resolvedSymbol;
    }
    return listType;
  }

  @Override
  public IType visit(SetDefinitionNode node) {
    if (node.getEntries().isEmpty()) {
      throw new RuntimeException("Can't infer type of empty set definition!");
    }
    var firstEntry = node.getEntries().getFirst();
    IType innerType = firstEntry.accept(this);
    String setTypeName = SetType.getSetTypeName(innerType);
    Symbol resolvedSymbol = this.symbolTable.globalScope().resolve(setTypeName);
    SetType setType;
    if (resolvedSymbol.equals(Symbol.NULL)) {
      IScope globalScope = symbolTable.globalScope();
      setType = new SetType(innerType, globalScope);
      globalScope.bind(setType);
    } else {
      setType = (SetType) resolvedSymbol;
    }
    return setType;
  }

  @Override
  public IType visit(VarDeclNode node) {
    // TODO: is this required?
    throw new UnsupportedOperationException();
  }
}
