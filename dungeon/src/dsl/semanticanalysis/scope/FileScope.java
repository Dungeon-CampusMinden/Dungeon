package dsl.semanticanalysis.scope;

import entrypoint.ParsedFile;

public class FileScope extends Scope {
  protected final ParsedFile file;

  public FileScope(ParsedFile file, IScope parentScope) {
    super(parentScope);
    this.file = file;
  }

  public ParsedFile file() {
    return this.file;
  }
}
