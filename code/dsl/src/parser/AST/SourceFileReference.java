package parser.AST;

public class SourceFileReference {
    int line;
    int column;

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

    public static SourceFileReference NULL = new SourceFileReference(-1, -1);

    /**
     * @return String representation of this SourceFileReference
     */
    public String toString() {
        return "l: " + this.line + ", c: " + this.column;
    }
}
