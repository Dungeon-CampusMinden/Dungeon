package dsl.semanticanalysis.groum;

import dsl.helper.ProfilingTimer;
import dsl.parser.ast.Node;
import dsl.programmanalyzer.RelationshipRecorder;
import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.analyzer.TypeInferrer;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.groum.node.*;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import java.util.*;

// TODO: how are we going to calculate data dependencies based on the SETS of
//  involved variables (like in the original paper)? if two nodes share an involved
//  variable, the one is data-dependent on the other...how are we going to implement this?
//  .
//  But do we even have to? We distinguish between read and write accesses and the last
//  node, which writes to a variable is registered as it's new latest definition..
public class FinalGroumBuilder implements GroumVisitor<List<InvolvedVariable>> {
  private SymbolTable symbolTable;
  private IEnvironment environment;
  private HashMap<Symbol, Long> instanceMap;
  private HashMap<Long, GroumNode> defNodes;
  private TypeInferrer inferrer;
  private Stack<GroumScope> groumScopeStack = new Stack<>();
  private HashMap<GroumNode, List<InvolvedVariable>> involvedVariables = new HashMap<>();
  private Groum groum;
  private HashSet<GroumNode> processedNodes = new HashSet<>();
  private HashMap<GroumNode, GroumScope> scopesForNodes = new HashMap<>();
  private long processedCounter = 0;

  // TODO: need other data structure to handle this..
  private ArrayDeque<GroumNode> nodesToProcess = new ArrayDeque<>();

  public Groum build(Node ast, SymbolTable symbolTable, IEnvironment env) {
    TemporalGroumBuilder builder = new TemporalGroumBuilder();
    HashMap<Symbol, Long> instanceMap = new HashMap<>();
    var temporalGroum = builder.walk(ast, symbolTable, env, instanceMap);
    var finalizedGroum = finalize(temporalGroum, instanceMap);
    RelationshipRecorder.instance.addRelatable(finalizedGroum);
    return finalizedGroum;
  }

