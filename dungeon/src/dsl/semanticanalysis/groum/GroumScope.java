package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;

import java.util.*;

public class GroumScope {
  public static GroumScope NONE = new GroumScope(Groum.NONE, GroumNode.NONE);
  private HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions = new HashMap<>();
  private Stack<GroumNode> conditionalScopesOrdered = new Stack<>();
  private HashMap<GroumNode, GroumScope> conditionalScopes = new HashMap<>();
  private HashMap<Long, HashSet<Long>> instancedDefinitions = new HashMap<>();
  private GroumScope parent;
  private GroumScope controlFlowParent;
  private HashSet<GroumScope> children = new HashSet<>();
  private HashSet<GroumScope> heritage = new HashSet<>();
  private GroumNode associatedGroumNode;
  private final Groum groum;

  public GroumScope(Groum groum, GroumNode associatedGroumNode) {
    this.groum = groum;
    this.parent = NONE;
    this.controlFlowParent = NONE;
    this.associatedGroumNode = associatedGroumNode;
  }

  public GroumScope(Groum groum, GroumScope parent, GroumNode associatedGroumNode) {
    this.groum = groum;
    this.parent = parent;
    this.parent.addChild(this);
    this.controlFlowParent = getConditionalParentScope(parent, -1);
    this.associatedGroumNode = associatedGroumNode;
  }

  public void pushConditionalScope(GroumScope conditionalScope) {
    this.conditionalScopes.put(conditionalScope.associatedGroumNode, conditionalScope);
    this.conditionalScopesOrdered.push(conditionalScope.associatedGroumNode);
  }

  public void addChild(GroumScope scope) {
    this.children.add(scope);
    this.parent.recalculateHeritage();
  }

  public HashSet<GroumScope> children() {
    return this.children;
  }

  private void recalculateHeritage() {
    this.heritage = new HashSet<>(this.children);
    for (var child : this.children) {
      this.heritage.addAll(child.heritage);
    }
    if (this.parent != NONE && this.parent != null) {
      this.parent.recalculateHeritage();
    }
  }

  public HashSet<GroumScope> heritage() {
    return heritage;
  }

  public GroumScope getConditionalScopeFor(GroumNode node) {
    return this.conditionalScopes.getOrDefault(node, GroumScope.NONE);
  }

  public HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions() {
    return this.variableDefinitions;
  }

  public GroumNode associatedGroumNode() {
    return this.associatedGroumNode;
  }

  public List<GroumNode> getDefinitions(Long instanceId) {
    return this.getDefinitions(instanceId, this);
  }

