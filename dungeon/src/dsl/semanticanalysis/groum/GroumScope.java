package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
  private HashMap<Long, List<GroumNode>> variableDefinitions = new HashMap<>();
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

  public GroumNode associatedGroumNode() {
    return this.associatedGroumNode;
  }

  public List<GroumNode> getDefinitions(Long instanceId) {
    if (this.variableDefinitions.containsKey(instanceId)) {
      return this.variableDefinitions.get(instanceId);
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

  // this may be just called from propagation...
  private void addDefinition(Long instanceId, GroumNode node) {
    if (!this.variableDefinitions.containsKey(instanceId)) {
      this.variableDefinitions.put(instanceId, new ArrayList<>());
    }

    var list = this.variableDefinitions.get(instanceId);
    list.add(node);
  }

  protected void propagateDefinitionToParents(Long instanceId, GroumNode node) {
    if (this == NONE || this.parent == NONE) {
      return;
    }

    var parentsList = this.parent.getDefinitions(instanceId);
    if (!parentsList.isEmpty()) {
      this.parent.addDefinitionFromPropagation(instanceId, node);
    }
  }

  protected void addDefinitionFromPropagation(Long instanceId, GroumNode node) {
    if (this == NONE) {
      return;
    }

    if (this.variableDefinitions.containsKey(instanceId) && !this.variableDefinitions.get(instanceId).isEmpty()) {
      this.addDefinition(instanceId, node);
    } else {
      if (this.parent != GroumScope.NONE) {
        this.parent.addDefinitionFromPropagation(instanceId, node);
      }
    }
  }

  public void createNewDefinition(Long instanceId, GroumNode node) {
    if (!this.variableDefinitions.containsKey(instanceId)) {
      this.variableDefinitions.put(instanceId, new ArrayList<>());
    }

    var list = this.variableDefinitions.get(instanceId);
    list.clear();
    list.add(node);

    propagateDefinitionToParents(instanceId, node);
  }
}
