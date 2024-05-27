package dsl.semanticanalysis.symbol;

import dsl.parser.ast.Node;
import org.neo4j.ogm.annotation.*;

import java.util.UUID;

@RelationshipEntity(type = "CREATES")
public class SymbolCreation {
  @Id UUID id = UUID.randomUUID();
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
