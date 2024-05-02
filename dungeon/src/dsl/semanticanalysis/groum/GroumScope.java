package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;
import java.util.*;

public class GroumScope {
  public static GroumScope NONE = new GroumScope(GroumNode.NONE);
  private HashMap<Long, HashMap<GroumScope, GroumNode>> variableDefinitions = new HashMap<>();
  private HashMap<ControlNode.ControlType, GroumScope> ifElseConditionalScopes = new HashMap<>();
  private Stack<GroumNode> conditionalScopesOrdered = new Stack<>();
  private HashMap<GroumNode, GroumScope> conditionalScopes = new HashMap<>();
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

  public void pushConditionalScope(GroumScope conditionalScope) {
    this.conditionalScopes.put(conditionalScope.associatedGroumNode, conditionalScope);
    this.conditionalScopesOrdered.push(conditionalScope.associatedGroumNode);
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
        if (!(assocNodeControlFlowParent instanceof ControlNode parentControlNode && assocNodeParentOfControlFlowParent instanceof ControlNode parentsParentControlNode)) {
          break;
        }

        if (parentControlNode.controlType().equals(ControlNode.ControlType.ifStmt) && parentsParentControlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
          // get else control node
          var elseStmtNode = assocNodeParentOfControlFlowParent.getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal).get(1);
          assert elseStmtNode instanceof ControlNode;
          assert ((ControlNode)elseStmtNode).controlType().equals(ControlNode.ControlType.elseStmt);

          // block the other scope
          var scopeToBlock = parentOfControlFlowParent.getConditionalScopeFor(elseStmtNode);
          if (scopeToBlock != GroumScope.NONE) {
            scopesToBlock.add(scopeToBlock);
          }
        } else if (parentControlNode.controlType().equals(ControlNode.ControlType.elseStmt) && parentsParentControlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
          // get if control node
          var ifStmtNode = assocNodeParentOfControlFlowParent.getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal).get(0);
          assert ifStmtNode instanceof ControlNode;
          assert ((ControlNode)ifStmtNode).controlType().equals(ControlNode.ControlType.ifStmt);

