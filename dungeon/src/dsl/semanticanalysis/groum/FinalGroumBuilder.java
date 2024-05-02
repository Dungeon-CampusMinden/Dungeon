package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.SymbolTable;
import dsl.semanticanalysis.analyzer.TypeInferrer;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.symbol.Symbol;
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
    var fileGlobalScope = new GroumScope(GroumNode.NONE);
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
      var defScope = new GroumScope(this.currentScope(), defNode);
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
                .filter(e -> e.edgeType().equals(GroumEdge.GroumEdgeType.temporal))
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

    return this.groum;
  }

  public GroumScope currentScope() {
    return this.groumScopeStack.peek();
  }

  @Override
  public List<InvolvedVariable> visit(ControlNode node) {
    GroumScope scope;
    switch (node.controlType()) {
      case ifElseStmt:
        // we never expect to visit the same node twice
        if (!this.scopesForNodes.containsKey(node)) {
          // new scope
          scope = new GroumScope(this.currentScope(), node);
          this.scopesForNodes.put(node, scope);

          // get if node
          var ifNode = node.outgoing().get(0).end();
          var ifScope = new GroumScope(scope, ifNode);
          this.scopesForNodes.put(ifNode, ifScope);
          scope.pushConditionalScope(ifScope);

          // get else node
          var elseNode = node.outgoing().get(1).end();
          var elseScope = new GroumScope(scope, elseNode);
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
          scope = new GroumScope(this.currentScope(), node);
          this.scopesForNodes.put(node, scope);
          this.currentScope().pushConditionalScope(scope);
        }
        break;
      case block:
        // we never expect to visit the same node twice
        if (!this.scopesForNodes.containsKey(node)) {
          // new scope
          scope = new GroumScope(this.currentScope(), node);
          this.scopesForNodes.put(node, scope);
        }
        break;
      case returnStmt:
        // TODO
        break;
      default:
        ;
    }

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(ConstRefAction node) {
    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(DefinitionAction node) {

    // all involved variables for the expression will already be calculated
    // does this node reference an expression on incoming edge?
    if (node.incoming().size() == 1) {
      var precedingNode = node.incoming().get(0).start();
      if (precedingNode instanceof ExpressionAction expressionAction) {
        var involvedVariablesInExpression = this.involvedVariables.get(expressionAction);
        if (involvedVariablesInExpression == null) {
          boolean b = true;
        } else {
          involvedVariablesInExpression.forEach(
              v -> {
                this.addInvolvedVariable(node, v, InvolvedVariable.TypeOfInvolvement.read);
                GroumEdge dataEdge =
                    new GroumEdge(
                        v.definitionNode(), node, GroumEdge.GroumEdgeType.dataDependencyRead);
                this.groum.addEdge(dataEdge);
              });
        }
      }
    }

    var existingDefinitions = this.currentScope().getDefinitions(node.referencedInstanceId());
    if (!existingDefinitions.isEmpty()) {
      existingDefinitions.forEach(
          v -> {
            GroumEdge dataEdge =
                new GroumEdge(v, node, GroumEdge.GroumEdgeType.dataDependencyRedefinition);
            this.groum.addEdge(dataEdge);
          });
    }

    // create new definition in scope
    this.currentScope().createNewDefinition(node.referencedInstanceId(), node);

    // add write involved variable
    var instanceSymbol = node.instanceSymbol();
    var involvedVariable =
        this.addInvolvedVariable(
            node, instanceSymbol, InvolvedVariable.TypeOfInvolvement.write, node);
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
      // TODO: always read?
      this.addInvolvedVariable(node, involvedVariable, InvolvedVariable.TypeOfInvolvement.read);
    }

    // Note: we do not explicitly create data dependency edges to the expression, as the expression
    // is just used as an intermediary object

    return returnList;
  }

  @Override
  public List<InvolvedVariable> visit(FunctionCallAction node) {
    // get involved variables for all parameters -> read

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(MethodAccessAction node) {
    // Note: temporally, lhs is evaluated before rhs
    // Note: if this method access is chained after some other member access (on lhs), the lhs
    // member access
    //       will be the parent of this node...

    // read all variables from parameters

    // write & read lhs (can't say for sure without more information about the method (does it
    // mutate the object?)
    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(ParameterInstantiationAction node) {
    // add to definition -> if the enclosing function is called, we somehow need to 'connect'
    //  this node to a pass as param node..

    var instanceId = node.referencedInstanceId();
    this.currentScope().createNewDefinition(instanceId, node);

    var involvedVariable =
        this.addInvolvedVariable(
            node, node.parameterSymbol(), InvolvedVariable.TypeOfInvolvement.write, node);

    return List.of(involvedVariable);
  }

  @Override
  public List<InvolvedVariable> visit(PassAsParameterAction node) {
    // read involved variables from incoming

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(PropertyAccessAction node) {
    // read lhs

    // involve current property

    return new ArrayList<>();
  }

  @Override
  public List<InvolvedVariable> visit(ReferenceInGraphAction node) {
    // read variable

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
              node, definitionSymbol, InvolvedVariable.TypeOfInvolvement.read, definitionNode);

      var dataDependency =
          new GroumEdge(definitionNode, node, GroumEdge.GroumEdgeType.dataDependencyRead);
      this.groum.addEdge(dataDependency);
      involvedVariables.add(involvedVariable);
    }

    return involvedVariables;
  }

  public InvolvedVariable addInvolvedVariable(
      GroumNode node,
      Symbol variableSymbol,
      InvolvedVariable.TypeOfInvolvement type,
      GroumNode definitionNode) {
    if (!this.involvedVariables.containsKey(node)) {
      this.involvedVariables.put(node, new ArrayList<>());
    }
    var involvedVariable = new InvolvedVariable(variableSymbol, type, definitionNode);
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
                  involvedVariable.variableSymbol(), type, involvedVariable.definitionNode()));
    } else {
      this.involvedVariables.get(node).add(involvedVariable);
    }
    return involvedVariable;
  }
}
