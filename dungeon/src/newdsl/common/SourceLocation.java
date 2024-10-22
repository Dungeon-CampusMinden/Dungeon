package newdsl.common;

import org.antlr.v4.runtime.ParserRuleContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SourceLocation {
    private final int row;
    private final int column;
    private final String absoluteFilePath;

    public SourceLocation(int row, int column, String absoluteFilePath) {
        this.row = row;
        this.column = column;
        this.absoluteFilePath = absoluteFilePath;
    }

    public SourceLocation(ParserRuleContext ctx) {
        this.row = Utils.getLine(ctx);
        this.column = Utils.getCharPosInLine(ctx);
        this.absoluteFilePath = Utils.getFileName(ctx);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    @Override
    public String toString() {
        String fileName = "<unknown>";
        if (this.absoluteFilePath != null) {
            Path path = Paths.get(this.absoluteFilePath);
            fileName = path.getFileName().toString();
        }
        return String.format("in file '%s' at line %s:%s", fileName, this.row, this.column);
    }
}
