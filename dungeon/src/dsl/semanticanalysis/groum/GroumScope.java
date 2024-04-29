package dsl.semanticanalysis.groum;

import dsl.semanticanalysis.symbol.Symbol;

import java.util.HashMap;

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
  private HashMap<Long, GroumNode> variableDefinitions = new HashMap<>();
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

  public GroumNode getDefinition(Long instanceId) {
    if (this.variableDefinitions.containsKey(instanceId)) {
      return this.variableDefinitions.get(instanceId);
    } else {
      if (this.parent != NONE) {
        return this.parent.getDefinition(instanceId);
      } else {
        return GroumNode.NONE;
      }
    }
  }

  public Symbol getDefinitionSymbol(Long instanceId) {
    GroumNode definitionNode = this.getDefinition(instanceId);
    return definitionNode.getDefinitionSymbol();
  }


  public void addDefinition(Long instanceId, GroumNode node) {
    this.addDefinition(instanceId, node, false);
  }

  public void addDefinition(Long instanceId, GroumNode node, boolean overwrite) {
    if (this.variableDefinitions.containsKey(instanceId)) {
      if (overwrite) {
        this.variableDefinitions.put(instanceId, node);
      }
    } else {
      this.variableDefinitions.put(instanceId, node);
    }
  }
}
