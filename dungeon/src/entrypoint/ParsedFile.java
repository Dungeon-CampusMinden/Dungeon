package entrypoint;

import dsl.parser.ast.Node;
import java.nio.file.Path;

import org.neo4j.ogm.annotation.*;

@NodeEntity
public class ParsedFile {
  @Id @GeneratedValue
  private Long id;

  @Transient protected Path filePath;

  @Property
  public final String pathString;

  @Relationship
  protected Node rootASTNode;

  public Path filePath() {
    return this.filePath;
  }

  public Node rootASTNode()  {
    return this.rootASTNode;
  }

  public ParsedFile() {
    this.filePath = null;
    this.rootASTNode = Node.NONE;
    this.pathString = "";
  }

  public ParsedFile(Path filePath, Node rootAstNode) {
    this.filePath = filePath;
    this.rootASTNode = rootAstNode;
    if (filePath == null) {
      this.pathString = "";
    } else {
      this.pathString = filePath.toString();
    }
  }
}