  public Groum finalize(Groum groum, HashMap<Symbol, Long> instanceMap) {
    // TODO: do we really need the instance map here? we won't define new actions
    //  and the groumNodes themselves contain the instance id's of the referenced symbols
    this.instanceMap = instanceMap;
    this.groum = groum;

    // data dependency rules:
    // 1. For an action node, its corresponding variable is considered an involved variable
    // 2. For a control node, all variables processed in the corresponding control strucutre
    //    are regarded as involved ones.
    // 3. For an action node representing a field assignment such as o.f = E, all variables
    //    processed in the evaluation of E are considered involved variables.
    // 4. For an action node representing an invocation of a method, all variables involved
    //    in the evaluation of the parameters of the method are considered as involved variables.
    // 5. If an invocation is used for an assignment, e.g. C x = new C() or x = o.m(), the assigned
    //    variable x is also involved.

    // three passes:
    // 1. iterate over all nodes to find definition nodes for instances (todo: maybe this should be
    // restricted
    //    to global definitions)
    // 2. calculate shared variables for each node
    //    (nodes, which are children of an expression node are used to model specific variable
    // references,
    //    and don't store 'shared variables', only the expression stores them...i guess)
    // 3. add data dependency edges (maybe able to do this in second pass)

    // first pass
    var fileGlobalScope = new GroumScope(groum, GroumNode.NONE);
    this.groumScopeStack.add(fileGlobalScope);

    this.defNodes = new HashMap<>();
    for (var node : groum.nodes) {
      if (node instanceof ActionNode actionNode) {
        if (actionNode.actionType().equals(ActionNode.ActionType.definition)
            || actionNode.actionType().equals(ActionNode.ActionType.definitionByImport)) {
          // is it a file-global definition?
          // TODO: once, the file scope is represented in Groums, we need a new way to determine,
          //  if a definition is file-global
          if (actionNode.parent() == GroumNode.NONE) {
            this.defNodes.put(actionNode.referencedInstanceId(), actionNode);
            // TODO: FILESCOPE
            this.currentScope().createNewDefinition(actionNode.referencedInstanceId(), actionNode);
          }
        }
      }
    }

    // second pass
    // TODO: how to maintain information, which variables are currently defined by which node?
    //  this is especially relevant for redefinition of variables, because from the redefinition
    //  onwards, the node to reference in the data dependency should be the redefinition node, not
    //  the original definition node
    // TODO: for each variable, we need a list of groumNodes for which the variable is involved
    //  (after it's latest (re)definition)

    // for each global action node:
    // calculate source nodes
    // iteratively:
    // - update currently in scope variable definitions
    // - use outgoing temporal edges to calculate next nodes to visit
    // - if we encounter a control node, which has children, push a new 'scope'

    // process all global definitions
    processedNodes.clear();
    processedCounter = 0;
    for (var defNode : this.defNodes.values()) {
      // create new scope
      GroumScope defScope;
      if (defNode instanceof DefinitionAction definitionAction
          && definitionAction.instanceSymbol().getSymbolType().equals(Symbol.SymbolType.Callable)) {
        // function defintion
        // var beginFuncControlNode =
        // definitionAction.getEndsOfOutgoing(GroumEdge.GroumEdgeType.EDGE_TEMPORAL).get(0);
        // defScope = new GroumScope(this.groum, this.currentScope(), beginFuncControlNode);
        var beginFuncNode = defNode.children().get(0);
        defScope = new GroumScope(this.groum, this.currentScope(), beginFuncNode);
      } else {
        defScope = new GroumScope(this.groum, this.currentScope(), defNode);
      }
      this.groumScopeStack.push(defScope);

      // get subgroums source nodes
      var defNodesSourceNodes =
          defNode.children().stream().filter(c -> c.incoming().size() == 0).toList();
      nodesToProcess.addAll(defNodesSourceNodes);

      while (!nodesToProcess.isEmpty()) {
        GroumNode currentNode = nodesToProcess.pop();
        if (processedNodes.contains(currentNode)) {
          // skip already processed node
          continue;
        }

        // ensure, that all preceding nodes are processed before the expression itself is processed
        // TODO: this is kind of ugly, would be nice, if we pushed the children on the stack in a
        // way, where
        //  this is always the case!
        if (currentNode instanceof ExpressionAction || currentNode instanceof MethodAccessAction) {
          var precedents = currentNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
          boolean allPrecedentsProcesseed = true;
          for (var precedent : precedents) {
            allPrecedentsProcesseed = this.processedNodes.contains(precedent);
            if (!allPrecedentsProcesseed) {
              break;
            }
          }

          // TODO: wouldn't it be better to just add the precedent nodes? not the children?
          if (!allPrecedentsProcesseed) {
            // If the current node to process has children, add them first!
            var currentNodeChildren = currentNode.children();

            this.nodesToProcess.addFirst(currentNode);
            currentNodeChildren.reversed().forEach(nodesToProcess::addFirst);
            continue;
          }
        }

        if (currentNode instanceof DefinitionAction
            || currentNode instanceof PropertyAccessAction) {
          var precedents = currentNode.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
          boolean allPrecedentsProcesseed = true;
          for (var precedent : precedents) {
            allPrecedentsProcesseed = this.processedNodes.contains(precedent);
            if (!allPrecedentsProcesseed) {
              break;
            }
          }

          if (!allPrecedentsProcesseed) {
            this.nodesToProcess.addFirst(currentNode);
            precedents.reversed().forEach(nodesToProcess::addFirst);
            continue;
          }
        }

        // push correct scope
        boolean pushedScope = false;
        var parent = currentNode.parent();

        // get next upper parent, which is in controlScopes
        while (parent != GroumNode.NONE && !this.scopesForNodes.containsKey(parent)) {
          parent = parent.parent();
        }

        // get parents groum scope
        if (parent instanceof ControlNode controlNode
            && controlNode.controlType() != ControlNode.ControlType.returnStmt) {
          if (controlNode.controlType() != ControlNode.ControlType.returnStmt) {
            if (this.scopesForNodes.containsKey(parent)) {
              this.groumScopeStack.push(this.scopesForNodes.get(parent));
              pushedScope = true;
            }
          }
        }

        // visit node
        currentNode.accept(this);

        // TODO: what if controlFlowParentNode is NULL?
        // set control flow parent of node
        var controlFlowParentNode = getCurrentControlParentNode();
        if (controlFlowParentNode != GroumNode.NONE) {
          // new edge
          /*var controlFlowParentEdge = new GroumEdge(controlFlowParentNode, currentNode, GroumEdge.GroumEdgeType.EDGE_CONTROL_PARENT);
          this.groum.addEdge(controlFlowParentEdge);*/
          currentNode.controlFlowParent(controlFlowParentNode);
        }

        // set processed counter idx
        this.processedCounter++;
        currentNode.setProcessedCounter(this.processedCounter);

        // add all following nodes to nodestoprocess
        if (!currentNode.children().isEmpty()) {
          // If the current node to process has children, add them first!
          var currentNodeChildren = currentNode.children();
          currentNodeChildren.reversed().forEach(nodesToProcess::addFirst);
        }

        // add following children
        nodesToProcess.addAll(
            currentNode.outgoing().stream()
                .filter(e -> e.edgeType().equals(GroumEdge.GroumEdgeType.EDGE_TEMPORAL))
                .map(GroumEdge::end)
                .toList());

        // mark node as processed
        processedNodes.add(currentNode);

        // restore scope stack
        if (pushedScope) {
          this.groumScopeStack.pop();
        }
      }
      this.groumScopeStack.pop();
    }

    // propagate the references through definition nodes for all global definitions, which are not
    // function definitions
    var globalNonFunctionDefinitions =
        defNodes.values().stream()
            .filter(n -> n instanceof DefinitionAction)
            .map(n -> (DefinitionAction) n)
            .filter(n -> !(n.instancedType() instanceof FunctionType))
            .toList();
    for (var definition : globalNonFunctionDefinitions) {
      var childrenSet = new HashSet<>(definition.children());

      // recursive: get the involved variables of preceding nodes until we reach the boundaries of
      // the definition /
      // challenge: how to find the boundary of the definition? -> has the node involved variables
      // or does it lay outside
      // of the children set.. if it lays outside the children set, we break
      // if it has involved variables, than an evaluation has already taken place and we can resume
      // from that
      // point on..
      calculateInvolvedVariablesBottomUp(definition, childrenSet);
    }

    HashMap<String, Long> times = new HashMap<>();
    try (var t = new ProfilingTimer("remove redundancy", times, ProfilingTimer.Unit.micro)) {
      this.groum.removeRedundantEdges();
    }

    return this.groum;
  }

