package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import java.util.*;

public class GroumScope {
  public static GroumScope NONE = new GroumScope(GroumNode.NONE);
  private HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions = new HashMap<>();
  private Stack<GroumNode> conditionalScopesOrdered = new Stack<>();
  private HashMap<GroumNode, GroumScope> conditionalScopes = new HashMap<>();
  private GroumScope parent;
  private GroumScope controlFlowParent;
  private HashSet<GroumScope> children = new HashSet<>();
  private HashSet<GroumScope> heritage = new HashSet<>();
  private GroumNode associatedGroumNode;

  public GroumScope(GroumNode associatedGroumNode) {
    this.parent = NONE;
    this.controlFlowParent = NONE;
    this.associatedGroumNode = associatedGroumNode;
  }

  public GroumScope(GroumScope parent, GroumNode associatedGroumNode) {
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
    } else if (controlFlowParent == GroumScope.NONE) {
      scopeToNodeMap.put(parentScope, node);
    } else {
      scopeToNodeMap.put(controlFlowParent, node);
    }

    var childScopesOfDefinitionScope = controlFlowParent.heritage;
    for (var scope : childScopesOfDefinitionScope) {
      scopeToNodeMap.remove(scope);
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
          propagateShadowingToParents(instanceId, this, new HashSet<>());
        }
      }
    }
  }

  private void propagateShadowingToParents(
      Long instanceId, GroumScope fromScope, HashSet<GroumScope> definitionsToShadow) {
    if (this.parent != GroumScope.NONE) {
      this.parent.propagateShadowing(instanceId, fromScope, definitionsToShadow);
    }
  }

  private void propagateShadowing(
      Long instanceId, GroumScope fromScope, HashSet<GroumScope> definitionsToShadow) {

    // check, if the from scope is a direct child of the else branch of this
    // if so, just propagate on from this scope
    if (this.associatedGroumNode instanceof ControlNode controlNode
        && controlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
      propagateShadowingToParents(instanceId, this, definitionsToShadow);
    } else {
      // collect all previous definitions to shadow
      // which will be all conditionalScopes before the 'fromScope'
      // (fromScope is guaranteed to be an ifElseScope)
      var iter = this.conditionalScopesOrdered.iterator();

      var definitions = this.variableDefinitions.get(instanceId);
      // TODO: also collect the definitions, which are just children of the current scope
      while (iter.hasNext()) {
        var next = iter.next();
        if (next == fromScope.associatedGroumNode) {
          break;
        }
        var shadowedScope = this.conditionalScopes.get(next);
        definitionsToShadow.add(shadowedScope);
        definitionsToShadow.addAll(shadowedScope.heritage);

        // TODO: also add all scope of which the shadowed scope is an ancestor
        /*for (var definition : definitions.keySet()) {
          if (definitionsToShadow.contains(definition)) {
            // skip
            continue;
          }
          if (definition.associatedGroumNode.hasAncestorLike(shadowedScope.associatedGroumNode)) {
            definitionsToShadow.add(definition);
          }
        }*/
      }

      if (fromScope.controlFlowParent != this) {
        definitionsToShadow.add(this);
      }

      if (definitions != null) {
        for (var definitionToShadow : definitionsToShadow) {
          definitions.remove(definitionToShadow);
        }
      }

      propagateShadowingToParents(instanceId, fromScope, definitionsToShadow);
    }
  }
}
