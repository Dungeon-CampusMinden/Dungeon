package dsl.parser.ast;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

// TODO: substitute this for more sophisticated method of source reference...
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

  public SourceFileReference combineWith(SourceFileReference other) {
    // smaller start
    int startLine;
    int startColumn;

    if (this == SourceFileReference.NULL) {
      return other;
    }
    if (other == SourceFileReference.NULL) {
      return this;
    }

    if (this.startLine == other.startLine) {
      startLine = this.startLine;
      startColumn = Math.min(this.endColumn, other.startColumn);
    } else {
      if (this.startLine < other.startLine) {
        startLine = this.startLine;
        startColumn = this.startColumn;
      } else {
        startLine = other.startLine;
        startColumn = other.startColumn;
      }
    }

    // bigger end
    int endLine;
    int endColumn;
    if (this.endLine == other.endLine) {
      endLine = this.endLine;
      endColumn = Math.max(this.endColumn, other.endColumn);
    } else {
      if (this.endLine > other.endLine) {
        endLine = this.endLine;
        endColumn = this.endColumn;
      } else {
        endLine = other.endLine;
        endColumn = other.endColumn;
      }
    }
    return new SourceFileReference(startLine, startColumn, endLine, endColumn);
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