  private GroumNode getCurrentControlParentNode() {
    var currentScopeNode = this.currentScope().associatedGroumNode();
    if (currentScopeNode instanceof ControlNode controlNode
        && !controlNode.controlType().equals(ControlNode.ControlType.returnStmt)
        && !controlNode.controlType().equals(ControlNode.ControlType.block)) {
      return currentScopeNode;
    } else {
      return this.currentScope().controlFlowParentNode();
    }
  }

  private List<InvolvedVariable> calculateInvolvedVariablesBottomUp(
      GroumNode node, HashSet<GroumNode> childrenInScope) {
    // get preceding nodes
    List<InvolvedVariable> nodesInvolvedVariables = new ArrayList<>();
    var precedingNodes = node.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_DATA_READ);
    for (var precedingNode : precedingNodes) {
      if (!childrenInScope.contains(precedingNode)) {
        continue;
      }

      if (this.involvedVariables.containsKey(precedingNode)) {
        // collect involved variables
        var precdingInvolvedVariables = this.involvedVariables.get(precedingNode);
        var precedingInvolvedVariablesOtherThanNodes =
            precdingInvolvedVariables.stream()
                .filter(v -> !v.definitionNode().equals(precedingNode))
                .toList();
        if (!precedingInvolvedVariablesOtherThanNodes.isEmpty()) {
          nodesInvolvedVariables.addAll(precdingInvolvedVariables);
          continue;
        }
      }
      var precedingInvolvedVariables =
          calculateInvolvedVariablesBottomUp(precedingNode, childrenInScope);
      nodesInvolvedVariables.addAll(precedingInvolvedVariables);
    }

    nodesInvolvedVariables.forEach(
        v -> {
          this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.read);
          var readEdge =
              new GroumEdge(v.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
          this.groum.addEdge(readEdge);
        });

