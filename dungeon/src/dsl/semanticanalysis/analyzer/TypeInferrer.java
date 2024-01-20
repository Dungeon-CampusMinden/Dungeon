package dsl.semanticanalysis.analyzer;

import dsl.parser.ast.*;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

public class TypeInferrer implements AstVisitor<IType> {
  // Deque<IScope> scopeStack;
  SymbolTable symbolTable;

  public TypeInferrer(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
    // this.scopeStack = new ArrayDeque<>();
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
    FunctionSymbol funcSymbol =
        (FunctionSymbol) this.symbolTable.getSymbolsForAstNode(node).getFirst();
    return funcSymbol.getFunctionType().getReturnType();
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
    // TODO
    throw new UnsupportedOperationException();
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
    Symbol symbol = this.symbolTable.getSymbolsForAstNode(node).getFirst();
    return symbol.getDataType();
  }

  @Override
  public IType visit(SetDefinitionNode node) {
    Symbol symbol = this.symbolTable.getSymbolsForAstNode(node).getFirst();
    return symbol.getDataType();
  }

  @Override
  public IType visit(VarDeclNode node) {
    // TODO: is this required?
    throw new UnsupportedOperationException();
  }
}