  public List<GroumNode> getDefinitions(Long instanceId, GroumScope fromScope) {
    if (this.variableDefinitions.containsKey(instanceId)) {
      // - check, if the fromScope is child of an ifElseStmt
      // - if so, add the scope of the other branch to the blocked scopes
      // - repeat that until we reach THIS scope or conditional parent is NONE

      var instanceDefinitions = this.variableDefinitions.get(instanceId);

      GroumScope scopeToCheck = fromScope;
      HashSet<GroumScope> scopesToBlock = new HashSet<>();
      while (scopeToCheck != GroumScope.NONE) {
        var immediateControlFlowParent = scopeToCheck.controlFlowParent;
        if (immediateControlFlowParent == GroumScope.NONE) {
          break;
        }

        var assocNodeControlFlowParent = immediateControlFlowParent.associatedGroumNode();
        var parentOfControlFlowParent = immediateControlFlowParent.controlFlowParent;
        if (parentOfControlFlowParent == GroumScope.NONE) {
          break;
        }

        var assocNodeParentOfControlFlowParent = parentOfControlFlowParent.associatedGroumNode();
        if (!(assocNodeControlFlowParent instanceof ControlNode parentControlNode
            && assocNodeParentOfControlFlowParent
                instanceof ControlNode parentsParentControlNode)) {
          break;
        }

        GroumScope scopeToBlock = GroumScope.NONE;
        if (parentControlNode.controlType().equals(ControlNode.ControlType.ifStmt)
            && parentsParentControlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
          // get else control node
          var elseStmtNode =
              assocNodeParentOfControlFlowParent
                  .getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal)
                  .get(1);
          assert elseStmtNode instanceof ControlNode;
          assert ((ControlNode) elseStmtNode)
              .controlType()
              .equals(ControlNode.ControlType.elseStmt);

          // block the other scope
          scopeToBlock = parentOfControlFlowParent.getConditionalScopeFor(elseStmtNode);
          if (scopeToBlock != GroumScope.NONE) {
            scopesToBlock.add(scopeToBlock);
          }
        } else if (parentControlNode.controlType().equals(ControlNode.ControlType.elseStmt)
            && parentsParentControlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
          // get if control node
          var ifStmtNode =
              assocNodeParentOfControlFlowParent
                  .getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal)
                  .get(0);
          assert ifStmtNode instanceof ControlNode;
          assert ((ControlNode) ifStmtNode).controlType().equals(ControlNode.ControlType.ifStmt);

          // block the other scope
          scopeToBlock = parentOfControlFlowParent.getConditionalScopeFor(ifStmtNode);
          if (scopeToBlock != GroumScope.NONE) {
            scopesToBlock.add(scopeToBlock);
          }
        }

        // add all children scopes of the scope to block
        if (scopeToBlock != GroumScope.NONE) {
          scopesToBlock.addAll(scopeToBlock.heritage);
        }

        // cont
        scopeToCheck = parentOfControlFlowParent;
      }
      final var finalScopesToBlock = scopesToBlock;
      return instanceDefinitions.entrySet().stream()
          .filter(e -> !finalScopesToBlock.contains(e.getKey()))
          .map(Map.Entry::getValue)
          .toList();
    } else {
      if (this.parent != NONE) {
        return this.parent.getDefinitions(instanceId, fromScope);
      } else {
        return new ArrayList<>();
      }
    }
  }

  public Symbol getDefinitionSymbol(Long instanceId) {
    var list = this.getDefinitions(instanceId);
    if (list.isEmpty()) {
      return Symbol.NULL;
    } else {
      GroumNode definitionNode = list.get(0);
      return definitionNode.getDefinitionSymbol();
    }
  }

  // this is just called from propagating a definition to parents
  private void addDefinition(Long instanceId, GroumNode node, GroumScope parentScope) {
    if (!this.variableDefinitions.containsKey(instanceId)) {
      this.variableDefinitions.put(instanceId, new HashMap<>());
    }

    var scopeToNodeMap = this.variableDefinitions.get(instanceId);
    var controlFlowParent = parentScope.controlFlowParent;

    if (this.controlFlowParent == controlFlowParent) {
      // The definition lies in the same control path as the current scope and will overwrite the
      // definition!
      // Therefore, we need to clear all variable definitions and just add the new one.
      scopeToNodeMap.clear();
      scopeToNodeMap.put(controlFlowParent, node);

      checkInstancedDefinitionsOverwriting(instanceId, List.of(node));
    } else if (controlFlowParent == GroumScope.NONE) {
      scopeToNodeMap.put(parentScope, node);
      // TODO: ??? -> seems about right, not to do it...
      //checkInstancedDefinitionsOverwriting(instanceId, parentScope);
    } else {
      scopeToNodeMap.put(controlFlowParent, node);
      // TODO: ??? -> seems about right, not to do it...
      //checkInstancedDefinitionsOverwriting(instanceId, controlFlowParent);
    }

    var childScopesOfDefinitionScope = controlFlowParent.heritage;
    for (var scope : childScopesOfDefinitionScope) {
      scopeToNodeMap.remove(scope);
    }
  }

  // returns: overwrittenDefinitions
  private void checkInstancedDefinitionsOverwriting(Long instanceId, List<GroumNode> definitionNodes) {
    if (this.instancedDefinitions.containsKey(instanceId)) {
      var dependentInstances = this.instancedDefinitions.get(instanceId);
      for (var dependentInstance : dependentInstances) {
        // the dependentInstance may be registered in instancedDefinitions even though no definition is stored in this scope
        // example: in the statement `ent.task_content_component.content = x;`
        // the `content` property is defined directly (and this definition will be stored in `variableDefinitions`)
        // the `task_content_component` is not directly defined but registered in `dependentInstances`
        // nonetheless, in order to 'connect' both the `task_content_component` and `content` properties with
        // the `ent` definition; this is needed for the case, when `ent.task_content_component` is assigned a new
        // value -> we need to invalidate the definition to `content`, even though `ent` is not redefined;
        // if only the `ent`-definition would be registered as a key to `instancedDefinitions`, this connection
        // could not be made!
        if (this.variableDefinitions.containsKey(dependentInstance)) {
          var instanceDefinitions = this.variableDefinitions.get(dependentInstance);
          for (var oldDefinitionNode : instanceDefinitions.values()) {
            definitionNodes.forEach(d -> {
              var redefEdges = new GroumEdge(oldDefinitionNode, d, GroumEdge.GroumEdgeType.dataDependencyRedefinition);
              this.groum.addEdge(redefEdges);
            });
          }
          instanceDefinitions.clear();
        }
        checkInstancedDefinitionsOverwriting(dependentInstance, definitionNodes);
      }
      this.instancedDefinitions.remove(instanceId);
    }
  }

  private GroumScope getConditionalParentScope(GroumScope scope, int maxSearchDepth) {
    var parentScope = scope;
    int searchDepth = 0;
    while ((maxSearchDepth == -1 || searchDepth < maxSearchDepth)
        && parentScope != GroumScope.NONE) {
      if (parentScope.associatedGroumNode instanceof ControlNode controlNode
          && (controlNode.controlType() != ControlNode.ControlType.block
              && controlNode.controlType() != ControlNode.ControlType.returnStmt)) {
        break;
      }
      parentScope = parentScope.parent;
      searchDepth++;
    }
    return parentScope;
  }

  protected void propagateDefinitionToParents(
      Long instanceId, GroumNode node, GroumScope parentScope) {
    if (this == NONE || this.parent == NONE) {
      return;
    }

    var parentsList = this.parent.getDefinitions(instanceId);
    if (!parentsList.isEmpty()) {
      this.parent.addDefinitionFromPropagation(instanceId, node, parentScope);
    }
  }

  protected void addDefinitionFromPropagation(
      Long instanceId, GroumNode node, GroumScope parentScope) {
    if (this == NONE) {
      return;
    }

    if (this.variableDefinitions.containsKey(instanceId)
        && !this.variableDefinitions.get(instanceId).isEmpty()) {
      this.addDefinition(instanceId, node, parentScope);
    }

    // propagate them definitions way up there, put them waaay up inside there, as far as they can
    // fit
    if (this.parent != GroumScope.NONE) {
      this.parent.addDefinitionFromPropagation(instanceId, node, parentScope);
    }
  }

  public void createNewDefinition(Long instanceId, GroumNode node) {
    if (!this.variableDefinitions.containsKey(instanceId)) {
      this.variableDefinitions.put(instanceId, new HashMap<>());
    }

    var list = this.variableDefinitions.get(instanceId);
    list.clear();
    list.put(this, node);

    this.checkInstancedDefinitionsOverwriting(instanceId, List.of(node));

    this.controlFlowParent.addDefinition(instanceId, node, this);

    propagateDefinitionToParents(instanceId, node, this);
    // propagate shadowed definitions
    if (this.controlFlowParent.associatedGroumNode instanceof ControlNode controlNode
        && controlNode.controlType().equals(ControlNode.ControlType.elseStmt)) {
      var parentsParent = controlFlowParent.controlFlowParent;
      if (parentsParent.associatedGroumNode instanceof ControlNode parentsControlNode
          && parentsControlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
        // does the if-branch contain a definition for instanceID?
        // get if node
        var ifNode = parentsControlNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal).get(0);
        var ifScope = parentsParent.getConditionalScopeFor(ifNode);

        // if the if scope does not itself contain the definitions, it's block may contain it
        if (ifScope.variableDefinitions.containsKey(instanceId)) {
          var ifDefinition = ifScope.variableDefinitions.get(instanceId).values().stream().toList();
          // TODO: should pass all groumNodes, which do the shadowing
          var shadowingNodes = new ArrayList<>(ifDefinition);
          shadowingNodes.add(node);
          propagateShadowingToParents(instanceId, shadowingNodes, this, new HashSet<>());
        }
      }
    }
  }

  private void propagateShadowingToParents(
    Long instanceIdNewDefiniton, ArrayList<GroumNode> newDefinitions, GroumScope fromScope, HashSet<GroumScope> definitionsToShadow) {
    if (this.parent != GroumScope.NONE) {
      this.parent.propagateShadowing(instanceIdNewDefiniton, newDefinitions, fromScope, definitionsToShadow);
    }
  }

  private void propagateShadowing(
    Long instanceId, ArrayList<GroumNode> newDefinitions, GroumScope fromScope, HashSet<GroumScope> definitionsToShadow) {

    if (this.associatedGroumNode instanceof ControlNode controlNode
        && controlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
      propagateShadowingToParents(instanceId, newDefinitions, this, definitionsToShadow);
    } else if (this.associatedGroumNode instanceof ControlNode controlNode
        && controlNode.isConditional()
        && !this.isBranchOfIfElse()) {
      // this is a conditional statement, which is not part of an ifElse stmt
      // stop the collection of definitions to shadow
      propagateShadowingToParents(instanceId, newDefinitions, this, definitionsToShadow);
    } else {
      var definitions = this.variableDefinitions.get(instanceId);

      if (fromScope.associatedGroumNode instanceof ControlNode controlNode
          && controlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
        // collect all previous definitions to shadow
        // which will be all conditionalScopes before the 'fromScope'
        // (fromScope is guaranteed to be an ifElseScope)
        var iter = this.conditionalScopesOrdered.iterator();
        while (iter.hasNext()) {
          var next = iter.next();
          var nextScope = this.conditionalScopes.get(next);

          if (nextScope == fromScope || nextScope.heritage.contains(fromScope)) {
            break;
          }

          var shadowedScope = this.conditionalScopes.get(next);
          definitionsToShadow.add(shadowedScope);

          var heritage = shadowedScope.heritage;
          definitionsToShadow.addAll(heritage);
        }

        if (fromScope.controlFlowParent == this) {
          // don't add this scope
          // why not?
          // what does this mean?
          // if this is the control flow parent of the from scope, this means, that this is a j
          boolean b = true;
        } else {
          // definitionsToShadow.add(this);
          definitionsToShadow.add(this);
          definitionsToShadow.add(this.controlFlowParent);
        }
      }

      if (definitions != null) {
        for (var definitionToShadow : definitionsToShadow) {
          checkInstancedDefinitionsOverwriting(instanceId, newDefinitions);
          definitions.remove(definitionToShadow);
        }
      }

      propagateShadowingToParents(instanceId, newDefinitions, fromScope, definitionsToShadow);
    }
  }

  private boolean isBranchOfIfElse() {
    return this.controlFlowParent != null
        && this.controlFlowParent.associatedGroumNode instanceof ControlNode controlNode
        && controlNode.controlType().equals(ControlNode.ControlType.ifElseStmt);
  }

  @Override
  public String toString() {
    return "scope: " + this.associatedGroumNode;
  }

  private void propagateInstanceDefinitionToParents(long instanceId, long parentsInstanceId, GroumScope fromScope) {
    if (this.parent != NONE && this.parent != null) {
      this.parent.registerInstanceDefinitionFromPropagation(instanceId, parentsInstanceId, fromScope);
      this.parent.propagateInstanceDefinitionToParents(instanceId, parentsInstanceId, fromScope);
    }
  }

  private void registerInstanceDefinitionFromPropagation(long instanceId, long parentsInstanceId, GroumScope fromScope) {
    if (this.variableDefinitions.containsKey(instanceId)) {
      if (!this.instancedDefinitions.containsKey(parentsInstanceId)) {
        this.instancedDefinitions.put(parentsInstanceId, new HashSet<>());
      }
      this.instancedDefinitions.get(parentsInstanceId).add(parentsInstanceId);
    }
  }

  public void registerInstanceDefinition(long instanceId, long parentsInstanceId) {
    if (!this.instancedDefinitions.containsKey(parentsInstanceId)) {
      this.instancedDefinitions.put(parentsInstanceId, new HashSet<>());
    }

    this.instancedDefinitions.get(parentsInstanceId).add(instanceId);

    // propagate to parents..
    this.propagateInstanceDefinitionToParents(instanceId, parentsInstanceId, this);
  }
}
