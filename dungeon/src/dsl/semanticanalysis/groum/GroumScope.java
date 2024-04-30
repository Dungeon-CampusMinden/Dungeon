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
  //  is this necessary? could we just go the easy route and just calculate data dependencies without
  //  regard of the type of involvement?? like the original paper..


  // TODO: keep track of competing definitions of conditional branches
  //  if a variable is redefined in all conditional branches, the original
  //  definition is not longer reachable
  // TODO: need a way of modelling conditional branches..
  //  basically for all conditional nodes we have a new branch
  //  we can only ever have one active definition of a variable per branch at a time!
  //private HashMap<Long, List<GroumNode>> variableDefinitions = new HashMap<>();
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

  //public HashMap<Long, List<GroumNode>> variableDefinitions() {
  public HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions() {
    return this.variableDefinitions;
  }

  public GroumNode associatedGroumNode() {
    return this.associatedGroumNode;
  }

  public List<GroumNode> getDefinitions(Long instanceId) {
    // TODO: filter out non-reachable definitions, which get shadowed by more recent definitions
    if (this.variableDefinitions.containsKey(instanceId)) {
      return this.variableDefinitions.get(instanceId).values().stream().filter(n -> this.shadowedDefinitions.get(instanceId) != n).toList();
    } else {
      if (this.parent != NONE) {
        return this.parent.getDefinitions(instanceId);
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

  // this may be just called from propagation...
  private void addDefinition(Long instanceId, GroumNode node, GroumScope parentScope) {
    if (node.getDefinitionSymbol().getName().equals("x")) {
      boolean b = true;
    }

    if (!this.variableDefinitions.containsKey(instanceId)) {
      this.variableDefinitions.put(instanceId, new HashMap<>());
    }

    var map = this.variableDefinitions.get(instanceId);
    // parent scope is required to be of a conditional type
    var conditionalParentScope = getConditionalParentScope(parentScope);
    if (conditionalParentScope == GroumScope.NONE) {
      // TODO: is this correct?
      map.put(parentScope, node);
    } else {
      map.put(conditionalParentScope, node);
    }

    checkForShadowedDefinitions(instanceId);
  }

  private GroumScope getConditionalParentScope(GroumScope scope) {
    var parentScope = scope;
    while (parentScope != GroumScope.NONE) {
      if (parentScope.associatedGroumNode instanceof ControlNode controlNode &&
        (controlNode.controlType() != ControlNode.ControlType.block && controlNode.controlType() != ControlNode.ControlType.returnStmt)) {
        break;
      }
      parentScope = parentScope.parent;
    }
    return parentScope;
  }

  private void checkForShadowedDefinitions(Long instanceId) {
    var instancesDefinitions = this.variableDefinitions.get(instanceId);

    if (this.ifElseConditionalScopes.isEmpty()) {
      return;
    }

    for (var conditionalScope : this.ifElseConditionalScopes) {
      if (!instancesDefinitions.containsKey(conditionalScope)) {
        this.shadowedDefinitions.remove(instanceId);
        return;
      }
    }

    // there are definitions in all conditional branches, definitions from before will not be
    // accessible
    var shadowedDefinitions = instancesDefinitions.entrySet().stream().filter(e -> !this.ifElseConditionalScopes.contains(e.getKey())).map(Map.Entry::getValue).toList();

    // TODO: is this really expected to be always only one?!
    if (shadowedDefinitions.size() != 1) {
      boolean b = true;
    }
    this.shadowedDefinitions.put(instanceId, shadowedDefinitions.get(0));
  }


  protected void propagateDefinitionToParents(Long instanceId, GroumNode node, GroumScope parentScope) {
    if (this == NONE || this.parent == NONE) {
      return;
    }

    var parentsList = this.parent.getDefinitions(instanceId);
    if (!parentsList.isEmpty()) {
      this.parent.addDefinitionFromPropagation(instanceId, node, parentScope);
    }
  }

  protected void addDefinitionFromPropagation(Long instanceId, GroumNode node, GroumScope parentScope) {
    if (this == NONE) {
      return;
    }

    if (this.variableDefinitions.containsKey(instanceId) && !this.variableDefinitions.get(instanceId).isEmpty()) {
      this.addDefinition(instanceId, node, parentScope);
    }

    // propagate them definitions way up there, put them waaay up inside there, as far as they can fit
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
