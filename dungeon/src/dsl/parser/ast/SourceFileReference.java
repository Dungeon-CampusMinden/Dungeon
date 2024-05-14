package dsl.parser.ast;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class SourceFileReference {
  @Id @GeneratedValue private Long id;
  @Property int startLine;
  @Property int startColumn;
  @Property int endLine;
  @Property int endColumn;

  /**
   * @return The line number of this SourceFileReference
   */
  public int getStartLine() {
    return startLine;
  }

  /**
   * @return The column number of this SourceFileReference
   */
  public int getStartColumn() {
    return startColumn;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getEndColumn() {
    return endColumn;
  }

  /**
   * Constructor
   *
   * @param sourceLine The line number of the new SourceFileReference
   * @param sourceColumn The column number of the new SourceFileReference
   */
  public SourceFileReference(int sourceLine, int sourceColumn) {
    this.startLine = sourceLine;
    this.startColumn = sourceColumn;
    this.endLine = sourceLine;
    this.endColumn = sourceColumn;
  }

  public SourceFileReference(int sourceLine, int sourceColumn, int endLine, int endColumn) {
    this.startLine = sourceLine;
    this.startColumn = sourceColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public static SourceFileReference NULL = new SourceFileReference(-1, -1);

  /**
   * @return String representation of this SourceFileReference
   */
  public String toString() {
    return "["
        + this.startLine
        + ":"
        + this.startColumn
        + " - "
        + this.endLine
        + ":"
        + this.endColumn
        + "]";
  }
}
