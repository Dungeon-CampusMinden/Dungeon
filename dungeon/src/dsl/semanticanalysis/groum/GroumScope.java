package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import java.util.*;

public class GroumScope {
  public static GroumScope NONE = new GroumScope(GroumNode.NONE);
  private HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions = new HashMap<>();
  private HashSet<GroumScope> ifElseConditionalScopes = new HashSet<>();
  private GroumScope parent;
  private GroumScope controlFlowParent;
  private GroumNode associatedGroumNode;

  public GroumScope(GroumNode associatedGroumNode) {
    this.parent = NONE;
    this.controlFlowParent = NONE;
    this.associatedGroumNode = associatedGroumNode;
  }

  public GroumScope(GroumScope parent, GroumNode associatedGroumNode) {
    this.parent = parent;
    this.controlFlowParent = getConditionalParentScope(parent, -1);
    this.associatedGroumNode = associatedGroumNode;
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
      var instanceDefinitions = this.variableDefinitions.get(instanceId).values();
      if (!this.ifElseConditionalScopes.isEmpty()) {
        // block all definitions from other scope

        // check, whether the 'fromScope' is child of one of the ifElseConditional Scopes and
        // block the defintions of the other
        // TODO: this probably can be simplified with the controlFlowParent!
        GroumScope scopeToBlock = GroumScope.NONE;
        var list = ifElseConditionalScopes.stream().toList();
        for (int i = 0; i < ifElseConditionalScopes.size(); i++) {
          var scope = list.get(i);
          if (fromScope.associatedGroumNode.hasAncestorLike(scope.associatedGroumNode)) {
            if (i == 0) {
              scopeToBlock = list.get(1);
            } else {
              scopeToBlock = list.get(0);
            }
          }
        }

        if (scopeToBlock != GroumScope.NONE) {
          // need to filter out also all children of that scope (all nested if-definitions)
          // TODO: this probably can be simplified with the controlFlowParent!

          // check, whether the definition node has the scope to block node as ancestor
          final var finalScopeToBlock = scopeToBlock;
          return
              instanceDefinitions.stream()
                  .filter(n -> !n.hasAncestorLike(finalScopeToBlock.associatedGroumNode))
                  .toList();
        }
      }
      return instanceDefinitions.stream().toList();
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

  public void setIfElseScopes(GroumScope ifScope, GroumScope elseScope) {
    this.ifElseConditionalScopes.clear();
    this.ifElseConditionalScopes.add(ifScope);
    this.ifElseConditionalScopes.add(elseScope);
  }

  // this is just called from propagating a definition to parents
  private void addDefinition(Long instanceId, GroumNode node, GroumScope parentScope) {
    if (!this.variableDefinitions.containsKey(instanceId)) {
      this.variableDefinitions.put(instanceId, new HashMap<>());
    }

    var scopeToNodeMap = this.variableDefinitions.get(instanceId);
    var controlFlowParent = parentScope.controlFlowParent;
    if (this.controlFlowParent == controlFlowParent) {
      // The definition lies in the same control path as the current scope and will overwrite the definition!
      // Therefore, we need to clear all variable definitions and just add the new one.
      scopeToNodeMap.clear();
      scopeToNodeMap.put(controlFlowParent, node);
    } else if (controlFlowParent == GroumScope.NONE) {
      scopeToNodeMap.put(parentScope, node);
    } else {
      scopeToNodeMap.put(controlFlowParent, node);
    }

    var controlFlowParentNode = controlFlowParent.associatedGroumNode();
    var childScopesOfDefinitionScope = scopeToNodeMap.keySet().stream().filter(s -> s.associatedGroumNode.hasAncestorLike(controlFlowParentNode)).toList();
    for (var scope : childScopesOfDefinitionScope) {
      scopeToNodeMap.remove(scope);
    }

    checkForShadowedDefinitions(instanceId, parentScope);
  }

  private GroumScope getConditionalParentScope(GroumScope scope, int maxSearchDepth) {
    var parentScope = scope;
    int searchDepth = 0;
    while ((maxSearchDepth == -1 || searchDepth < maxSearchDepth) && parentScope != GroumScope.NONE) {
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

  private void checkForShadowedDefinitions(Long instanceId, GroumScope scopeOfRecentlyAddedNode) {
    var instancesDefinitions = this.variableDefinitions.get(instanceId);

    // if there are no mutually exclusive conditional scopes (a combination of one if- and one else-branch),
    // all definitions will be reachable
    if (this.ifElseConditionalScopes.isEmpty()) {
      return;
    }

    // check, if both conditional scopes contain a definition of the same variable
    for (var conditionalScope : this.ifElseConditionalScopes) {
      if (!instancesDefinitions.containsKey(conditionalScope)) {
        return;
      }
    }

    // there are definitions in all conditional branches, definitions from before will not be
    // accessible
    HashSet<GroumScope> shadowedDefinitions = new HashSet<>();

    var conditionalScopeList = this.ifElseConditionalScopes.stream().toList();
    var ifScope = conditionalScopeList.get(0);
    var elseScope = conditionalScopeList.get(1);

    for (var definition : instancesDefinitions.keySet()) {
      var defNode = definition.associatedGroumNode();
      var ifScopeNode = ifScope.associatedGroumNode();
      var elseScopeNode = elseScope.associatedGroumNode();

      if ((!defNode.isOrDescendentOf(ifScopeNode) && !defNode.isOrDescendentOf(elseScopeNode))){
        shadowedDefinitions.add(definition);
      }

    }

    for (var def : shadowedDefinitions) {
      this.variableDefinitions.get(instanceId).remove(def);
    }
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
    this.ifElseConditionalScopes.clear();

    propagateDefinitionToParents(instanceId, node, this);
    checkForShadowedDefinitions(instanceId, this);
  }
}
