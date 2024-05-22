package dsl.semanticanalysis.scope;

import entrypoint.ParsedFile;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class FileScope extends Scope {
  @Relationship(type = "OF_FILE", direction = Relationship.Direction.OUTGOING)
  protected ParsedFile file;

  public FileScope() {
    super(Scope.NULL);
    this.file = null;
  }

  public FileScope(ParsedFile file, IScope parentScope) {
    super(parentScope, "fs: " + file.filePath());
    this.file = file;
  }

  public ParsedFile file() {
    return this.file;
  }
}