          var scopeToBlock = parentOfControlFlowParent.getConditionalScopeFor(ifStmtNode);
          if (scopeToBlock != GroumScope.NONE) {
            scopesToBlock.add(scopeToBlock);
          }
        }

        // cont
        scopeToCheck = parentOfControlFlowParent;
      }
      final var finalScopesToBlock = scopesToBlock;
      var instanceDefinitions = this.variableDefinitions.get(instanceId);
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

  public void setIfElseScopes(GroumScope ifScope, GroumScope elseScope) {
    this.ifElseConditionalScopes.clear();
    this.ifElseConditionalScopes.put(ControlNode.ControlType.ifStmt, ifScope);
    this.ifElseConditionalScopes.put(ControlNode.ControlType.elseStmt, elseScope);
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

  /*private boolean doBothBranchesContainDef(Long instanceId, GroumScope ifElseScope) {
    HashMap<GroumScope, GroumNode> instanceDefinitions = this.variableDefinitions.get(instanceId);
    List<GroumNode> ifElseChildNodes = ifElseScope.associatedGroumNode().getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal);
    GroumNode ifNode = ifElseChildNodes.get(0);
    GroumNode elseNode = ifElseChildNodes.get(1);

    boolean containsDefForAllBranches = true;
    GroumScope ifScope = ifElseScope.getConditionalScopeFor(ifNode);
    if ()
    containsDefForAllBranches &= instanceDefinitions.containsKey(ifScope);
    GroumScope elseScope = ifElseScope.getConditionalScopeFor(elseNode);
    containsDefForAllBranches &= instanceDefinitions.containsKey(elseScope);

  }*/

  private void checkForShadowedDefinitions(Long instanceId, GroumScope scopeOfRecentlyAddedNode) {
    var instancesDefinitions = this.variableDefinitions.get(instanceId);

    // check, if the scopeOfRecenltyAddedNode Kind eines ifElseStmts ist, was in conditionalScopes dieser Scope
    // gespeichert ist

    /*GroumScope ifElseParentScope = scopeOfRecentlyAddedNode.controlFlowParent;
    while (ifElseParentScope != GroumScope.NONE) {
      if (ifElseParentScope.associatedGroumNode() instanceof ControlNode controlNode && controlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
        if (this.conditionalScopes.containsKey(controlNode)) {
          break;
        }
      }
    }

    if (ifElseParentScope == GroumScope.NONE) {
      return;
    }
     */

    // TODO:
    //  if the last pushed scope is an ifElse stmt and this contains definitions for both scopes
    //  this shadows the definitions in the scopes above the ifElse stmt
    //  if the last pushed scope is another conditional statement (for, while) and this contains
    //  a definition for that scope, nothing is shadowed
    //  basically determine, the last conditional scope, which contains a definition for the variable
    //  if it happens to be an if else statement and this scope stores a definition for both branches,
    //  the definitions from before are shadowed, otherwise not
    // TODO: Note: would be nice, if a scope would propagate the shadowing to their parents.. is that possible?

    // reaching check
    /*var ifElseScope = ifElseParentScope;
    HashMap<GroumScope, GroumNode> instanceDefinitions = this.variableDefinitions.get(instanceId);
    List<GroumNode> ifElseChildNodes = ifElseScope.associatedGroumNode().getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal);
    GroumNode ifNode = ifElseChildNodes.get(0);
    GroumNode elseNode = ifElseChildNodes.get(1);

    GroumScope ifScope = ifElseScope.getConditionalScopeFor(ifNode);
    instanceDefinitions.containsKey(ifScope);

    GroumScope elseScope = ifElseScope.getConditionalScopeFor(elseNode);
    instanceDefinitions.containsKey(elseScope);
     */

    // TODO: has this scope an ifElseScope in it's conditional scopes?
    //  if yes: does this scope store a definition for both branches?
    //  has one of the branches of the ifElseScope another ifElseScope?
    //  if yes: does this scope store a definition for both branches?
    //  repeat ad nauseam

    // if there are no mutually exclusive conditional scopes (a combination of one if- and one else-branch),
    // all definitions will be reachable
    if (this.ifElseConditionalScopes.isEmpty()) {
      return;
    }

    // check, if both conditional scopes contain a definition of the same variable
    for (var conditionalScope : this.ifElseConditionalScopes.values()) {
      if (!instancesDefinitions.containsKey(conditionalScope)) {
        return;
      }
    }

    // there are definitions in all conditional branches, definitions from before will not be
    // accessible
    HashSet<GroumScope> shadowedDefinitions = new HashSet<>();

    var conditionalScopeList = this.ifElseConditionalScopes.values().stream().toList();
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

    // TODO: propagate shadowed variable definitions

    /*if (this.controlFlowParent.associatedGroumNode instanceof ControlNode controlNode && controlNode.controlType().equals(ControlNode.ControlType.elseStmt)) {
      var parentsParent = controlFlowParent.controlFlowParent;
      if (parentsParent.associatedGroumNode instanceof ControlNode parentsControlNode && parentsControlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
        // does the if-branch contain a definition for instanceID?
        // get if node
        var ifNode = parentsControlNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal).get(0);
        var ifScope = parentsParent.getConditionalScopeFor(ifNode);
        var variableDefinitionsForInstanceId = parentsParent.variableDefinitions.get(instanceId);
        if (variableDefinitionsForInstanceId.containsKey(ifScope)) {
          // todo: notify parents..
          propagateShadowingToParents(instanceId, this, new ArrayList<>());
        }
      }
    }*/
  }

  private void propagateShadowingToParents(Long instanceId, GroumScope fromScope, List<GroumScope> definitionsToShadow) {
    this.parent.propagateShadowing(instanceId, fromScope, definitionsToShadow);
  }

  private void propagateShadowing(Long instanceId, GroumScope fromScope, List<GroumScope> definitionsToShadow) {
    var definitions = this.variableDefinitions.get(instanceId);
    for (var definitionToShadow : definitionsToShadow) {
      definitions.remove(definitionToShadow);
    }

    // check, if the from scope is a direct child of the else branch of this
    // if so, just propagate on from this scope
    if (this.associatedGroumNode instanceof ControlNode controlNode && controlNode.controlType().equals(ControlNode.ControlType.ifElseStmt)) {
      var elseNode = this.associatedGroumNode.getEndsOfOutgoing(GroumEdge.GroumEdgeType.temporal).get(1);
      var elseScope = this.conditionalScopes.get(elseNode);
      if (elseScope.conditionalScopes.containsKey(fromScope.associatedGroumNode)) {
        boolean yes = true;
        propagateShadowingToParents(instanceId, this, definitionsToShadow);
      }
      // TODO: what happens if not?
    } else {
      // TODO: collect all previous definitions to shadow
      //  which will be all conditionalScopes before the 'fromScope' (fromScope is guaranteed to be an ifElseScope)
      var iter = this.conditionalScopesOrdered.iterator();

      // TODO: NEXT STEP!!!
      while (iter.hasNext())  {
        var next = iter.next();
        var shadowedScope  = this.conditionalScopes.get(next);
        definitions.remove(shadowedScope);

        definitionsToShadow.add(shadowedScope);
        if (next == fromScope.associatedGroumNode) {
          break;
        }
      }

      //propagateShadowingToParents(instanceId, this, );
    }
  }
}
