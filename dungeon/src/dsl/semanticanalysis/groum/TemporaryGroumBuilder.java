package dsl.semanticanalysis.groum;

import dsl.IndexGenerator;
import dsl.parser.ast.*;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TemporaryGroumBuilder implements AstVisitor<Groum> {
  private SymbolTable symbolTable;
  private IEnvironment environment;
  private HashMap<Symbol, Long> instanceMap = new HashMap<>();

  public Groum walk(Node astNode, SymbolTable symbolTable, IEnvironment environment) {
    this.symbolTable = symbolTable;
    this.environment = environment;

    var groumNode = astNode.accept(this);

    this.symbolTable = null;
    this.environment = null;

    return groumNode;
  }

  private long createOrGetInstanceId(Symbol symbol) {
    if (!this.instanceMap.containsKey(symbol)) {
      this.instanceMap.put(symbol, IndexGenerator.getIdx());
    }
    return this.instanceMap.get(symbol);
  }

  @Override
  public Groum visit(Node node) {
    switch (node.type) {
      case Program:
        List<Groum> groums = new ArrayList<>();
        for (var child : node.getChildren()) {
          var childGroum = child.accept(this);
          groums.add(childGroum);
        }
        // TODO: merge all groums together (parallel for now?)
        Groum merged = new Groum();
        for (var groum : groums) {
          merged = merged.mergeParallel(groum);
        }
        return merged;
        //break;
      default:
        return Groum.NONE;
    }
  }

  @Override
  public Groum visit(FuncDefNode node) {
    // visit paramNodes
    List<Groum> paramGroums = new ArrayList<>();
    for (var paramNode : node.getParameters()) {
      var paramGroum = paramNode.accept(this);
      paramGroums.add(paramGroum);
    }

    // visit all stmt nodes
    List<Groum> stmtGroums = new ArrayList<>();
    for (var stmt : node.getStmts()) {
      var stmtGroum = stmt.accept(this);
      stmtGroums.add(stmtGroum);
    }

    Groum merged = new Groum();

    for (var paramGroum : paramGroums) {
      merged = merged.mergeParallel(paramGroum);
    }

    for (var stmtGroum : stmtGroums) {
      merged = merged.mergeSequential(stmtGroum);
    }

    return merged;
  }

  @Override
  public Groum visit(ParamDefNode node) {
    // resolve symbol
    var paramId = node.getIdNode();
    var paramSymbol = symbolTable.getSymbolsForAstNode(paramId).get(0);

    // TODO: create new ActionNode ?
    // TODO: does this create a new value instance? -> depends on type of
    //  parameter
    var action = new ParameterInstantiationAction(paramSymbol, createOrGetInstanceId(paramSymbol));
    return new Groum(action);
  }

  @Override
  public Groum visit(ReturnStmtNode node) {
    // TODO: visit inner node
    var innerGroum = node.getInnerStmtNode().accept(this);

    // maybe just add the reference in a return stmt as an annotation?
    ControlNode returnNode = new ControlNode(ControlNode.ControlType.returnStmt);
    var controlGroum = new Groum(returnNode);

    // TODO: ???
    return innerGroum.mergeSequential(controlGroum);
  }

  @Override
  public Groum visit(IdNode node) {
    // Id in expression.. i guess
    Symbol referencedSymbol = symbolTable.getSymbolsForAstNode(node).get(0);
    var action = new VariableReferenceAction(referencedSymbol, createOrGetInstanceId(referencedSymbol));
    return new Groum(action);
  }

  @Override
  public Groum visit(VarDeclNode node) {
    // TODO: the rhs should be created in a way, which allows for treating it as a single node
    //  which is used by the resulting primary <init> node of this here method
    Groum rhsGroum = Groum.NONE;
    if (node.getDeclType().equals(VarDeclNode.DeclType.assignmentDecl)) {
      // get rhs
      rhsGroum = node.getRhs().accept(this);
    }

    var id = node.getIdentifier();
    var symbol = symbolTable.getSymbolsForAstNode(id).get(0);

    // TODO: this does not take into account the data dependencies!!
    var instantiationAction = new InstantiationAction(symbol, createOrGetInstanceId(symbol));
    Groum initGroum = new Groum(instantiationAction);
    Groum mergedGroum;
    if (!rhsGroum.equals(Groum.NONE)) {
      // expression action
      ExpressionAction expr = new ExpressionAction(rhsGroum.nodes, IndexGenerator.getIdx());
      Groum exprGroum = new Groum(expr);

      var intermediaryGroum = rhsGroum.mergeSequential(exprGroum, GroumEdge.GroumEdgeType.dataDependency);
      //exprGroum = rhsGroum.mergeSequential(exprGroum, GroumEdge.GroumEdgeType.dataDependency);
      mergedGroum = intermediaryGroum.mergeSequential(initGroum);
    } else {
      mergedGroum = initGroum;
    }

    return mergedGroum;
  }

  @Override
  public Groum visit(DecNumNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(NumNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(StringNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(BinaryNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotDefNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(EdgeRhsNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotEdgeStmtNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotAttrNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotIdList node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotNodeStmtNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotAttrListNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotDependencyTypeNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotDependencyTypeAttrNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(EdgeOpNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(PropertyDefNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ObjectDefNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(FuncCallNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(AggregateValueDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(PrototypeDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ConditionalStmtNodeIf node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ConditionalStmtNodeIfElse node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(StmtBlockNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(BoolNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(MemberAccessNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(LogicOrNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(LogicAndNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(EqualityNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ComparisonNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(TermNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    return lhsGroum.mergeParallel(rhsGroum);
  }

  @Override
  public Groum visit(FactorNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(UnaryNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(AssignmentNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ListDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(SetDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ListTypeIdentifierNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(SetTypeIdentifierNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(MapTypeIdentifierNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(LoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(WhileLoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(CountingLoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ForLoopStmtNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(LoopBottomMark node) {
    throw new UnsupportedOperationException();
  }


  @Override
  public Groum visit(ItemPrototypeDefinitionNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ImportNode node) {
    throw new UnsupportedOperationException();
  }
}
