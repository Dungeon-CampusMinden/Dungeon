package dsl.semanticanalysis.groum;

import dsl.IndexGenerator;
import dsl.parser.ast.*;
import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.analyzer.TypeInferrer;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.groum.node.*;
import dsl.semanticanalysis.symbol.Symbol;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class TemporalGroumBuilder implements AstVisitor<Groum> {
  private SymbolTable symbolTable;
  // private IEnvironment environment;

  // this will store instance ids for specific symbols
  private HashMap<Symbol, Long> symbolInstanceMap;
  private HashMap<Object, Long> constValueInstanceMap;

  // this will store instance ids for specific ast nodes; this is needed, because
  // a property definition may be child of a aggregate value definition which is
  // child of a global object definition; in this case, only the global
  // object definition will have a unique symbol instance id, the value definitions
  // and property definitions inside it will just link to the symbol inside the
  // objects aggregate data type, which won't be unique
  private HashMap<Node, Long> nodeInstanceMap;

  private HashMap<Long, HashMap<Symbol, Long>> memberAccessInstanceMap;
  private Stack<Long> memberAccessContextStack;
  private TypeInferrer inferrer;

  public Groum walk(
      Node astNode,
      SymbolTable symbolTable,
      IEnvironment environment,
      HashMap<Symbol, Long> instanceMap) {
    this.symbolTable = symbolTable;
    // this.environment = environment;
    this.symbolInstanceMap = instanceMap;
    this.nodeInstanceMap = new HashMap<>();
    this.memberAccessContextStack = new Stack<>();
    this.memberAccessInstanceMap = new HashMap<>();
    this.constValueInstanceMap = new HashMap<>();
    this.inferrer = new TypeInferrer(this.symbolTable, null);

    var groumNode = astNode.accept(this);

    this.symbolTable = null;
    // this.environment = null;
    this.inferrer = null;
    this.symbolInstanceMap = null;

    return groumNode;
  }

  public Groum walk(Node astNode, SymbolTable symbolTable, IEnvironment environment) {
    return this.walk(astNode, symbolTable, environment, new HashMap<>());
  }

  private long createOrGetInstanceId(Symbol symbol) {
    if (!this.symbolInstanceMap.containsKey(symbol)) {
      this.symbolInstanceMap.put(symbol, IndexGenerator.getIdx());
    }
    return this.symbolInstanceMap.get(symbol);
  }

  private long createOrGetMemberInstanceId(
      Long contextInstanceId, Symbol symbolToResolveInContext) {
    // lookup contextInstanceId in memberAccessInstanceMap
    if (!this.memberAccessInstanceMap.containsKey(contextInstanceId)) {
      createContextMap(contextInstanceId);
    }

    if (this.memberAccessInstanceMap.containsKey(contextInstanceId)) {
      // this maps the symbol resolved in the context to other instance ids
      var contextMap = this.memberAccessInstanceMap.get(contextInstanceId);

      if (!contextMap.containsKey(symbolToResolveInContext)) {
        contextMap.put(symbolToResolveInContext, IndexGenerator.getIdx());
      }

      return contextMap.get(symbolToResolveInContext);
    }
    return -1;
  }

  private void createContextMap(Long instanceIdToCreateContextFor) {
    if (!this.memberAccessInstanceMap.containsKey(instanceIdToCreateContextFor)) {
      this.memberAccessInstanceMap.put(instanceIdToCreateContextFor, new HashMap<>());
    }
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
    // add begin node
    merged = merged.mergeSequential(new ControlNode(ControlNode.ControlType.beginFunc));
    Groum mergedParamGroum = new Groum();

    for (var paramGroum : paramGroums) {
      mergedParamGroum = mergedParamGroum.mergeParallel(paramGroum);
    }
    merged = merged.mergeSequential(mergedParamGroum);

    for (var stmtGroum : stmtGroums) {
      merged = merged.mergeSequential(stmtGroum);
    }

    var funcSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    var funcDefAction = new DefinitionAction(funcSymbol, createOrGetInstanceId(funcSymbol));
    funcDefAction.relatedAstNode(node);
    merged = merged.mergeSequential(new ControlNode(ControlNode.ControlType.endFunc));

    funcDefAction.addChildren(merged.nodes);
    merged = merged.mergeSequential(funcDefAction);

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
    action.relatedAstNode(node);
    return new Groum(action);
  }

  @Override
  public Groum visit(ReturnStmtNode node) {
    var innerGroum = node.getInnerStmtNode().accept(this);

    // maybe just add the reference in a return stmt as an annotation?
    ControlNode returnNode = new ControlNode(ControlNode.ControlType.returnStmt);
    returnNode.addChildren(innerGroum.nodes);
    returnNode.relatedAstNode(node);
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
    action.relatedAstNode(node);
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
    instantiationAction.relatedAstNode(node);

    Groum initGroum = new Groum(instantiationAction);
    Groum mergedGroum;
    if (!rhsGroum.equals(Groum.NONE)) {
      // expression action
      ExpressionAction expr = new ExpressionAction(rhsGroum.nodes, IndexGenerator.getIdx());
      expr.relatedAstNode(node);
      Groum exprGroum = new Groum(expr);

      var intermediaryGroum = rhsGroum.mergeSequential(exprGroum);
      // exprGroum = rhsGroum.mergeSequential(exprGroum, GroumEdge.GroumEdgeType.dataDependency);
      mergedGroum = intermediaryGroum.mergeSequential(initGroum);
    } else {
      mergedGroum = initGroum;
    }

    return mergedGroum;
  }

  public Long getConstValueInstanceId(Object value) {
    if (!this.constValueInstanceMap.containsKey(value)) {
      this.constValueInstanceMap.put(value, IndexGenerator.getIdx());
    }
    return this.constValueInstanceMap.get(value);
  }

  @Override
  public Groum visit(DecNumNode node) {
    var type = node.accept(this.inferrer);
    Long instanceId = getConstValueInstanceId(node.getValue());
    var refAction = new ConstRefAction((Symbol) type, node.getValue(), instanceId);
    refAction.relatedAstNode(node);
    return new Groum(refAction);
  }

  @Override
  public Groum visit(NumNode node) {
    var type = node.accept(this.inferrer);
    Long instanceId = getConstValueInstanceId(node.getValue());
    var refAction = new ConstRefAction((Symbol) type, node.getValue(), instanceId);
    refAction.relatedAstNode(node);
    return new Groum(refAction);
  }

  @Override
  public Groum visit(StringNode node) {
    var type = node.accept(this.inferrer);
    Long instanceId = getConstValueInstanceId(node.getValue());
    var refAction = new ConstRefAction((Symbol) type, node.getValue(), instanceId);
    refAction.relatedAstNode(node);
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
      // TODO: doing this parallel has no effect for single edge, since both references are added
      // temporally sequential
      //  do we need to preserve the edge information (which node to which edge)? if not, we could
      // just
      //  treat dot edges sequentially as well
      stmtGroumsMerged = stmtGroumsMerged.mergeParallel(stmtGroum);
    }

    // get symbol of graph
    var groum = Groum.NONE;
    var graphSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);

    // create definition action for graph
    var defAction = new DefinitionAction(graphSymbol, createOrGetInstanceId(graphSymbol));
    defAction.relatedAstNode(node);
    var defGroum = new Groum(defAction);

    defAction.addChildren(stmtGroumsMerged.nodes);

    groum =
        stmtGroumsMerged.mergeSequential(
            defGroum,
            List.of(GroumEdge.GroumEdgeType.EDGE_DATA_READ, GroumEdge.GroumEdgeType.EDGE_TEMPORAL));

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
      // mergedIdListGroums = mergedIdListGroums.mergeSequential(idListGroum);
      mergedIdListGroums = mergedIdListGroums.mergeParallel(idListGroum);
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
        refAction.relatedAstNode(node);
        Groum refGroum = new Groum(refAction);
        idGroums.add(refGroum);
      }
    }
    Groum mergedGroum = Groum.NONE;
    for (var idGroum : idGroums) {
      // mergedGroum = mergedGroum.mergeSequential(idGroum);
      mergedGroum = mergedGroum.mergeParallel(idGroum);
    }
    return mergedGroum;
  }

  @Override
  public Groum visit(DotNodeStmtNode node) {
    var idSymbol = this.symbolTable.getSymbolsForAstNode(node.getIdentifier()).get(0);
    ReferenceInGraphAction referenceInGraphAction =
        new ReferenceInGraphAction(idSymbol, createOrGetInstanceId(idSymbol));
    referenceInGraphAction.relatedAstNode(node);
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
    // get symbol
    var propertySymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    // get parent object definition and get instanced id
    var parent = node.getParent();
    var parentsParent = parent.getParent();
    long propertyInstanceId = -1;
    if (parentsParent.type == Node.Type.ObjectDefinition) {
      Symbol symbolOfParent = this.symbolTable.getSymbolsForAstNode(parentsParent).get(0);
      long parentInstanceId = createOrGetInstanceId(symbolOfParent);
      propertyInstanceId = createOrGetMemberInstanceId(parentInstanceId, propertySymbol);
    } else if (parentsParent.type == Node.Type.AggregateValueDefinition) {
      long parentsInstanceId = this.nodeInstanceMap.getOrDefault(parentsParent, -1L);
      propertyInstanceId = createOrGetMemberInstanceId(parentsInstanceId, propertySymbol);
    }

    this.nodeInstanceMap.put(node, propertyInstanceId);

    // visit stmt
    var stmtGroum = node.getStmtNode().accept(this);

    DefinitionAction definitionAction = new DefinitionAction(propertySymbol, propertyInstanceId);
    definitionAction.relatedAstNode(node);

    Groum definitionGroum = new Groum(definitionAction);
    Groum merged =
        stmtGroum.mergeSequential(
            definitionGroum,
            List.of(GroumEdge.GroumEdgeType.EDGE_DATA_READ, GroumEdge.GroumEdgeType.EDGE_TEMPORAL));

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
      // mergedPropDefGroums = mergedPropDefGroums.mergeSequential(propDefGroum);
      mergedPropDefGroums = mergedPropDefGroums.mergeParallel(propDefGroum);
    }

    // get object symbol
    var objectSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);

    // create object def action
    // TODO: make it an object definition (explicit subclass)
    //  - visit method should include data dependency of all previous definition nodes!
    //  - or just add the data dependency here?
    DefinitionAction objectDefAction =
        new DefinitionAction(objectSymbol, createOrGetInstanceId(objectSymbol));
    objectDefAction.relatedAstNode(node);
    objectDefAction.addChildren(mergedPropDefGroums.nodes);

    Groum definitionGroum = new Groum(objectDefAction);
    // Groum groum = mergedPropDefGroums.mergeSequential(definitionGroum);
    Groum groum =
        mergedPropDefGroums.mergeSequential(
            definitionGroum,
            List.of(GroumEdge.GroumEdgeType.EDGE_DATA_READ, GroumEdge.GroumEdgeType.EDGE_TEMPORAL));

    return groum;
  }

  @Override
  public Groum visit(FuncCallNode node) {
    var mergedParameterGroums = getGroumForParameters(node);

    // add funcCall
    var functionSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    FunctionCallAction action =
        new FunctionCallAction(functionSymbol, createOrGetInstanceId(functionSymbol));
    action.relatedAstNode(node);
    Groum finalGroum = mergedParameterGroums.mergeSequential(action);

    return finalGroum;
  }

  private Groum getGroumForParameters(FuncCallNode node) {
    // visit all parameters
    ArrayList<Groum> parameterGroums = new ArrayList<>(node.getParameters().size());
    for (var paramNode : node.getParameters()) {
      var paramGroum = paramNode.accept(this);
      var parameterAction = new PassAsParameterAction(paramGroum.nodes, IndexGenerator.getIdx());
      parameterAction.relatedAstNode(node);

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
    // add def node for aggergate value def
    var valueSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);

    // get parent of value definition
    var nodesParent = node.getParent();
    var parentsParent = nodesParent.getParent();
    long valuesInstanceId = -1;
    if (parentsParent.type == Node.Type.PrototypeDefinition
        || parentsParent.type == Node.Type.ItemPrototypeDefinition) {
      // parent is a unique symbol.. but we could also just reference the instance id for the AST
      // node..
      var parentsSymbol = this.symbolTable.getSymbolsForAstNode(parentsParent).get(0);
      var parentsInstanceId = createOrGetInstanceId(parentsSymbol);
      valuesInstanceId = createOrGetMemberInstanceId(parentsInstanceId, valueSymbol);
    } else if (nodesParent.type == Node.Type.AggregateValueDefinition) {
      // the node is part of another aggregate value definition
      boolean b = true;
    } else if (nodesParent.type == Node.Type.PropertyDefinition) {
      // the node is part of another property definition
      long parentsInstanceId = this.nodeInstanceMap.get(nodesParent);
      valuesInstanceId = createOrGetMemberInstanceId(parentsInstanceId, valueSymbol);
    }

    // add values instance id to map
    this.nodeInstanceMap.put(node, valuesInstanceId);

    // visit all property definitions
    ArrayList<Groum> propertyDefinitionGroums =
        new ArrayList<>(node.getPropertyDefinitionNodes().size());
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

    var definitionAction = new DefinitionAction(valueSymbol, valuesInstanceId);
    definitionAction.relatedAstNode(node);
    Groum definitionGroum = new Groum(definitionAction);

    Groum mergedGroum =
        mergedPropertyDefinitionGroums.mergeSequential(
            definitionGroum,
            List.of(GroumEdge.GroumEdgeType.EDGE_DATA_READ, GroumEdge.GroumEdgeType.EDGE_TEMPORAL));
    return mergedGroum;
  }

  @Override
  public Groum visit(PrototypeDefinitionNode node) {
    // visit all component definitions
    ArrayList<Groum> componentDefGroums =
        new ArrayList<>(node.getComponentDefinitionNodes().size());
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
    defAction.relatedAstNode(node);

    defAction.addChildren(mergedComponentDefinitionGroums.nodes);
    var defGroum = new Groum(defAction);
    Groum merged =
        mergedComponentDefinitionGroums.mergeSequential(
            defGroum,
            List.of(GroumEdge.GroumEdgeType.EDGE_DATA_READ, GroumEdge.GroumEdgeType.EDGE_TEMPORAL));

    return merged;
  }

  @Override
  public Groum visit(ConditionalStmtNodeIf node) {
    // visit condition
    Groum conditionGroum = node.getCondition().accept(this);
    Groum stmtGroum = node.getIfStmt().accept(this);

    var ifControlNode = new ControlNode(ControlNode.ControlType.ifStmt);
    ifControlNode.relatedAstNode(node);

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
    parentControlNode.relatedAstNode(node);

    var ifControlNode = new ControlNode(ControlNode.ControlType.ifStmt);
    ifControlNode.relatedAstNode(node.getIfStmt());

    var elseControlNode = new ControlNode(ControlNode.ControlType.elseStmt);
    elseControlNode.relatedAstNode(node.getElseStmt());

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

    // force calculation of ifStmt before elseStmt
    var sinkNodes = mergedIfBranch.sinkNodes();
    if (!sinkNodes.isEmpty()) {
      var sinkNode = sinkNodes.getFirst();
      var forceEdge =
          new GroumEdge(
              sinkNode, elseControlNode, GroumEdge.GroumEdgeType.EDGE_TEMPORAL, false, true);
      merged.addEdge(forceEdge);
    }

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
    blockAction.relatedAstNode(node);
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

    // if the lhs of the member access is a callable, we generally want to use the return type as
    // the 'scope'
    // in which the member access is happening
    if (lhsSymbol instanceof ICallable callable) {
      lhsSymbol = (Symbol) callable.getFunctionType().getReturnType();
    }

    // setup context
    Long contextInstanceId;
    if (node.getParent().type == Node.Type.MemberAccess) {
      // if the parent is also a member access, another context will be on the context stack
      // -> get that id, that is the parents context
      var parentsInstanceId = this.memberAccessContextStack.peek();

      // create a new member instance id in the context of the parents instance for lhs symbol
      contextInstanceId = createOrGetMemberInstanceId(parentsInstanceId, lhsSymbol);
    } else {
      contextInstanceId = createOrGetInstanceId(lhsSymbol);
    }

    this.memberAccessContextStack.push(contextInstanceId);
    createContextMap(contextInstanceId);

    Groum mergedGroum = Groum.NONE;
    // in a method call, we first land here
    if (node.getRhs().type.equals(Node.Type.FuncCall)) {
      Groum parameterEvaluationGroum = getGroumForParameters((FuncCallNode) node.getRhs());

      Symbol functionSymbol = this.symbolTable.getSymbolsForAstNode(node.getRhs()).get(0);
      var methodAccessAction = new MethodAccessAction(lhsSymbol, functionSymbol, contextInstanceId);
      methodAccessAction.relatedAstNode(node);

      // add scoping information: the parameter evaluations are 'owned' by the method access
      methodAccessAction.addChildren(parameterEvaluationGroum.nodes);

      var lhsRedefinitionAction = new DefinitionAction(lhsSymbol, contextInstanceId);
      lhsRedefinitionAction.relatedAstNode(node);

      methodAccessAction.instanceRedefinitionNode(lhsRedefinitionAction);
      methodAccessAction.addChild(lhsRedefinitionAction);

      var methodEvaluation = parameterEvaluationGroum.mergeSequential(methodAccessAction);
      var methodWithRedefinition = Groum.NONE;

      methodWithRedefinition = methodWithRedefinition.mergeParallel(methodEvaluation);
      methodWithRedefinition = methodWithRedefinition.mergeParallel(lhsRedefinitionAction);

      mergedGroum = methodWithRedefinition;
    } else if (node.getRhs().type.equals(Node.Type.MemberAccess)) {

      // visit inner member access
      var innerMemberAccessGroum = node.getRhs().accept(this);

      // need to get the information about lhs of deeper member access
      var lhsOfInnerMemberAccess = node.getRhs().getChild(0);
      var innerLhsSymbol = this.symbolTable.getSymbolsForAstNode(lhsOfInnerMemberAccess).get(0);

      Groum accessGroum;
      if (lhsOfInnerMemberAccess.type.equals(Node.Type.Identifier)) {
        var memberInstanceId = createOrGetMemberInstanceId(contextInstanceId, innerLhsSymbol);
        var accessAction =
            new PropertyAccessAction(
                lhsSymbol, innerLhsSymbol, contextInstanceId, memberInstanceId);
        accessAction.relatedAstNode(node);

        // add scoping information: the inner member access is 'owned' by this property access
        accessAction.addChildren(innerMemberAccessGroum.nodes);
        accessGroum = new Groum(accessAction);
      } else {
        // function call node
        // handle method access
        Groum parameterEvaluationGroum =
            getGroumForParameters((FuncCallNode) lhsOfInnerMemberAccess);

        var methodAccessAction =
            new MethodAccessAction(lhsSymbol, innerLhsSymbol, contextInstanceId);
        methodAccessAction.relatedAstNode(node);

        // add scoping information: the parameter evaluations are 'owned' by the method access
        methodAccessAction.addChildren(parameterEvaluationGroum.nodes);
        methodAccessAction.addChildren(innerMemberAccessGroum.nodes);

        var lhsRedefinitionAction = new DefinitionAction(lhsSymbol, contextInstanceId);
        lhsRedefinitionAction.relatedAstNode(node);

        methodAccessAction.instanceRedefinitionNode(lhsRedefinitionAction);
        methodAccessAction.addChild(lhsRedefinitionAction);

        var methodEvaluation = parameterEvaluationGroum.mergeSequential(methodAccessAction);
        var methodWithRedefinition = Groum.NONE;

        methodWithRedefinition = methodWithRedefinition.mergeParallel(methodEvaluation);
        methodWithRedefinition = methodWithRedefinition.mergeParallel(lhsRedefinitionAction);

        accessGroum = methodWithRedefinition;
      }

      mergedGroum = accessGroum.mergeSequential(innerMemberAccessGroum);
    } else if (node.getRhs().type.equals(Node.Type.Identifier)) {
      // TODO: calculate instance id for whole member access!

      // property access
      var rhsSymbol = this.symbolTable.getSymbolsForAstNode(node.getRhs()).get(0);
      var memberInstanceId = createOrGetMemberInstanceId(contextInstanceId, rhsSymbol);
      var accessAction =
          new PropertyAccessAction(lhsSymbol, rhsSymbol, contextInstanceId, memberInstanceId);
      accessAction.relatedAstNode(node);

      mergedGroum = new Groum(accessAction);
    }

    this.memberAccessContextStack.pop();

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
    var mergedGroum = lhsGroum.mergeParallel(rhsGroum);

    var expressionAction = new ExpressionAction(mergedGroum.nodes, IndexGenerator.getIdx());
    if (node.getComparisonType() == ComparisonNode.ComparisonType.greaterThan) {
      expressionAction.operator(ExpressionAction.Operator.greaterThan);
    }
    mergedGroum = mergedGroum.mergeSequential(expressionAction);

    return mergedGroum;
  }

  @Override
  public Groum visit(TermNode node) {
    var lhsGroum = node.getLhs().accept(this);
    var rhsGroum = node.getRhs().accept(this);
    // TODO: save operator? how? subexpression?
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
    Symbol assigneeSymbol;

    Groum lhsGroum = Groum.NONE;
    GroumNode assignmentAction = GroumNode.NONE;
    if (node.getLhs().type == Node.Type.MemberAccess) {
      lhsGroum = node.getLhs().accept(this);

      // the sink node (last node in groum) will be the accessed property
      var sinkNode = lhsGroum.sinkNodes().get(0);
      if (sinkNode instanceof PropertyAccessAction propertyAccessAction) {
        var contextId = propertyAccessAction.referencedInstanceId();
        assigneeSymbol = propertyAccessAction.propertySymbol();
        var assigneeInstanceId = this.memberAccessInstanceMap.get(contextId).get(assigneeSymbol);
        assignmentAction = new DefinitionAction(assigneeSymbol, assigneeInstanceId);
        assignmentAction.relatedAstNode(node);
      }
    } else {
      var id = node.getLhs();
      assigneeSymbol = symbolTable.getSymbolsForAstNode(id).get(0);
      assignmentAction =
          new DefinitionAction(assigneeSymbol, createOrGetInstanceId(assigneeSymbol));
      assignmentAction.relatedAstNode(node);
    }

    Groum assignmentGroum = new Groum(assignmentAction);

    if (node.getLhs().type == Node.Type.MemberAccess) {
      var sourceNode = lhsGroum.sourceNodes().get(0);
      sourceNode.addChildren(assignmentGroum.nodes);
    }

    Groum mergedAssignmentGroum;

    // get rhs
    Groum rhsGroum = node.getRhs().accept(this);
    if (!rhsGroum.equals(Groum.NONE)) {
      // expression action
      ExpressionAction expr = new ExpressionAction(rhsGroum.nodes, IndexGenerator.getIdx());
      expr.relatedAstNode(node.getRhs());
      Groum exprGroum = new Groum(expr);

      var mergedExpressionGroum = rhsGroum.mergeSequential(exprGroum);

      mergedAssignmentGroum = lhsGroum.mergeParallel(mergedExpressionGroum);
      mergedAssignmentGroum = mergedAssignmentGroum.mergeSequential(assignmentGroum);
    } else {
      mergedAssignmentGroum = lhsGroum.mergeSequential(assignmentGroum);
    }

    return mergedAssignmentGroum;
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
    var expression = new ExpressionAction(conditionGroum.nodes, IndexGenerator.getIdx());
    expression.relatedAstNode(node);
    Groum conditionExpressionGroum = new Groum(expression);
    Groum mergedGroum = conditionGroum.mergeSequential(conditionExpressionGroum);

    var controlNode = new ControlNode(ControlNode.ControlType.whileLoop);
    controlNode.relatedAstNode(node);

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
    var iterableRef =
        new VariableReferenceAction(iterableSymbol, createOrGetInstanceId(iterableSymbol));
    iterableRef.relatedAstNode(node.getIterableIdNode());

    mergedGroum = mergedGroum.mergeSequential(iterableRef);

    var varSymbol = this.symbolTable.getSymbolsForAstNode(node.getVarIdNode()).get(0);
    var varDef = new DefinitionAction(varSymbol, createOrGetInstanceId(varSymbol));
    varDef.relatedAstNode(node.getVarIdNode());

    mergedGroum = mergedGroum.mergeSequential(varDef);

    var controlNode = new ControlNode(ControlNode.ControlType.forLoop);
    controlNode.relatedAstNode(node);

    Groum controlGroum = new Groum(controlNode);
    mergedGroum = mergedGroum.mergeSequential(controlGroum);

    // counter variable definition
    var counterSymbol = this.symbolTable.getSymbolsForAstNode(node.getCounterIdNode()).get(0);
    var counterDef = new DefinitionAction(counterSymbol, createOrGetInstanceId(counterSymbol));
    counterDef.relatedAstNode(node.getCounterIdNode());
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

    // TODO: this may well be a member access -> just visit it?
    /*var iterableSymbol = this.symbolTable.getSymbolsForAstNode(node.getIterableIdNode()).get(0);
    var iterableRef =
        new VariableReferenceAction(iterableSymbol, createOrGetInstanceId(iterableSymbol));
    iterableRef.relatedAstNode(node.getIterableIdNode());*/

    var iterableGroum = node.getIterableIdNode().accept(this);
    mergedGroum = mergedGroum.mergeSequential(iterableGroum);

    var varSymbol = this.symbolTable.getSymbolsForAstNode(node.getVarIdNode()).get(0);
    var varDef = new DefinitionAction(varSymbol, createOrGetInstanceId(varSymbol));
    varDef.relatedAstNode(node.getVarIdNode());
    mergedGroum = mergedGroum.mergeSequential(varDef);

    var controlNode = new ControlNode(ControlNode.ControlType.forLoop);
    controlNode.relatedAstNode(node);
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
    ArrayList<Groum> propertyDefinitionGroums =
        new ArrayList<>(node.getPropertyDefinitionNodes().size());
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
    definitionAction.relatedAstNode(node);
    definitionAction.addChildren(mergedPropertyDefinitionGroums.nodes);

    var defGroum = new Groum(definitionAction);

    Groum mergedGroum =
        mergedPropertyDefinitionGroums.mergeSequential(
            defGroum,
            List.of(GroumEdge.GroumEdgeType.EDGE_DATA_READ, GroumEdge.GroumEdgeType.EDGE_TEMPORAL));
    return mergedGroum;
  }

  @Override
  public Groum visit(ImportNode node) {
    var importSymbol = this.symbolTable.getSymbolsForAstNode(node).get(0);
    var defAction = new DefinitionByImportAction(importSymbol, createOrGetInstanceId(importSymbol));
    defAction.relatedAstNode(node);
    return new Groum(defAction);
  }
}
