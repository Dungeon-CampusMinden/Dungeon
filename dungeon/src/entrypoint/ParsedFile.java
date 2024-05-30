package entrypoint;

import dsl.IndexGenerator;
import dsl.parser.ast.Node;
import dsl.programmanalyzer.Relatable;
import dsl.programmanalyzer.Relate;
import dsl.programmanalyzer.RelationshipRecorder;
import java.nio.file.Path;
import org.neo4j.ogm.annotation.*;

@NodeEntity
public class ParsedFile implements Relatable {
  @Id @GeneratedValue private Long id;
  @Property public Long internalId = IndexGenerator.getUniqueIdx();

  @Transient protected Path filePath;

  @Property public final String pathString;

  @Relate @Transient protected Node rootASTNode;

  public Path filePath() {
    return this.filePath;
  }

  public Node rootASTNode() {
    return this.rootASTNode;
  }

  public ParsedFile() {
    this.filePath = null;
    this.rootASTNode = Node.NONE;
    this.pathString = "";

    RelationshipRecorder.instance.addRelatable(this);
    // recordRelationships();
  }

  public ParsedFile(Path filePath, Node rootAstNode) {
    this.filePath = filePath;
    this.rootASTNode = rootAstNode;
    if (filePath == null) {
      this.pathString = "";
    } else {
      this.pathString = filePath.toString();
    }

    RelationshipRecorder.instance.addRelatable(this);
    // recordRelationships();
  }

  @Override
  public Long getId() {
    return this.internalId;
  }
}
