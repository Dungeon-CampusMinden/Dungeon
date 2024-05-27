package dsl.semanticanalysis.symbol;

import dsl.parser.ast.Node;
import org.neo4j.ogm.annotation.*;

import java.util.UUID;

@RelationshipEntity(type = "REFERENCES")
public class SymbolReference {
  @Id UUID id = UUID.randomUUID();
  @StartNode private Node astNode;
  @EndNode private Symbol symbol;

  public Symbol getSymbol() {
    return symbol;
  }

  public Node getAstNode() {
    return astNode;
  }

  public SymbolReference() {
    this.astNode = Node.NONE;
    this.symbol = Symbol.NULL;
  }

  public SymbolReference(Node astNode, Symbol symbol) {
    this.astNode = astNode;
    this.symbol = symbol;
  }
}
