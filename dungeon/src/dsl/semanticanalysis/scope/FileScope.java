package dsl.semanticanalysis.scope;

import dsl.programmanalyzer.Relate;
import dsl.programmanalyzer.RelationshipRecorder;
import entrypoint.ParsedFile;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

@NodeEntity
public class FileScope extends Scope {
  @Relate(type = "OF_FILE")
  @Transient
  protected ParsedFile file;

  public FileScope() {
    super(Scope.NULL);
    this.file = null;
    RelationshipRecorder.instance.addRelatable(this);
  }

  public FileScope(ParsedFile file, IScope parentScope) {
    super(parentScope, "fs: " + file.filePath());
    this.file = file;
    RelationshipRecorder.instance.addRelatable(this);
  }

  public ParsedFile file() {
    return this.file;
  }
}
