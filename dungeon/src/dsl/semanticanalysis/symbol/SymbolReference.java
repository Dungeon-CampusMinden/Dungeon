package dsl.semanticanalysis.symbol;

import dsl.parser.ast.Node;
import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type="REFERENCES")
public class SymbolReference {
  @Id
  @GeneratedValue
  private Long id;
  @StartNode
  private Node astNode;
  @EndNode
  private Symbol symbol;

  public SymbolReference() {
    this.astNode = Node.NONE;
    this.symbol = Symbol.NULL;
  }

  public SymbolReference(Node astNode, Symbol symbol) {
    this.astNode = astNode;
    this.symbol = symbol;
  }
}
