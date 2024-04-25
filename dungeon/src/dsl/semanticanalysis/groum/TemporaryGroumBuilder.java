package dsl.semanticanalysis.groum;

import dsl.IndexGenerator;
import dsl.parser.ast.*;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.analyzer.TypeInferrer;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.Symbol;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TemporaryGroumBuilder implements AstVisitor<Groum> {
  private SymbolTable symbolTable;
  private IEnvironment environment;
  private HashMap<Symbol, Long> instanceMap = new HashMap<>();
  private TypeInferrer inferrer;

  public Groum walk(Node astNode, SymbolTable symbolTable, IEnvironment environment) {
    this.symbolTable = symbolTable;
    this.environment = environment;
    this.inferrer = new TypeInferrer(this.symbolTable, null);

    var groumNode = astNode.accept(this);

    this.symbolTable = null;
    this.environment = null;
    this.inferrer = null;

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
        Groum merged = new Groum();
        for (var groum : groums) {
          merged = merged.mergeParallel(groum);
        }
        return merged;
      case GroupedExpression:
        // TODO: could create a new expression here, because this will in fact be evaluated before
        // everything else
        return node.getChild(0).accept(this);
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
    var innerGroum = node.getInnerStmtNode().accept(this);

    // maybe just add the reference in a return stmt as an annotation?
    ControlNode returnNode = new ControlNode(ControlNode.ControlType.returnStmt);
    returnNode.addChildren(innerGroum.nodes);
    var controlGroum = new Groum(returnNode);

    // TODO: ???
    return innerGroum.mergeSequential(controlGroum);
  }

  @Override
  public Groum visit(IdNode node) {
    // Id in expression.. i guess
    Symbol referencedSymbol = symbolTable.getSymbolsForAstNode(node).get(0);
    var action =
        new VariableReferenceAction(referencedSymbol, createOrGetInstanceId(referencedSymbol));
    return new Groum(action);
  }

  @Override
  public Groum visit(VarDeclNode node) {
    Groum rhsGroum = Groum.NONE;
    if (node.getDeclType().equals(VarDeclNode.DeclType.assignmentDecl)) {
      // get rhs
      rhsGroum = node.getRhs().accept(this);
    }

    var id = node.getIdentifier();
    var symbol = symbolTable.getSymbolsForAstNode(id).get(0);

    var instantiationAction = new DefinitionAction(symbol, createOrGetInstanceId(symbol));
    Groum initGroum = new Groum(instantiationAction);
    Groum mergedGroum;
    if (!rhsGroum.equals(Groum.NONE)) {
      // expression action
      ExpressionAction expr = new ExpressionAction(rhsGroum.nodes, IndexGenerator.getIdx());
      Groum exprGroum = new Groum(expr);

      // TODO: is this the correct place to add a data dependency?
      var intermediaryGroum =
          rhsGroum.mergeSequential(exprGroum, GroumEdge.GroumEdgeType.dataDependency);
      // exprGroum = rhsGroum.mergeSequential(exprGroum, GroumEdge.GroumEdgeType.dataDependency);
      mergedGroum = intermediaryGroum.mergeSequential(initGroum);
    } else {
      mergedGroum = initGroum;
    }

    return mergedGroum;
  }

  @Override
  public Groum visit(DecNumNode node) {
    var type = node.accept(this.inferrer);
    var refAction = new ConstRefAction((Symbol) type);
    return new Groum(refAction);
  }

  @Override
  public Groum visit(NumNode node) {
    var type = node.accept(this.inferrer);
    var refAction = new ConstRefAction((Symbol) type);
    return new Groum(refAction);
  }

  @Override
  public Groum visit(StringNode node) {
    var type = node.accept(this.inferrer);
    var refAction = new ConstRefAction((Symbol) type);
    return new Groum(refAction);
  }

  @Override
  public Groum visit(BinaryNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotDefNode node) {
    /*
    // example:
    graph g {
      t1 -> t2;
      t3, t4 -> t5;
    }
    */

    // visit all stmts
    ArrayList<Groum> stmtGroums = new ArrayList<>(node.getStmtNodes().size());
    for (var stmtNode : node.getStmtNodes()) {
      Groum stmtGroum = stmtNode.accept(this);
      stmtGroums.add(stmtGroum);
    }

    Groum stmtGroumsMerged = Groum.NONE;
    for (var stmtGroum : stmtGroums) {
      stmtGroumsMerged = stmtGroumsMerged.mergeSequential(stmtGroum);
    }

    // get symbol of graph
    var groum = Groum.NONE;
    var graphSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);

    // create definition action for graph
    var defAction = new DefinitionAction(graphSymbol, createOrGetInstanceId(graphSymbol));
    var defGroum = new Groum(defAction);

    groum = stmtGroumsMerged.mergeSequential(defGroum);
    // TODO: scope for definition node?

    return groum;
  }

  @Override
  public Groum visit(EdgeRhsNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotEdgeStmtNode node) {
    // TODO: create an "expression"-like group here to cluster the idList-references
    //  based on their occurence in separate statements?
    ArrayList<Groum> idListGroums = new ArrayList<>(node.getIdLists().size());
    for (var idList : node.getIdLists()) {
      var idListGroum = idList.accept(this);
      idListGroums.add(idListGroum);
    }

    Groum mergedIdListGroums = Groum.NONE;
    for (var idListGroum : idListGroums) {
      mergedIdListGroums = mergedIdListGroums.mergeSequential(idListGroum);
    }

    return mergedIdListGroums;
  }

  @Override
  public Groum visit(DotAttrNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(DotIdList node) {
    ArrayList<Groum> idGroums = new ArrayList<>(node.getIdNodes().size());
    for (var idNode : node.getIdNodes()) {
      var idSymbol = this.symbolTable.getSymbolsForAstNode(idNode).get(0);
      if (idSymbol != Symbol.NULL) {
        var refAction = new ReferenceInGraphAction(idSymbol, createOrGetInstanceId(idSymbol));
        Groum refGroum = new Groum(refAction);
        idGroums.add(refGroum);
      }
    }
    Groum mergedGroum = Groum.NONE;
    for (var idGroum :idGroums) {
      mergedGroum = mergedGroum.mergeSequential(idGroum);
    }
    return mergedGroum;
  }

  @Override
  public Groum visit(DotNodeStmtNode node) {
    var idSymbol = this.symbolTable.getSymbolsForAstNode(node.getId()).get(0);
    ReferenceInGraphAction referenceInGraphAction = new ReferenceInGraphAction(idSymbol, createOrGetInstanceId(idSymbol));
    return new Groum(referenceInGraphAction);
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
    // visit stmt
    var stmtGroum = node.getStmtNode().accept(this);

    // get symbol
    var propertySymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    // TODO: are there problems related to instance id, because references a property of a datatype?
    DefinitionAction definitionAction = new DefinitionAction(propertySymbol, createOrGetInstanceId(propertySymbol));
    Groum definitionGroum = new Groum(definitionAction);
    Groum merged = stmtGroum.mergeSequential(definitionGroum);

    return merged;
  }

  @Override
  public Groum visit(ObjectDefNode node) {
    // visit all property definitions

    ArrayList<Groum> propertyDefGroums = new ArrayList<>(node.getPropertyDefinitions().size());
    for (var propDef : node.getPropertyDefinitions()) {
      Groum propDefGroum = propDef.accept(this);
      propertyDefGroums.add(propDefGroum);
    }

    Groum mergedPropDefGroums = Groum.NONE;
    for (var propDefGroum : propertyDefGroums) {
      mergedPropDefGroums = mergedPropDefGroums.mergeSequential(propDefGroum);
    }

    // get object symbol
    var objectSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);

    // create object def action
    DefinitionAction objectDefAction = new DefinitionAction(objectSymbol, createOrGetInstanceId(objectSymbol));
    Groum definitionGroum = new Groum(objectDefAction);
    Groum groum = mergedPropDefGroums.mergeSequential(definitionGroum);

    return groum;
  }

  @Override
  public Groum visit(FuncCallNode node) {
    var mergedParameterGroums = getGroumForParameters(node);

    // add funcCall
    var functionSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    FunctionCallAction action = new FunctionCallAction(functionSymbol, createOrGetInstanceId(functionSymbol));
    Groum finalGroum = mergedParameterGroums.mergeSequential(action);

    return finalGroum;
  }

  private Groum getGroumForParameters(FuncCallNode node) {
    // visit all parameters
    ArrayList<Groum> parameterGroums = new ArrayList<>(node.getParameters().size());
    for (var paramNode : node.getParameters()) {
      var paramGroum = paramNode.accept(this);
      var parameterAction = new PassAsParameterAction(paramGroum.nodes, IndexGenerator.getIdx());
      Groum mergedPassGroum = paramGroum.mergeSequential(parameterAction);
      parameterGroums.add(mergedPassGroum);
    }

    Groum mergedParameterGroums = Groum.NONE;
    for (var parameterGroum : parameterGroums) {
      mergedParameterGroums = mergedParameterGroums.mergeParallel(parameterGroum);
    }

    return mergedParameterGroums;
  }

  @Override
  public Groum visit(AggregateValueDefinitionNode node) {
    // visit all property definitions
    ArrayList<Groum> propertyDefinitionGroums = new ArrayList<>(node.getPropertyDefinitionNodes().size());
    for (var propDefNode : node.getPropertyDefinitionNodes()) {
      var propDefGroum = propDefNode.accept(this);
      propertyDefinitionGroums.add(propDefGroum);
    }

    // merge them all
    Groum mergedPropertyDefinitionGroums = Groum.NONE;
    for (var groum : propertyDefinitionGroums) {
      // TODO: parallel or sequential?
      mergedPropertyDefinitionGroums = mergedPropertyDefinitionGroums.mergeParallel(groum);
    }

    // add def node for aggergate value def
    var valueSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    var definitionAction = new DefinitionAction(valueSymbol, createOrGetInstanceId(valueSymbol));

    Groum mergedGroum = mergedPropertyDefinitionGroums.mergeSequential(definitionAction);
    return mergedGroum;
  }

  @Override
  public Groum visit(PrototypeDefinitionNode node) {
    // visit all component definitions
    ArrayList<Groum> componentDefGroums = new ArrayList<>(node.getComponentDefinitionNodes().size());
    for (var componentDefNode : node.getComponentDefinitionNodes()) {
      var componentDefGroum = componentDefNode.accept(this);
      componentDefGroums.add(componentDefGroum);
    }

    Groum mergedComponentDefinitionGroums = Groum.NONE;
    for (var groum : componentDefGroums) {
      // TODO: parallel or sequential?
      mergedComponentDefinitionGroums = mergedComponentDefinitionGroums.mergeParallel(groum);
    }

    // create definition action
    var protoTypeSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    var defAction = new DefinitionAction(protoTypeSymbol, createOrGetInstanceId(protoTypeSymbol));
    Groum merged = mergedComponentDefinitionGroums.mergeSequential(defAction);

    return merged;
  }

  @Override
  public Groum visit(ConditionalStmtNodeIf node) {
    // visit condition
    Groum conditionGroum = node.getCondition().accept(this);
    Groum stmtGroum = node.getIfStmt().accept(this);

    var ifControlNode = new ControlNode(ControlNode.ControlType.ifStmt);
    ifControlNode.addChildren(conditionGroum.nodes);
    ifControlNode.addChildren(stmtGroum.nodes);

    Groum merged = conditionGroum.mergeSequential(ifControlNode);
    merged = merged.mergeSequential(stmtGroum);

    return merged;
  }

  @Override
  public Groum visit(ConditionalStmtNodeIfElse node) {
    // visit condition and branches
    Groum conditionGroum = node.getCondition().accept(this);
    Groum ifStmtGroum = node.getIfStmt().accept(this);
    Groum elseStmtGroum = node.getElseStmt().accept(this);

    // create control nodes
    var parentControlNode = new ControlNode(ControlNode.ControlType.ifElseStmt);
    var ifControlNode = new ControlNode(ControlNode.ControlType.ifStmt);
    var elseControlNode = new ControlNode(ControlNode.ControlType.elseStmt);

    // add groum nodes as children to their respective parents
    parentControlNode.addChildren(conditionGroum.nodes);
    ifControlNode.addChildren(ifStmtGroum.nodes);
    elseControlNode.addChildren(elseStmtGroum.nodes);

    // merge groums for conditions and branches
    var mergedIfBranch = new Groum(ifControlNode).mergeSequential(ifStmtGroum);
    var mergedElseBranch = new Groum(elseControlNode).mergeSequential(elseStmtGroum);

    // add the merged branches as children to the parent control node
    parentControlNode.addChildren(mergedIfBranch.nodes);
    parentControlNode.addChildren(mergedElseBranch.nodes);

    // merge groums
    var parentControlWithCondition = conditionGroum.mergeSequential(parentControlNode);
    Groum mergedBranches = mergedIfBranch.mergeParallel(mergedElseBranch);
    Groum merged = parentControlWithCondition.mergeSequential(mergedBranches);

    return merged;
  }

  @Override
  public Groum visit(StmtBlockNode node) {
    // get all stmts
    ArrayList<Groum> stmtGroums = new ArrayList<>(node.getStmts().size());
    for (var stmt : node.getStmts()) {
      stmtGroums.add(stmt.accept(this));
    }

    // merge them all under stmt node
    var blockAction = new ControlNode(ControlNode.ControlType.block);
    for (var stmtGroum : stmtGroums) {
      blockAction.addChildren(stmtGroum.nodes);
    }
    Groum blockGroum = new Groum(blockAction);
    for (var groum : stmtGroums) {
      // TODO: add the nodes as children to calculate scope?

      blockGroum = blockGroum.mergeSequential(groum);
    }

    return blockGroum;
  }

  @Override
  public Groum visit(BoolNode node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(MemberAccessNode node) {
    // lhs is never member access, whenever we chain member accesses,
    // rhs is the 'deeper' member access
    var lhsSymbol = this.symbolTable.getSymbolsForAstNode(node.getLhs()).get(0);

    // if the lhs of the member access is a callable, we generally want to use the return type as the 'scope'
    // in which the member access is happening
    if (lhsSymbol instanceof ICallable callable) {
      lhsSymbol = (Symbol)callable.getFunctionType().getReturnType();
    }

    Groum mergedGroum = Groum.NONE;
    // in a method call, we first land here
    if (node.getRhs().type.equals(Node.Type.FuncCall)) {
      Groum parameterEvaluationGroum = getGroumForParameters((FuncCallNode)node.getRhs());

      Symbol functionSymbol = this.symbolTable.getSymbolsForAstNode(node.getRhs()).get(0);
      var methodAccessAction = new MethodAccessAction(lhsSymbol, functionSymbol, createOrGetInstanceId(lhsSymbol));

      // add scoping information: the parameter evaluations are 'owned' by the method access
      methodAccessAction.addChildren(parameterEvaluationGroum.nodes);

      mergedGroum = parameterEvaluationGroum.mergeSequential(methodAccessAction);
    } else if (node.getRhs().type.equals(Node.Type.MemberAccess)) {

      // visit inner member access
      var innerMemberAccessGroum = node.getRhs().accept(this);

      // need to get the information about lhs of deeper member access
      var lhsOfInnerMemberAccess = node.getRhs().getChild(0);
      var innerLhsSymbol = this.symbolTable.getSymbolsForAstNode(lhsOfInnerMemberAccess).get(0);

      Groum accessGroum;
      if (lhsOfInnerMemberAccess.type.equals(Node.Type.Identifier)) {
        var accessAction = new PropertyAccessAction(lhsSymbol, innerLhsSymbol, createOrGetInstanceId(lhsSymbol));

        // add scoping information: the inner member access is 'owned' by this property access
        accessAction.addChildren(innerMemberAccessGroum.nodes);
        accessGroum = new Groum(accessAction);
      } else {
        // function call node
        // handle method access
        Groum parameterEvaluationGroum = getGroumForParameters((FuncCallNode)lhsOfInnerMemberAccess);
        var methodAccessAction = new MethodAccessAction(lhsSymbol, innerLhsSymbol, createOrGetInstanceId(lhsSymbol));

        // add scoping information: the parameter evaluation and inner member access is 'owned' by the method access
        methodAccessAction.addChildren(parameterEvaluationGroum.nodes);
        methodAccessAction.addChildren(innerMemberAccessGroum.nodes);
        accessGroum = parameterEvaluationGroum.mergeSequential(methodAccessAction);
      }

      mergedGroum = accessGroum.mergeSequential(innerMemberAccessGroum);
    } else if (node.getRhs().type.equals(Node.Type.Identifier)) {
      // property access
      var rhsSymbol = this.symbolTable.getSymbolsForAstNode(node.getRhs()).get(0);
      var accessAction = new PropertyAccessAction(lhsSymbol, rhsSymbol, createOrGetInstanceId(lhsSymbol));

      mergedGroum = new Groum(accessAction);
    }

    return mergedGroum;
  }

  @Override
  public Groum visit(LogicOrNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    return lhsGroum.mergeParallel(rhsGroum);
  }

  @Override
  public Groum visit(LogicAndNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    return lhsGroum.mergeParallel(rhsGroum);
  }

  @Override
  public Groum visit(EqualityNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    return lhsGroum.mergeParallel(rhsGroum);
  }

  @Override
  public Groum visit(ComparisonNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    return lhsGroum.mergeParallel(rhsGroum);
  }

  @Override
  public Groum visit(TermNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    return lhsGroum.mergeParallel(rhsGroum);
  }

  @Override
  public Groum visit(FactorNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    return lhsGroum.mergeParallel(rhsGroum);
  }

  @Override
  public Groum visit(UnaryNode node) {
    return node.getInnerNode().accept(this);
  }

  @Override
  public Groum visit(AssignmentNode node) {
    // basically like init

    // get rhs
    Groum rhsGroum = node.getRhs().accept(this);

    var id = node.getLhs();
    var symbol = symbolTable.getSymbolsForAstNode(id).get(0);

    // TODO: this does not take into account the data dependencies!!
    var assignmentAction = new DefinitionAction(symbol, createOrGetInstanceId(symbol));
    Groum assignmentGroum = new Groum(assignmentAction);
    Groum mergedGroum;
    if (!rhsGroum.equals(Groum.NONE)) {
      // expression action
      ExpressionAction expr = new ExpressionAction(rhsGroum.nodes, IndexGenerator.getIdx());
      Groum exprGroum = new Groum(expr);

      var intermediaryGroum =
          rhsGroum.mergeSequential(exprGroum, GroumEdge.GroumEdgeType.dataDependency);
      // exprGroum = rhsGroum.mergeSequential(exprGroum, GroumEdge.GroumEdgeType.dataDependency);
      mergedGroum = intermediaryGroum.mergeSequential(assignmentGroum);
    } else {
      mergedGroum = assignmentGroum;
    }

    return mergedGroum;
  }

  @Override
  public Groum visit(ListDefinitionNode node) {
    // get groums for entries
    ArrayList<Groum> entryGroums = new ArrayList<>(node.getEntries().size());
    for (var entry : node.getEntries()) {
      var groum = entry.accept(this);
      entryGroums.add(groum);
    }

    // merge all entry groums parallely under expression node
    Groum mergedEntryGroums = Groum.NONE;
    for (Groum entryGroum : entryGroums) {
      mergedEntryGroums = mergedEntryGroums.mergeParallel(entryGroum);
    }

    return mergedEntryGroums;
  }

  @Override
  public Groum visit(SetDefinitionNode node) {
    // get groums for entries
    ArrayList<Groum> entryGroums = new ArrayList<>(node.getEntries().size());
    for (var entry : node.getEntries()) {
      var groum = entry.accept(this);
      entryGroums.add(groum);
    }

    // merge all entry groums parallely under expression node
    Groum mergedEntryGroums = Groum.NONE;
    for (Groum entryGroum : entryGroums) {
      mergedEntryGroums = mergedEntryGroums.mergeParallel(entryGroum);
    }

    return mergedEntryGroums;
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
    Groum conditionGroum = node.getExpressionNode().accept(this);
    Groum conditionExpressionGroum =
        new Groum(new ExpressionAction(conditionGroum.nodes, IndexGenerator.getIdx()));
    Groum mergedGroum = conditionGroum.mergeSequential(conditionExpressionGroum);

    var controlNode = new ControlNode(ControlNode.ControlType.whileLoop);
    Groum controlGroum = new Groum(controlNode);
    mergedGroum = mergedGroum.mergeSequential(controlGroum);

    var stmtGroum = node.getStmtNode().accept(this);
    mergedGroum = mergedGroum.mergeSequential(stmtGroum);

    // Note: this will cause the controlNode to contain itself
    controlNode.addChildren(mergedGroum.nodes);

    return mergedGroum;
  }

  @Override
  public Groum visit(CountingLoopStmtNode node) {
    Groum mergedGroum = Groum.NONE;

    var iterableSymbol = this.symbolTable.getSymbolsForAstNode(node.getIterableIdNode()).get(0);
    var iterableRef = new VariableReferenceAction(iterableSymbol, createOrGetInstanceId(iterableSymbol));
    mergedGroum = mergedGroum.mergeSequential(iterableRef);

    var varSymbol = this.symbolTable.getSymbolsForAstNode(node.getVarIdNode()).get(0);
    var varDef = new DefinitionAction(varSymbol, createOrGetInstanceId(varSymbol));
    mergedGroum = mergedGroum.mergeSequential(varDef);

    var controlNode = new ControlNode(ControlNode.ControlType.forLoop);
    Groum controlGroum = new Groum(controlNode);
    mergedGroum = mergedGroum.mergeSequential(controlGroum);

    // counter variable definition
    var counterSymbol = this.symbolTable.getSymbolsForAstNode(node.getCounterIdNode()).get(0);
    var counterDef = new DefinitionAction(counterSymbol, createOrGetInstanceId(counterSymbol));
    mergedGroum = mergedGroum.mergeSequential(counterDef);

    var stmtGroum = node.getStmtNode().accept(this);
    mergedGroum = mergedGroum.mergeSequential(stmtGroum);

    // Note: this will cause the controlNode to contain itself
    controlNode.addChildren(mergedGroum.nodes);

    return mergedGroum;
  }

  @Override
  public Groum visit(ForLoopStmtNode node) {
    Groum mergedGroum = Groum.NONE;

    var iterableSymbol = this.symbolTable.getSymbolsForAstNode(node.getIterableIdNode()).get(0);
    var iterableRef = new VariableReferenceAction(iterableSymbol, createOrGetInstanceId(iterableSymbol));
    mergedGroum = mergedGroum.mergeSequential(iterableRef);

    var varSymbol = this.symbolTable.getSymbolsForAstNode(node.getVarIdNode()).get(0);
    var varDef = new DefinitionAction(varSymbol, createOrGetInstanceId(varSymbol));
    mergedGroum = mergedGroum.mergeSequential(varDef);

    var controlNode = new ControlNode(ControlNode.ControlType.forLoop);
    Groum controlGroum = new Groum(controlNode);
    mergedGroum = mergedGroum.mergeSequential(controlGroum);

    var stmtGroum = node.getStmtNode().accept(this);
    mergedGroum = mergedGroum.mergeSequential(stmtGroum);

    // Note: this will cause the controlNode to contain itself
    controlNode.addChildren(mergedGroum.nodes);

    return mergedGroum;
  }

  @Override
  public Groum visit(LoopBottomMark node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Groum visit(ItemPrototypeDefinitionNode node) {
    // visit all property definitions
    ArrayList<Groum> propertyDefinitionGroums = new ArrayList<>(node.getPropertyDefinitionNodes().size());
    for (var propDefNode : node.getPropertyDefinitionNodes()) {
      var propDefGroum = propDefNode.accept(this);
      propertyDefinitionGroums.add(propDefGroum);
    }

    // merge them all
    Groum mergedPropertyDefinitionGroums = Groum.NONE;
    for (var groum : propertyDefinitionGroums) {
      // TODO: parallel or sequential?
      mergedPropertyDefinitionGroums = mergedPropertyDefinitionGroums.mergeParallel(groum);
    }

    // add def node for aggergate value def
    var valueSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    var definitionAction = new DefinitionAction(valueSymbol, createOrGetInstanceId(valueSymbol));

    Groum mergedGroum = mergedPropertyDefinitionGroums.mergeSequential(definitionAction);
    return mergedGroum;
  }

  @Override
  public Groum visit(ImportNode node) {
    var importSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    var defAction = new DefinitionByImportAction(importSymbol, createOrGetInstanceId(importSymbol));
    return new Groum(defAction);
  }
}
