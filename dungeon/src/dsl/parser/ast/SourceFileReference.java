package dsl.parser.ast;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

// TODO: substitute this for more sophisticated method of source reference...
//@NodeEntity
public class SourceFileReference {
  @Id @GeneratedValue private Long id;
  @Property int line;
  @Property int column;

  /**
   * @return The line number of this SourceFileReference
   */
  public int getLine() {
    return line;
  }

  /**
   * @return The column number of this SourceFileReference
   */
  public int getColumn() {
    return column;
  }

  /**
   * Constructor
   *
   * @param sourceLine The line number of the new SourceFileReference
   * @param sourceColumn The column number of the new SourceFileReference
   */
  public SourceFileReference(int sourceLine, int sourceColumn) {
    line = sourceLine;
    column = sourceColumn;
  }

  public SourceFileReference() {
    line = -1;
    column = -1;
  }

  public static SourceFileReference NULL = new SourceFileReference(-1, -1);

  /**
   * @return String representation of this SourceFileReference
   */
  public String toString() {
    return "l: " + this.line + ", c: " + this.column;
  }
}
