package parser.AST;

public class SourceFileReference {
    int line;
    int column;

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public SourceFileReference(int sourceLine, int sourceColumn) {
        line = sourceLine;
        column = sourceColumn;
    }

    public static SourceFileReference NULL = new SourceFileReference(-1, -1);

    public String toString() {
        return "l: " + this.line + ", c: " + this.column;
    }
}
