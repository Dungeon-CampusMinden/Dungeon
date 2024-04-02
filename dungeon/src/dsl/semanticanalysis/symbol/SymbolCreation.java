package dsl.semanticanalysis.symbol;

import dsl.parser.ast.Node;
import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "CREATES")
public class SymbolCreation {
  @Id @GeneratedValue private Long id;
  @StartNode private Node astNode;
  @EndNode private Symbol symbol;

  public SymbolCreation() {
    this.astNode = Node.NONE;
    this.symbol = Symbol.NULL;
  }

  public SymbolCreation(Node astNode, Symbol symbol) {
    this.astNode = astNode;
    this.symbol = symbol;
  }
}