    return nodesInvolvedVariables;
  }

  public GroumScope currentScope() {
    return this.groumScopeStack.peek();
  }

  @Override
  public List<InvolvedVariable> visit(ControlNode node) {
    GroumScope scope;
    var incomingNodes = node.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
    switch (node.controlType()) {
      case ifElseStmt:
        // we never expect to visit the same node twice
        if (!this.scopesForNodes.containsKey(node)) {
          // new scope
          scope = new GroumScope(this.groum, this.currentScope(), node);
          this.scopesForNodes.put(node, scope);

          // get if node
          var ifNode = node.outgoing().get(0).end();
          var ifScope = new GroumScope(this.groum, scope, ifNode);
          this.scopesForNodes.put(ifNode, ifScope);
          scope.pushConditionalScope(ifScope);

          // get else node
          var elseNode = node.outgoing().get(1).end();
          var elseScope = new GroumScope(this.groum, scope, elseNode);
          this.scopesForNodes.put(elseNode, elseScope);
          scope.pushConditionalScope(elseScope);

          // add conditional branch to current scope
          this.currentScope().pushConditionalScope(scope);
        }
        break;
      case whileLoop:
      case forLoop:
      case countingForLoop:
      case ifStmt:
      case elseStmt:
        if (!this.scopesForNodes.containsKey(node)) {
          // new scope
          scope = new GroumScope(this.groum, this.currentScope(), node);
          this.scopesForNodes.put(node, scope);
          this.currentScope().pushConditionalScope(scope);
        }

        // TODO: test this
        for (var incomingNode : incomingNodes) {
          var nodesInvolvedVariables =
              this.involvedVariables.getOrDefault(incomingNode, new ArrayList<>());
          nodesInvolvedVariables.forEach(
              n -> {
                this.addInvolvedVariable(node, n, n.typeOfInvolvement());
                var readEdge =
                    new GroumEdge(n.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
                this.groum.addEdge(readEdge);
              });
        }

        break;
      case block:
        // we never expect to visit the same node twice
        if (!this.scopesForNodes.containsKey(node)) {
          // new scope
          scope = new GroumScope(this.groum, this.currentScope(), node);
          this.scopesForNodes.put(node, scope);
        }

        break;
      case returnStmt:

        // read involved variables from incoming
        // var involvedVariables = new ArrayList<InvolvedVariable>();
        for (var incomingNode : incomingNodes) {
          var nodesInvolvedVariables =
              this.involvedVariables.getOrDefault(incomingNode, new ArrayList<>());
          nodesInvolvedVariables.forEach(
              n -> {
                this.addInvolvedVariable(node, n, n.typeOfInvolvement());
                var readEdge =
                    new GroumEdge(n.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
                this.groum.addEdge(readEdge);
              });
        }
        break;
      default:
        ;
    }

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(ConstRefAction node) {
    this.addInvolvedVariable(
        node, node.referencedInstanceId(), InvolvedVariable.TypeOfInvolvement.read, node);
    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(DefinitionAction node) {
    var precedingNodes = node.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
    HashSet<GroumNode> connectedNodes = new HashSet<>();
    for (var precedingNode : precedingNodes) {
      if (precedingNode instanceof MethodAccessAction
          || precedingNode instanceof PropertyAccessAction
          || precedingNode instanceof ExpressionAction
          || precedingNode instanceof ConstRefAction) {
        var involvedVariablesInExpression = this.involvedVariables.get(precedingNode);

        if (precedingNode instanceof PropertyAccessAction propertyAccessAction) {
          involvedVariablesInExpression =
              involvedVariablesInExpression.stream()
                  .filter(v -> v.variableInstanceId() != node.referencedInstanceId())
                  .toList();
        }

        if (involvedVariablesInExpression != null) {
          involvedVariablesInExpression.forEach(
              v -> {
                this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.read);
                GroumNode defNode = v.definitionNode();
                if (!connectedNodes.contains(defNode)) {
                  GroumEdge dataEdge =
                      new GroumEdge(
                          v.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
                  this.groum.addEdge(dataEdge);
                  connectedNodes.add(defNode);
                }
              });
        }

        if (precedingNode instanceof PropertyAccessAction propertyAccessAction) {
          this.addInvolvedVariable(
              node,
              propertyAccessAction.propertyInstanceId,
              InvolvedVariable.TypeOfInvolvement.write,
              node);
          GroumEdge dataEdge =
              new GroumEdge(propertyAccessAction, node, GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
          this.groum.addEdge(dataEdge);
        }
      }
    }

    var existingDefinitions = this.currentScope().getDefinitions(node.referencedInstanceId());
    if (!existingDefinitions.isEmpty()) {
      existingDefinitions.forEach(
          v -> {
            if (v != node) {
              GroumEdge dataEdge = new GroumEdge(v, node, GroumEdge.GroumEdgeType.EDGE_DATA_WRITE);
              this.groum.addEdge(dataEdge);
            }
          });
    }

    // create new definition in scope
    this.currentScope().createNewDefinition(node.referencedInstanceId(), node);

    // store the information, that the definition is linked to another instance
    // so that we can invalidate the definition, if the instance itself is redefined
    GroumNode nodeParent = node.parent();
    if (nodeParent instanceof PropertyAccessAction propertyAccessAction) {
      GroumNode propertyAccess =
          node.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL).get(0);

      while (propertyAccess instanceof PropertyAccessAction previousPropertyAccess) {
        this.currentScope()
            .registerInstanceDefinition(
                previousPropertyAccess.propertyInstanceId,
                previousPropertyAccess.referencedInstanceId());
        propertyAccess =
            previousPropertyAccess
                .getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL)
                .get(0);
      }
    }

    // add write involved variable
    long instanceId = node.referencedInstanceId();
    var involvedVariable =
        this.addInvolvedVariable(node, instanceId, InvolvedVariable.TypeOfInvolvement.write, node);
    return List.of(involvedVariable);
  }

  @Override
  public List<InvolvedVariable> visit(DefinitionByImportAction node) {
    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(ExpressionAction node) {
    // get all involved variables from incoming edges
    List<GroumNode> expressionTerms = node.incoming().stream().map(GroumEdge::start).toList();

    // concatenate involved variables of all terms
    List<InvolvedVariable> expressionInvolvedVariables = new ArrayList<>();
    for (var term : expressionTerms) {
      if (this.involvedVariables.containsKey(term)) {
        expressionInvolvedVariables.addAll(this.involvedVariables.get(term));
      }
    }

    // set them as involved variables for the expression for convenience
    List<InvolvedVariable> returnList = new ArrayList<>(expressionInvolvedVariables.size());
    for (var involvedVariable : expressionInvolvedVariables) {
      this.addInvolvedVariable(node, involvedVariable, involvedVariable.typeOfInvolvement());
      var dataEdge =
          new GroumEdge(
              involvedVariable.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
      this.groum.addEdge(dataEdge);
    }

    return returnList;
  }

  @Override
  public List<InvolvedVariable> visit(FunctionCallAction node) {
    // get involved variables for all parameters -> read

    var passAsParamNodes = node.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
    for (var paramNode : passAsParamNodes) {
      var paramNodesInvolvedVariables = this.involvedVariables.get(paramNode);
      if (paramNodesInvolvedVariables != null) {
        paramNodesInvolvedVariables.forEach(
            n -> this.addInvolvedVariable(node, n, n.typeOfInvolvement()));
      }
    }

    // get all definitions
    var involvedVariables = this.involvedVariables.get(node);
    if (involvedVariables != null) {
      for (var involvedVariable : involvedVariables) {
        long instanceId = involvedVariable.variableInstanceId();
        var definitions = this.currentScope().getDefinitions(instanceId);
        // add read dependency
        for (var definition : definitions) {
          var edge = new GroumEdge(definition, node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
          this.groum.addEdge(edge);
        }
      }
    }

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(ParameterInstantiationAction node) {
    // add to definition -> if the enclosing function is called, we somehow need to 'connect'
    //  this node to a pass as param node..

    var instanceId = node.referencedInstanceId();
    this.currentScope().createNewDefinition(instanceId, node);

    var involvedVariable =
        this.addInvolvedVariable(node, instanceId, InvolvedVariable.TypeOfInvolvement.write, node);

    return List.of(involvedVariable);
  }

  @Override
  public List<InvolvedVariable> visit(PassAsParameterAction node) {
    // read involved variables from incoming
    var incomingNodes = node.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL);
    // var involvedVariables = new ArrayList<InvolvedVariable>();
    for (var incomingNode : incomingNodes) {
      var nodesInvolvedVariables =
          this.involvedVariables.getOrDefault(incomingNode, new ArrayList<>());
      nodesInvolvedVariables.forEach(
          n -> {
            this.addInvolvedVariable(node, n, n.typeOfInvolvement());
            var readEdge =
                new GroumEdge(n.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
            this.groum.addEdge(readEdge);
          });
    }

    return this.involvedVariables.get(node);
  }

  @Override
  public List<InvolvedVariable> visit(MethodAccessAction node) {
    // TODO
    // Note: if this method access is chained after some other member access (on lhs), the lhs
    // member access
    //       will be the parent of this node...

    // write & read lhs (can't say for sure without more information about the method (does it
    // mutate the object?)

    // read all variables from parameters, which are all temporally before
    var parameterNodes =
        node.getStartsOfIncoming(GroumEdge.GroumEdgeType.EDGE_TEMPORAL).stream()
            .filter(n -> n instanceof PassAsParameterAction)
            .map(n -> (PassAsParameterAction) n)
            .toList();
    List<InvolvedVariable> involvedParameterVariables = new ArrayList<>();

    parameterNodes.forEach(
        n -> {
          var involvedVars = this.involvedVariables.getOrDefault(n, new ArrayList<>());
          involvedParameterVariables.addAll(involvedVars);
        });

    involvedParameterVariables.forEach(
        v -> {
          var definitionsForParameterVariables =
              this.currentScope().getDefinitions(v.variableInstanceId());

          definitionsForParameterVariables.forEach(
              d -> {
                var readEdge = new GroumEdge(d, node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
                this.groum.addEdge(readEdge);
              });
        });

    involvedParameterVariables.forEach(
        v -> {
          this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.read);
        });

    List<InvolvedVariable> parentsInvolvedVariables = new ArrayList<>();
    if (node.parent() instanceof MethodAccessAction parentNode) {
      // TODO: handle; should be easy
      parentsInvolvedVariables = this.involvedVariables.get(parentNode);

      parentsInvolvedVariables.forEach(
          v -> {
            this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.readWrite);
            var edge =
                new GroumEdge(v.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
            this.groum.addEdge(edge);
          });

    } else if (node.parent() instanceof PropertyAccessAction parentNode) {
      // get parents involved variables
      parentsInvolvedVariables = this.involvedVariables.get(parentNode);

      // should the direct parent variables also be read?
      // it would be completely correct to include the redefinition action as an involved variable
      // here..
      var directParentVariables =
          parentsInvolvedVariables.stream()
              .filter(n -> n.variableInstanceId().equals(parentNode.propertyInstanceId))
              .toList();
      directParentVariables.forEach(
          v -> {
            var edge =
                new GroumEdge(v.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
            this.groum.addEdge(edge);
          });

      var otherInvolvedVariables =
          parentsInvolvedVariables.stream()
              .filter(n -> !n.variableInstanceId().equals(parentNode.propertyInstanceId))
              .toList();
      otherInvolvedVariables.forEach(
          v -> {
            this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.read);
            var edge =
                new GroumEdge(v.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
            this.groum.addEdge(edge);
          });
    } else {
      // read lhs
      var lhsInstanceId = node.referencedInstanceId();
      var definitions = this.currentScope().getDefinitions(lhsInstanceId);

      // get involved variables for definitions
      definitions.forEach(
          d -> {
            this.addInvolvedVariable(
                node, node.referencedInstanceId(), InvolvedVariable.TypeOfInvolvement.read, d);
            var edge = new GroumEdge(d, node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
            this.groum.addEdge(edge);
          });
    }

    // add redefined instance as involved variable
    this.addInvolvedVariable(
        node,
        node.instanceRedefinitionNode().referencedInstanceId(),
        InvolvedVariable.TypeOfInvolvement.write,
        node.instanceRedefinitionNode());

    // add method return value as involved variable
    this.addInvolvedVariable(
        node, node.methodCallInstanceId(), InvolvedVariable.TypeOfInvolvement.write, node);

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(PropertyAccessAction node) {
    List<InvolvedVariable> parentsInvolvedVariables = new ArrayList<>();
    if (node.parent() instanceof MethodAccessAction parentNode) {
      // TODO: handle; should be easy
      parentsInvolvedVariables = this.involvedVariables.get(parentNode);
      if (parentsInvolvedVariables == null) {
        boolean b = true;
      } else {
        parentsInvolvedVariables.forEach(
            v -> {
              this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.readWrite);
              var edge =
                  new GroumEdge(v.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
              this.groum.addEdge(edge);
            });
      }

    } else if (node.parent() instanceof PropertyAccessAction parentNode) {
      // get parents involved variables
      parentsInvolvedVariables = this.involvedVariables.get(parentNode);

      // TODO: do we really want all the involved variables of the definition of the parent here?
      //  or just the direct parent?

      parentsInvolvedVariables.forEach(
          v -> {
            this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.readWrite);
            var edge =
                new GroumEdge(v.definitionNode(), node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
            this.groum.addEdge(edge);
          });
    } else {
      // read lhs
      var lhsInstanceId = node.referencedInstanceId();
      var definitions = this.currentScope().getDefinitions(lhsInstanceId);
      // get involved variables for definitions
      definitions.forEach(
          d -> {
            this.addInvolvedVariable(
                node, node.referencedInstanceId(), InvolvedVariable.TypeOfInvolvement.read, d);
            var edge = new GroumEdge(d, node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
            this.groum.addEdge(edge);
          });
    }

    // search for existing definitions of this property
    var propertyDefinitions = this.currentScope().getDefinitions(node.propertyInstanceId);

    // read definitions
    propertyDefinitions.forEach(
        d -> {
          // this.addInvolvedVariable(node, node.referencedInstanceId(),
          // InvolvedVariable.TypeOfInvolvement.read, d);
          this.addInvolvedVariable(
              node, node.propertyInstanceId, InvolvedVariable.TypeOfInvolvement.read, d);
          var edge = new GroumEdge(d, node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
          this.groum.addEdge(edge);
        });

    // this only adds the property as an involved variable, not the lhs
    this.addInvolvedVariable(
        node, node.propertyInstanceId, InvolvedVariable.TypeOfInvolvement.read, node);

    // for testing
    var involvedVariables = this.involvedVariables.get(node);
    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(ReferenceInGraphAction node) {
    // TODO
    // read variable

    long instanceId = node.referencedInstanceId();
    var definitons = this.currentScope().getDefinitions(instanceId);
    definitons.forEach(
        d -> {
          this.addInvolvedVariable(node, instanceId, InvolvedVariable.TypeOfInvolvement.read, d);
          var dataEdge = new GroumEdge(d, node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
          this.groum.addEdge(dataEdge);
        });

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(VariableReferenceAction node) {
    // add read involved variable

    // get instance id
    var instanceId = node.referencedInstanceId();

    var definitionSymbol = this.currentScope().getDefinitionSymbol(instanceId);

    // search instanceId in current definitions
    var definitionNodes = this.currentScope().getDefinitions(instanceId);

    ArrayList<InvolvedVariable> involvedVariables = new ArrayList<>();
    for (var definitionNode : definitionNodes) {
      var involvedVariable =
          this.addInvolvedVariable(
              node, instanceId, InvolvedVariable.TypeOfInvolvement.read, definitionNode);

      var dataDependency =
          new GroumEdge(definitionNode, node, GroumEdge.GroumEdgeType.EDGE_DATA_READ);
      this.groum.addEdge(dataDependency);
      involvedVariables.add(involvedVariable);
    }

    return involvedVariables;
  }

  public InvolvedVariable addInvolvedVariable(
      GroumNode node,
      Long variableInstanceId,
      InvolvedVariable.TypeOfInvolvement type,
      GroumNode definitionNode) {
    if (!this.involvedVariables.containsKey(node)) {
      this.involvedVariables.put(node, new ArrayList<>());
    }
    var involvedVariable = new InvolvedVariable(variableInstanceId, type, definitionNode);
    this.involvedVariables.get(node).add(involvedVariable);
    return involvedVariable;
  }

  public InvolvedVariable addInvolvedVariable(
      GroumNode node, InvolvedVariable involvedVariable, InvolvedVariable.TypeOfInvolvement type) {
    if (!this.involvedVariables.containsKey(node)) {
      this.involvedVariables.put(node, new ArrayList<>());
    }
    if (!involvedVariable.typeOfInvolvement().equals(type)) {
      this.involvedVariables
          .get(node)
          .add(
              new InvolvedVariable(
                  involvedVariable.variableInstanceId(), type, involvedVariable.definitionNode()));
    } else {
      this.involvedVariables.get(node).add(involvedVariable);
    }
    return involvedVariable;
  }
}
