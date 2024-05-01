package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import java.util.*;

public class GroumScope {
  public static GroumScope NONE = new GroumScope(GroumNode.NONE);

  // TODO: THIS DOES NOT WORK!!
  //  if a variable is re-defined in a block, this information won't be carried
  //  out of this scope...this is a flawed idea!
  //  .
  //  Okay - Idea: we need to keep track of all possible execution flows, where a variable COULD
  //  be set (there is a write operation regarding the instance id in question)
  //  rules for that:
  //  - while: could be bypassed, so two execution flows
  //  - if/else: could not be bypassed, depends on the statements in bodies
  //  - ...
  //  - basically we need to find out, where the control flow regarding variable writes converges
  //  ...
  //  is this necessary? could we just go the easy route and just calculate data dependencies
  // without
  //  regard of the type of involvement?? like the original paper..

  // TODO: keep track of competing definitions of conditional branches
  //  if a variable is redefined in all conditional branches, the original
  //  definition is not longer reachable
  // TODO: need a way of modelling conditional branches..
  //  basically for all conditional nodes we have a new branch
  //  we can only ever have one active definition of a variable per branch at a time!
  // private HashMap<Long, List<GroumNode>> variableDefinitions = new HashMap<>();
  private HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions = new HashMap<>();
  private HashSet<GroumScope> ifElseConditionalScopes = new HashSet<>();
  // TODO: calculate
  private HashMap<Long, GroumNode> shadowedDefinitions = new HashMap<>();
  private GroumScope parent;
  private GroumNode associatedGroumNode;

  public GroumScope(GroumNode associatedGroumNode) {
    this.parent = NONE;
    this.associatedGroumNode = associatedGroumNode;
  }

  public GroumScope(GroumScope parent, GroumNode associatedGroumNode) {
    this.parent = parent;
    this.associatedGroumNode = associatedGroumNode;
  }

  // public HashMap<Long, List<GroumNode>> variableDefinitions() {
  public HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions() {
    return this.variableDefinitions;
  }

  public GroumNode associatedGroumNode() {
    return this.associatedGroumNode;
  }

  public List<GroumNode> getDefinitions(Long instanceId) {
    // TODO: filter out non-reachable definitions, which get shadowed by more recent definitions
    /*if (this.variableDefinitions.containsKey(instanceId)) {
      return this.variableDefinitions.get(instanceId).values().stream().filter(n -> this.shadowedDefinitions.get(instanceId) != n).toList();
    } else {
      if (this.parent != NONE) {
        return this.parent.getDefinitions(instanceId);
      } else {
        return new ArrayList<>();
      }
    }*/
    return this.getDefinitions(instanceId, this);
  }

  public List<GroumNode> getDefinitions(Long instanceId, GroumScope fromScope) {
    // TODO: filter out non-reachable definitions, which get shadowed by more recent definitions
    if (this.variableDefinitions.containsKey(instanceId)) {
      var filtered =
          this.variableDefinitions.get(instanceId).values().stream()
              .filter(n -> this.shadowedDefinitions.get(instanceId) != n);
      if (!this.ifElseConditionalScopes.isEmpty()) {
        // block all definitions from other scope

        /*var conditionalFromScopeParent = getConditionalParentScope(fromScope);
        if (conditionalFromScopeParent == GroumScope.NONE) {
          conditionalFromScopeParent = fromScope;
        }*/
        // TODO: check, whether the 'fromScope' is child of one of the ifElseConditional Scopes and
        // block the
        //  defintions of the other
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
          var intermediaryList = filtered.toList();
          var definitionsFromScope = this.variableDefinitions.get(instanceId).get(scopeToBlock);
          final var finalScopeToBlock = scopeToBlock;

          // TODO: need to filter out also all children of that scope (all nested if-definitions)
          //  NEXT STEP!!!

          // check, whether the definition node has the scope to block node as ancestor
          // var filteredList = intermediaryList.stream().filter(n ->
          // !n.equals(definitionsFromScope)).toList();
          var filteredList =
              intermediaryList.stream()
                  .filter(n -> !n.hasAncestorLike(finalScopeToBlock.associatedGroumNode))
                  .toList();
          return filteredList;
        }
      }
      return filtered.toList();
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

    var map = this.variableDefinitions.get(instanceId);

    // parent scope is required to be of a conditional type
    var immediateParentScope = getConditionalParentScope(parentScope, 1);

    if (immediateParentScope.associatedGroumNode() instanceof ControlNode controlNode && controlNode.controlType().equals(ControlNode.ControlType.block)) {
      // the parent scope of the passed parent scope is a block (nested block!)
      // this overwrites any previous definitions!
      // treat it as a new variable definition!

      //var conditionalParentScope = getConditionalParentScope(immediateParentScope, -1);
      // TODO: unwrap the parentScope until it is a conditional
      //propagateDefinitionToParents(instanceId, node, immediateParentScope);

      // TODO: need to debug this and the definition getting when proc idx == 31 (variable ref)
      //  NEXT STEP !!!
      // Note: this likely does not work, because propagation is done after the call to this addDefinition anyways..
      immediateParentScope.addDefinition(instanceId, node, immediateParentScope);
    }

    if (immediateParentScope == GroumScope.NONE) {
      // TODO: is this correct?
      map.put(parentScope, node);
    } else {
      map.put(immediateParentScope, node);
    }

    checkForShadowedDefinitions(instanceId);
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

  private void checkForShadowedDefinitions(Long instanceId) {
    var instancesDefinitions = this.variableDefinitions.get(instanceId);

    // if there are no mutually exclusive conditional scopes (a combination of one if- and one else-branch),
    // all definitions will be reachable
    if (this.ifElseConditionalScopes.isEmpty()) {
      return;
    }

    // check, if both conditional scopes contain a definition of the same variable
    for (var conditionalScope : this.ifElseConditionalScopes) {
      if (!instancesDefinitions.containsKey(conditionalScope)) {
        this.shadowedDefinitions.remove(instanceId);
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

    //var shadowedDefinitions = instancesDefinitions.keySet().stream().filter(n -> !shadowedDefinitions.contains(n)).toList();

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
    if (node.getDefinitionSymbol().getName().equals("x")) {
      boolean b = true;
    }
    if (!this.variableDefinitions.containsKey(instanceId)) {
      this.variableDefinitions.put(instanceId, new HashMap<>());
    }

    var list = this.variableDefinitions.get(instanceId);
    list.clear();
    list.put(this, node);
    this.ifElseConditionalScopes.clear();

    propagateDefinitionToParents(instanceId, node, this);
    checkForShadowedDefinitions(instanceId);
  }
}
