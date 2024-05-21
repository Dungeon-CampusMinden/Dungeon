package dsl.parser.ast;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
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
  @Property int absoluteStart;
  @Property int absoluteEnd;

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
    this.absoluteEnd = -1;
    this.absoluteStart = -1;
  }

  public SourceFileReference(int sourceLine, int sourceColumn, int endLine, int endColumn) {
    this.startLine = sourceLine;
    this.startColumn = sourceColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
    this.absoluteEnd = -1;
    this.absoluteStart = -1;
  }

  public SourceFileReference(int sourceLine, int sourceColumn, int endLine, int endColumn, int absoluteStart, int absoluteEnd) {
    this.startLine = sourceLine;
    this.startColumn = sourceColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
    this.absoluteStart = absoluteStart;
    this.absoluteEnd = absoluteEnd;
  }

  public SourceFileReference() {
    this.startLine = -1;
    this.startColumn = -1;
    this.endLine = -1;
    this.endColumn = -1;
    this.absoluteStart = -1;
    this.absoluteEnd = -1;
  }

  public static SourceFileReference fromCtx(ParserRuleContext ctx) {
    int startLine = ctx.start.getLine()-1;
    int startColumn = ctx.start.getCharPositionInLine();
    int endLine;
    int endColumn;

    if (ctx.stop == null) {
      endLine = ctx.start.getLine()-1;
      endColumn = ctx.start.getCharPositionInLine();
    } else {
      endLine = ctx.stop.getLine()-1;
      endColumn = ctx.stop.getCharPositionInLine();
    }
    int absoluteStart = -1;
    int absoluteEnd = -1;
    if (ctx.start instanceof CommonToken commonToken) {
      absoluteStart = commonToken.getStartIndex();
    }
    if (ctx.stop instanceof CommonToken commonToken) {
      absoluteEnd = commonToken.getStopIndex();
    }
    var sourceFileReference = new SourceFileReference(startLine, startColumn, endLine, endColumn, absoluteStart, absoluteEnd);
    return sourceFileReference;
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
        + "]"
        + " - absolute: ["
        + this.absoluteStart
        + ":"
        + this.absoluteEnd
        + "]"
        ;
  }
}
